package com.mchat.recinos.Backend.ViewModels;

import android.app.Application;

import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Message;
import com.mchat.recinos.Backend.Repository;

public class ClientViewModel {
    private Repository mRepository;

    public ClientViewModel (Application application) {
        mRepository = new Repository(application);
    }

    public int getChatID(String uid){return mRepository.getChatID(uid);}
    public Chat getChat(String uid){return mRepository.getChat(uid);}

    public boolean chatExists(String friend_uid){return mRepository.chatExists(friend_uid);}

    //Wrapper around repository insert method. Helps with the abstraction
    public void insert(Message message) { mRepository.insert(message); }

    public void insert(Chat chat){ mRepository.insert(chat);}

    public void updateChatPreview(String uid, String preview, long time, int unread) {mRepository.updateChatPreview(uid, preview, time, unread);}




}
