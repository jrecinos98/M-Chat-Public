package com.mchat.recinos;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.MultiDexApplication;

import com.mchat.recinos.AsyncTasks.InitClientTask;
import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.Entities.User;
import com.mchat.recinos.Backend.LocalDatabase;

public class MyApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    private static Client client;
    public static LocalBroadcastManager broadcastManager;
    public static  Application app;
    private int activity_num= 0;


    @Override
    public void onCreate() {
        super.onCreate();
        //This should be fine since it holds a reference to the application which does not get garbage collected.
        app = this;
        registerActivityLifecycleCallbacks(this);
        //broadcastManager= LocalBroadcastManager.getInstance(this);
    }

    //May return client with null connection if connection to server fails or if the async process has not concluded
    public static Client getClient(){
        if(client == null && User.getLogInUser()!=null){
            client= new Client(app, User.getLogInUser());
        }
        //If client was initiated just now the connection may be null
        return  client;
    }
    //Initialize the client and server connection
    public static void initClient(){
        if(client == null && User.getLogInUser() != null){
            client= new Client(app, User.getLogInUser());
        }
        if (!client.isConnected()){
            client.initClient();
        }
    }

    public static void resetClient(){
        client.disconnect();
        client= null;
        //Reset the Singleton variable
        LocalDatabase.reset();
    }
    public static void startAsync(ProgressBar p){
        if(!MyApplication.getClient().isConnected()){
            p.setIndeterminate(true);
            InitClientTask task = new InitClientTask(app, p);
            task.execute();
        }
    }
    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        activity_num++;
    }
    @Override
    public void onActivityStarted(Activity activity) {
    }
    @Override
    public void onActivityResumed(Activity activity) {
        /*
        if(User.getLogInUser() != null) {
            if (!MyApplication.getClient().isConnected()) {
                InitClientTask task = new InitClientTask(this, null);
                task.execute();
                Log.d("APPLICATION_DEBUG", "USING OnActivityResumed");
            }
        }
        */

    }
    @Override
    public void onActivityPaused(Activity activity) {
    }
    @Override
    public void onActivityStopped(Activity activity) {
        activity_num--;
    }
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }
    @Override
    public void onActivityDestroyed(Activity activity) {
    }


}
