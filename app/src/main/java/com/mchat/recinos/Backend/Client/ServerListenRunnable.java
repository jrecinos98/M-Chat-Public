package com.mchat.recinos.Backend.Client;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mchat.recinos.AsyncTasks.ImageDownloadTask;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Backend.Entities.ImageMessage;
import com.mchat.recinos.Backend.Entities.Message;
import com.mchat.recinos.Backend.ViewModels.ClientViewModel;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.CONSTANTS;
import com.mchat.recinos.Util.IO;
import com.mchat.recinos.Util.Util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import Protobuf.ProtoMessage;

//TODO, To avoid race condition when using imagedownload task by using the URL (or UID) as key to get the chat that needs to get image
// set. When the callback (onImageDownloadResult) defined below is called. We return the image bytes and the URL(or UID) used to get the right chat from hashmap

class ServerListenRunnable implements Runnable, ImageDownloadTask.DownloadResultCallback{
    private AtomicBoolean running = new AtomicBoolean(false);
    private Connection connection ;
    private Context context;
    private ClientViewModel clientViewModel;

    //Chats that are in the process of being created (Downloading an image)
    private final HashMap<Integer, Chat> pendingChats;

    //Messages that belong to a chat currently being created
    private final HashMap<String, List<Message>> pendingMessages;

    //Integer: ID , ProtoMessage.Message: Message sent
    private final Map<Integer, ProtoMessage.Message> unAckedMessages;

    /**
     * @param context Application context
     * @param connection Connection information
     * @param unAckedMessages List of messages that have yet to receive an ACK
     */
    public ServerListenRunnable(Context context, Connection connection, Map<Integer, ProtoMessage.Message> unAckedMessages ){
        this.context = context;
        this.connection = connection;
        this.clientViewModel = new ClientViewModel((Application) context);

        this.pendingChats = new HashMap<>();
        this.pendingMessages = new HashMap<>();
        this.unAckedMessages = unAckedMessages;
    }

    /**
     * Stops the read thread.
     */
    public void interrupt() {
        running.set(false);
    }

    private ProtoMessage.Message readMessage(){
        byte[] sizeByte = IO.readFromStream(connection.input, 4);
        if(sizeByte == null){
            connection.shutOff();
            return null;
        }
        int size = ByteBuffer.wrap(sizeByte).getInt();
        ProtoMessage.Message message = null;
        try {
            byte[] msg_bytes = IO.readFromStream(connection.input, size);
            message = ProtoMessage.Message.parseFrom(msg_bytes);
        }catch (InvalidProtocolBufferException e){
            e.getUnfinishedMessage();
        }
        return  message;
    }

    /**
     * Sends ACK of received message to the server. Starts a new thread
     * @param id
     */
    private void sendAck(int id){
        ProtoMessage.Message ack =ProtoMessage.Message.newBuilder()
                .setType(CONSTANTS.PROTO_MESSAGE_DATA_TYPES.ACK)
                .setMsgId(id)
                .build();
        WriteToServerRunnable ackRunnable = new WriteToServerRunnable(ack, connection.output);
        //Start new thread to send the ACK
        (new Thread(ackRunnable)).start();
    }

    private void handleNonTextMessage(Message message, ProtoMessage.Message protoMessage){
        String extension = protoMessage.getMessage().getMimeType();
        if(message.getType() == CONSTANTS.MESSAGE_DATA_TYPES.IMAGE){
            ImageMessage imageMessage = (ImageMessage) message;
            String fileName = Util.generateFileNameWithExtension(String.valueOf(message.getTimeStamp()),  extension);
            Bitmap sentImage = ((ImageMessage) message).getImage();
            ((ImageMessage) message).setImage(BitmapTransform.scaleUpBitmap(sentImage,350, context));
            imageMessage.setUri(fileName);
        }
        else if(message.getType() == CONSTANTS.MESSAGE_DATA_TYPES.AUDIO){

        }
    }
    public void run(){
        running.set(true);
        while (running.get() && connection.isOpen()) {
            //TODO do not use readlines as any new lines in the message will split the message.
            //TODO if server closes connection we read empty string and it crashes. Useful for now but fix eventually. In the future use a broadcast to let listeners know the connection has died
            ProtoMessage.Message protoMessage = readMessage();
            if(protoMessage != null) {
                //Log.d("CLIENT_READ", protoMessage.toString());
                long receivedTime = Calendar.getInstance().getTime().getTime();
                if (protoMessage.getType() == CONSTANTS.PROTO_MESSAGE_DATA_TYPES.ACK) {
                    synchronized (unAckedMessages) {
                        unAckedMessages.remove(protoMessage.getMsgId());
                    }
                    continue;
                }
                //Send ACK
                sendAck(protoMessage.getMsgId());

                //Convert ProtoBuf to a Message object
                Message msg = Message.parseProtoBuf(protoMessage.getMessage(), receivedTime);
                if (msg.getType() != CONSTANTS.MESSAGE_DATA_TYPES.TEXT) {
                    handleNonTextMessage(msg, protoMessage);
                }
                String sender_id = protoMessage.getSenderUid();
                //Message msg = createMessage(message, receivedTime);

                Log.d("SERVER_LISTEN", "MSG_TYPE -> " + msg.getType() + " SENDER -> " + sender_id + " TEXT -> " + msg.getData());

                //Check if the chat this message belongs to is in progress and add to the list if it is
                //The list will only be non-null if a chat with the same uid is in progress
                if(pendingMessages.get(sender_id) != null){
                    pendingMessages.get(sender_id).add(msg);
                }
                // If the chat already exists.
                else if (clientViewModel.chatExists(sender_id)) {
                    addToExistingChat(sender_id, msg);
                }
                // If the friend data needs to be fetched
                else {
                    addToNewChat(sender_id, msg);
                }
            }
        }
    }

