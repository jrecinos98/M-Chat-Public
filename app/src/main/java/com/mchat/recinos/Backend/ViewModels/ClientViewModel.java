package com.mchat.recinos.Backend.ViewModels;

import android.app.Application;

import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Messages.Message;
import com.mchat.recinos.Backend.Repository;

public class ClientViewModel {
    private Repository mRepository;

    public ClientViewModel (Application application, Client client) {
        mRepository = new Repository(application, client);
    }

    public int getChatID(String uid){return mRepository.getChatID(uid);}
    public Chat getChatWithUID(String uid){return mRepository.getChatWithUID(uid);}

    public boolean chatExists(String friend_uid){return mRepository.chatExists(friend_uid);}

    //Wrapper around repository insert method. Helps with the abstraction
    public void sendMessage(Message message) { mRepository.sendMessage(message); }

    public void receiveMessage(Message message) {mRepository.receiveMessage(message);}

    public void insertNewChat(){

    }

    public void insert(Chat chat){ mRepository.insert(chat);}

    public void updateChatPreview(String uid, String preview, long time, int unread) {mRepository.updateChatPreview(uid, preview, time, unread);}




}
