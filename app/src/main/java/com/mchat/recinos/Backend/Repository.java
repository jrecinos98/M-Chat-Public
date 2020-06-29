package com.mchat.recinos.Backend;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.mchat.recinos.Backend.DAOs.ChatDao;
import com.mchat.recinos.Backend.DAOs.MessageDao;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.ImageMessage;
import com.mchat.recinos.Backend.Entities.Message;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.CONSTANTS;
import com.mchat.recinos.Util.IO;

import java.lang.ref.WeakReference;
import java.util.List;

//TODO Repository can be wrapper to both local and cloud database (though cloud is used mainly for logging in for now).
// Maybe when we can update profile and need to sync both local and cloud it will be useful
public class Repository {
    private ChatDao chatDao;
    private MessageDao messageDao;
    private LiveData<List<Chat>> chatList;
    private WeakReference<Context> context;

    public Repository(Application application) {
        LocalDatabase db = LocalDatabase.getDatabase(application);
        chatDao = db.chatDao();
        messageDao = db.messageDao();
        chatList = chatDao.getTimeOrderedChats();
        context = new WeakReference<>(application);
    }

    //TODO add option to only read 50 items at a time only.
    public LiveData<List<Chat>> getChatList() {
        return chatList;
    }

    public LiveData<List<Chat>> getOrderedChats() { return chatDao.getTimeOrderedChats();}

    public LiveData<List<Chat>> getNameOrderedChats(){ return chatDao.getNameOrderedChats();}

    public int getChatID(String uid){ return chatDao.getChatID(uid);}

    public Chat getChat(String uid){return chatDao.getChat(uid);}

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
                if(message.getType() == CONSTANTS.MESSAGE_DATA_TYPES.IMAGE){
                    if(context.get() != null) {
                        Log.d("REPOSITORY", "Deleting: "+ message.getData());
                        IO.deleteInternalFile(context.get(), IO.formatSubDirectory(CONSTANTS.INTERNAL_DIRECTORY.MESSAGE_IMAGES, String.valueOf(message.getOwner_cid())), message.getData());
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

    public void insert(Message message){
        //WriteExecutor writes in a new thread
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            //Save image to internal storage
            if(message.getType() == CONSTANTS.MESSAGE_DATA_TYPES.IMAGE)
                IO.saveInternalFile(context.get(), IO.formatSubDirectory(CONSTANTS.INTERNAL_DIRECTORY.MESSAGE_IMAGES, String.valueOf(message.getOwner_cid())), message.getData(), new BitmapTransform.SerialBitmap(((ImageMessage) message).getImage()));
            messageDao.insert(new Message(message));
        });
    }

}
