package com.mchat.recinos.Backend.ViewModels;

import android.app.Application;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.User;
import com.mchat.recinos.Backend.Repository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private Repository mRepository;

    public HomeViewModel(Application application, Client client) {
        super(application);
        mRepository = new Repository(application, client);

    }
    public void deleteUser(ProgressBar progressBar){
        progressBar.setVisibility(View.VISIBLE);
        mRepository.resetClient();
        mRepository.googleSignOut().addOnCompleteListener((task)-> {
           if(task.isSuccessful()){
               mRepository.deleteUser(User.getLogInUser());
               mRepository.signOut();

           }
            progressBar.setVisibility(View.INVISIBLE);
        });
    }
    public LiveData<List<Chat>> getChats() { return mRepository.getChatList(); }

    public LiveData<List<Chat>> getOrderedChats() { return mRepository.getOrderedChats();}

    //Wrapper around repository insert method. Helps with the abstraction
    public void insert(Chat chat) { mRepository.insert(chat); }

    public void updateChatPreview(String uid, String preview, long time, int unread){ mRepository.updateChatPreview(uid, preview, time, unread);}

    //Factory to use different constructors for view models
    public static class HomeViewModelFactory implements ViewModelProvider.Factory {
        private Application mApplication;
        private LifecycleOwner owner;
        private int cid;
        private  Client client;

        private ProgressBar progressBar;


        public HomeViewModelFactory(Application application, Client client, LifecycleOwner lifecycleOwner, int id) {
            mApplication = application;
            owner= lifecycleOwner;
            this.client = client;
            this.cid = id;
        }
        public HomeViewModelFactory(Application application, Client client){
            mApplication = application;
            this.client = client;
        }

        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

            return (T) new HomeViewModel(mApplication, client);
        }
    }
}