    /**
     * @param sender_id ID of the user that sent the message
     * @param msg Message object
     */
    private void addToExistingChat(String sender_id, Message msg){
        Chat chat = clientViewModel.getChat(sender_id);
        msg.setOwner_cid(chat.getCid());
        int unread_count = chat.getUnreadCount();
        if(chat.getCid() != Client.CURRENT_CHAT)
            unread_count++;
        clientViewModel.updateChatPreview(sender_id,  msg.getPreview(), Util.getTime(), unread_count);
        clientViewModel.insert(msg);
    }

    /**
     * @param sender_id ID of the user that sent the message
     * @param msg Message object
     */
    private void addToNewChat(String sender_id, Message msg){

        //Add the received message to the HashMap of pending messages (Any other received message with tht UID will be added)
        List<Message> messages =  new ArrayList<>(Collections.singletonList(msg));
        pendingMessages.put(sender_id, messages);

        //Using uid get username
        CloudDatabase.getInstance().getUserName(sender_id).addOnCompleteListener((userNameTask) -> {
            if (userNameTask.isSuccessful()){
                DocumentSnapshot usernameDoc = userNameTask.getResult();
                if(usernameDoc != null) {
                    String username = (String) usernameDoc.get(CONSTANTS.USERS_ENTRY.USERNAME);
                    //Using username get public data
                    CloudDatabase.getInstance().getUserInfoFromUserName(username).addOnCompleteListener((userInfoTask) -> {
                        if (userInfoTask.isSuccessful()) {
                            DocumentSnapshot userInfoDoc = userInfoTask.getResult();
                            if (userInfoDoc != null) {
                                Map<String, Object> data = userInfoDoc.getData();
                                if(data != null) {
                                    Contact friend = Util.createFriendUser(data, username);
                                    //Generate new unique ID
                                    int cid = Util.newChatID(context);

                                    //Add the chat to the HashMap of chats in progress
                                    Chat chat = new Chat(cid, friend, msg.getPreview(), Util.getTime());
                                    pendingChats.put(cid, chat);

                                    startDownload(friend, cid);
                                }
                            }
                        } else
                            Log.d("SERVER_LISTEN", "Error getting public data");
                    });
                }
            }
            else
                Log.d("SERVER_LISTEN", "Error getting username");
        });
    }
    /**
     *  DownloadImageTask to fetch the friend's image
     * @param friend Contact object containing all the relevant information about the sender
     */
    private void startDownload(Contact friend, int cid){
        //Start task to fetch the contact's image
        ImageDownloadTask.DownloadImageTask imageTask = new ImageDownloadTask.DownloadImageTask(cid, ServerListenRunnable.this);
        try {
            imageTask.execute(friend.getPhoto_URL()).get(); //get() will make the thread wait till execution is done
        }catch(Exception e){
            Log.d("SERVER_LISTEN", e.toString());
        }
    }
    /**
     * Callback for ImageDownloadTask
     * @param id Chat identifier number
     * @param image returns the downloaded image as bytes
     */
    public void onImageDownloadResult(int id , byte[] image){
        Chat chat;
        //It should be fine to remove chat here. If any message for this new chat arrives then it will be added to the message list
        synchronized (pendingChats) {
            chat = pendingChats.remove(id);
        }
        if(chat != null) {
            /*BitmapTransform.SerialBitmap serialBitmap = new BitmapTransform.SerialBitmap(image);
            String filename = Util.generateFileNameWithExtension(chat.getFriendUid(), "jpg");
            //IO.saveInternalFile(context, CONSTANTS.INTERNAL_DIRECTORY.CONTACT_IMAGES, filename, serialBitmap);*/
            //Update image on chat
            chat.getContact().setImage(image);

            //Retrieve the outstanding messages list. Use synchronized block to prevent race condition.
            //This method may get called right as a new message is added that should belong to list
            //If we synchronize then any time this ist is used outside this thread it will block.
            synchronized (pendingMessages) {
                //remove list
                List<Message> messages = pendingMessages.remove(chat.getFriendUid());
                if (messages != null) {
                    //Save all the pending messages and increase count
                    for (int i = 0; i < messages.size(); i++) {
                        Message message = messages.get(i);
                        //update the preview to match the last received msg
                        if(i == (messages.size()-1)){
                            chat.setPreview(message.getPreview());
                        }
                        //Set the msg owner ID or else it wont be matched
                        message.setOwner_cid(chat.getCid());
                        chat.increaseUnread();
                        clientViewModel.insert(messages.get(i));
                    }
                }
                clientViewModel.insert(chat);
            }
        }
    }
}