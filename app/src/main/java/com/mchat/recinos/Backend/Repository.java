package com.mchat.recinos.Backend;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.Task;
import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.DAOs.ChatDao;
import com.mchat.recinos.Backend.DAOs.MessageDao;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Messages.ImageMessage;
import com.mchat.recinos.Backend.Entities.Messages.Message;
import com.mchat.recinos.Backend.Entities.User;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.Constants;
import com.mchat.recinos.Util.IO;
import com.mchat.recinos.Util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TODO use Repository Pattern. This way all data handling is performed by the repository (both local data persistence and remote)
public class Repository {
    private ChatDao chatDao;
    private MessageDao messageDao;
    private LiveData<List<Chat>> chatList;
    private WeakReference<Context> context;
    private Client client;


    private static final int NUMBER_OF_TASKS = 3;
    //Used whenever a write request is to be made to the database. Need to call execute on it and pass a lambda func (runnable)
    public final ExecutorService task = Executors.newFixedThreadPool(NUMBER_OF_TASKS);

    public Repository(Application application, Client c) {
        LocalDatabase db = LocalDatabase.getDatabase(application);
        chatDao = db.chatDao();
        messageDao = db.messageDao();
        chatList = chatDao.getTimeOrderedChats();
        context = new WeakReference<>(application);
        client = c;
    }

    public Task<Void> googleSignOut(){
        return Authentication.googleSignOut(context.get());
    }
    //TODO return a Task<Void> so we can attach a listener
    public void deleteUser(User user){
        CloudDatabase.getInstance().deleteUser(user);
    }
    public void signOut(){
        Authentication.getInstance().signOut();
    }
    public void resetClient(){
        client.disconnect();
        client = null;
        LocalDatabase.reset();
    }


    //TODO add option to only read 50 items at a time only.
    public LiveData<List<Chat>> getChatList() {
        return chatList;
    }

    public LiveData<List<Chat>> getOrderedChats() { return chatDao.getTimeOrderedChats();}

    public LiveData<List<Chat>> getNameOrderedChats(){ return chatDao.getNameOrderedChats();}

    public int getChatID(String uid){ return chatDao.getChatID(uid);}

    public Chat getChatWithUID(String uid){return chatDao.getChatWithUID(uid);}

    public Chat getChat(int cid){ return chatDao.getChat(cid);}

