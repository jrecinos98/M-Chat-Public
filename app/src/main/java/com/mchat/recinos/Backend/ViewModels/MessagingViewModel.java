package com.mchat.recinos.Backend.ViewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Messages.Message;
import com.mchat.recinos.Backend.Repository;

import java.util.List;

public class MessagingViewModel extends AndroidViewModel {
    private Repository mRepository;
    //private MutableLiveData<List<Message>> mutableList;
    private LiveData<List<Message>> messageList;

    public MessagingViewModel(Application application, Client client){
        super(application);
        mRepository = new Repository(application, client);
    }
    public MessagingViewModel(Application application, Client client, LifecycleOwner owner, int cid){
        super(application);
        mRepository = new Repository(application, client);
        messageList = mRepository.getMessageList(cid);
        //mutableList = new MutableLiveData<>();
        //Here we make we bind the owner to listen to any message that is added
        /*mRepository.getAllMessages().observe(owner, (messages) -> {
            //TODO to optimize better I can make the observer look at a latest_message table that records the cid of the latest message
            // this way when a new message is added we can check this cid and if it matches current cid then we fetch the message list
            // otherwise for every new message we would be refetching the whole list.
            // TIP: IF YOU EVER TRY TO DO THIS AGAIN. AUTOGENERATED MESSAGE ID IS GENERATED AFTER IT IS INSERTED SO KEY WILL ALWAYS BE SAME. (NOT SURE)
            mutableList.setValue(mRepository.getMessageList(cid));
        });*/

    }
    //Return reference to the MutableLiveData so observer will be notified when data is updated.
    public LiveData<List<Message>> getCurrentChatMessages() {
        return messageList;
    }

    public int getChatID(String uid){return mRepository.getChatID(uid);}

    public boolean chatExists(String friend_uid){return mRepository.chatExists(friend_uid);}

    //Wrapper around repository insert method. Helps with the abstraction
    public void sendMessage(Message message) { mRepository.sendMessage(message); }

    public void insert(Chat chat){ mRepository.insert(chat);}


    public void updateChatPreview(String uid, String preview, long time, int unread) { mRepository.updateChatPreview(uid, preview, time, unread );}

    public void deleteChat(int id, List<Message> messages){
        mRepository.delete(id, messages);
    }

    //Factory to use different constructors for view models
    public static class MessageViewModelFactory implements ViewModelProvider.Factory {
        private Application mApplication;
        private LifecycleOwner owner;
        private int cid;
        private  Client client;


        public MessageViewModelFactory(Application application, Client client, LifecycleOwner lifecycleOwner, int id) {
            mApplication = application;
            owner= lifecycleOwner;
            this.client = client;
            this.cid = id;
        }
        public MessageViewModelFactory(Application application, Client client){
            mApplication = application;
            this.client = client;
        }

        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

            if(owner == null){
                return (T) new MessagingViewModel(mApplication, client);
            }
            return (T) new MessagingViewModel(mApplication, client, owner, cid);
        }
    }
}
