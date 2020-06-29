package com.mchat.recinos.Backend.ViewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Repository;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private Repository mRepository;

    public ChatViewModel (Application application) {
        super(application);
        mRepository = new Repository(application);
    }
    public LiveData<List<Chat>> getChats() { return mRepository.getChatList(); }

    public LiveData<List<Chat>> getOrderedChats() { return mRepository.getOrderedChats();}

    //Wrapper around repository insert method. Helps with the abstraction
    public void insert(Chat chat) { mRepository.insert(chat); }

    public void updateChatPreview(String uid, String preview, long time, int unread){ mRepository.updateChatPreview(uid, preview, time, unread);}
}
