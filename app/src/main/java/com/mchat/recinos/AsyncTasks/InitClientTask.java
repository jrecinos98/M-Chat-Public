package com.mchat.recinos.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.MyApplication;

import java.lang.ref.WeakReference;


public class InitClientTask extends AsyncTask<Integer, Void,Integer > {
        private WeakReference<ProgressBar> progressBar;
        private WeakReference<Context> appContext;
        public InitClientTask(Context context, ProgressBar p){
            if(p != null)
                progressBar = new WeakReference<ProgressBar>(p);
            appContext= new WeakReference<Context>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected Integer doInBackground(Integer... c) {

            //If getClient is called and the client is in the process of being created then two connections will be established with the server.
            MyApplication.initClient();
            //Allow to attempt connection for ~ 10 seconds. Will end if client connects or if ~ 10 seconds go by
            repeat(10, MyApplication.getClient());
            if (MyApplication.getClient() != null && MyApplication.getClient().isConnected()){
                return 1;
            }
            return -1;
        }
        private void repeat(int n, Client client){
            int num = n*5;
            int count = 0;
            //Every 1/5 of a second check whether the client connected
            while (count < num) {
                if(client != null && client.isConnected()){
                    return;
                }
                try{
                    Thread.sleep( 200 );
                }catch ( InterruptedException e ){
                    e.printStackTrace();
                }
                count++;
            }
        }
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result > 0){
                if(progressBar != null) {
                    //Using a weak reference
                    ProgressBar p = progressBar.get();
                    if (p != null) {
                        progressBar.get().setVisibility(View.GONE);
                        Log.d("CLIENT", MyApplication.getClient().getUserID() + "->" + MyApplication.getClient().getPrimaryUser().getPhotoURL());
                        //Toast.makeText(getApplicationContext(),
                        //        "Connected to server", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else{
                if (appContext.get() != null)
                    Toast.makeText(appContext.get(),"Failed to connect. Try again.", Toast.LENGTH_LONG).show();
          }
      }
}