    public boolean chatExists(String friend_uid){
        List<Chat> chat = chatDao.chatExists(friend_uid);
        return chat.size() > 0;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(Chat chat) {
        //WriteExecutor writes in a new thread
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            chatDao.insert(chat);
        });
    }

    public void updateChatPreview(String uid, String preview, long time, int unread) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            chatDao.updatePreview(uid, preview, time, unread);
        });
    }

    public void delete(int cid, List<Message> messages){
        LocalDatabase.databaseWriteExecutor.execute(()->{
            chatDao.delete(cid);
            for(int i =0 ; i < messages.size(); i++){
                Message message = messages.get(i);
                if(message.getType() == Constants.MESSAGE_DATA_TYPES.IMAGE){
                    if(context.get() != null) {
                        Log.d("REPOSITORY", "Deleting: "+ message.getData());
                        LocalStorage.deleteInternalFile(context.get(), IO.formatSubDirectory(Constants.INTERNAL_DIRECTORY.MESSAGE_IMAGES, String.valueOf(message.getOwner_cid())), message.getData());
                    }
                }
                delete(message);
            }
        });
    }
    //Deletes a message from their respective table and from Message table
    private void delete(Message message){
        messageDao.delete(message);
    }


    /***********************************************************************************************
     *                                   MESSAGE METHODS                                           *
     * *********************************************************************************************

     /*
     *  Find all messages for a given chat (using cid)
     */
    public LiveData<List<Message>> getMessageList(int cid) {
        return messageDao.getTimeSortedChatMessages(cid);
    }

    public LiveData<List<Message>> getAllMessages(){ return messageDao.getAll();}

    /**
     * Used anytime we want to send a message to the server.
     * @param message The message we want to send (Image, Video, text, etc...)
     */
    public void sendMessage(Message message){
        switch(message.getType()){
            case Constants.MESSAGE_DATA_TYPES.TEXT:
                handleMessage(message);
                break;
            case Constants.MESSAGE_DATA_TYPES.IMAGE:
                sendImageMessage((ImageMessage) message);
                break;
            case Constants.MESSAGE_DATA_TYPES.AUDIO:
                break;
        }
        //May be unnecessary as  sendMessage already starts a new thread.
    }

    /**
     * Stores message on the database and sends the message over the network.
     * It assumes that all relevant fields in message have been populated.
     * @param message Message
     */
    private void handleMessage(Message message){
        client.networkIO.execute(()->{
            //TODO fix when Chat does not exist like on NewMessageActivity.
            client.sendMessage(getChat(message.getOwner_cid()).getUID(), message);
            Log.d("CHAT_ACTIVITY", "Sent to server");
        });
        saveMessage(message);
    }


    /**
     * called everytime a new message is received.
     * @param message Adds a received message to the database.
     */
    public void receiveMessage(Message message){
        saveMessage(message);
    }

    /**
     * Hidden method. Only accessible through sendMessage and ReceiveMessage.
     * Saves the given message to the database.
     * @param message Message to be saved.
     */
    private void saveMessage(Message message){
        task.execute(()->{
            String preview = message.getPreview();
            //Save image to internal storage
            if(message.getType() == Constants.MESSAGE_DATA_TYPES.IMAGE) {
                Log.d("REPOSITORY", String.valueOf(((ImageMessage) message).getImageBytes().length) );
                LocalStorage.saveInternalFile(context.get(), IO.formatSubDirectory(Constants.INTERNAL_DIRECTORY.MESSAGE_IMAGES, String.valueOf(message.getOwner_cid())), message.getData(), new BitmapTransform.SerialBitmap(((ImageMessage) message).getImage()));
            }
            messageDao.insert(message);
            chatDao.updatePreview(
                    getChat(message.getOwner_cid()).getUID(),
                    preview,
                    Util.getTime(),
                    0);
        });
    }

    /**
     * Takes care of Bitmap operations on images in separate thread.
     * Once the Bitmap transformations are done it calls handleMessage to persist and forward.
     * @param imageMessage ImageMessage to be sent over the network
     */
    private void  sendImageMessage( ImageMessage imageMessage){
        //TODO show a temporary loading image
        //Init a long lasting task in a separate thread.
        task.execute(()->{
            try {
                Uri imageUri = imageMessage.getUri();
                if(imageUri != null) {
                    //TODO if extension isn't in compress list do not attempt to compress.
                    //Use the file extension to determine the type of compression to use
                    final Bitmap.CompressFormat compressionType = Constants.BITMAP_FORMATS.get(
                            Util.getExtension(imageMessage.getFileName())
                    );
                    ///ALL THIS JUST TO OBTAIN OPTIONS

                    //Read image from stream
                    InputStream imageStream = context.get().getContentResolver().openInputStream(imageUri);
                    // decode image size (decode metadata only, not the whole image)
                    BitmapFactory.Options options = BitmapTransform.readMetaData(imageStream);
                    String imageType = options.outMimeType;
                    options.inSampleSize = BitmapTransform.calculateInSampleSize(options, 600, 600);
                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;

                    ///NOW READ IMAGE

                    imageStream = context.get().getContentResolver().openInputStream(imageUri);
                    // decode full image
                    Bitmap internal = BitmapFactory.decodeStream(imageStream, null, options);
                    //Afterwards rotate the image if necessary
                    Bitmap internalBitmap = BitmapTransform.correctBitmap(context.get(), internal, imageUri, internal.getWidth());
                    //Compress the image by 30%
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    internalBitmap.compress(compressionType, 70, out);
                    //Update the image message
                    imageMessage.setImage(BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray())));

                    //Add to database
                    handleMessage(imageMessage);
                }

            }catch (IOException e){
                e.printStackTrace();
            }
        });



    }


}
