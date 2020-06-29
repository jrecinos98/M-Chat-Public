package com.mchat.recinos.AsyncTasks;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Util.BitmapTransform;

import java.io.IOException;
import java.io.InputStream;

public class ImageDownloadTask {
    public static int DEFAULT_ID = -1;
    public static byte[] downloadImage(String url, Contact friend) throws IOException{
        byte[] image = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            android.graphics.Bitmap b = BitmapFactory.decodeStream(in);
            image = BitmapTransform.SerialBitmap.toArray(b);
            //friend.setImage(image);
        }catch(IOException e){
            throw new IOException();
        }
        return image;
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, byte []> {
        Contact contact;
        DownloadResultCallback receiver;
        int id;
        public DownloadImageTask( int id, DownloadResultCallback receiver) {
            Log.d("START_DOWNLOAD", "CREATED");
            this.receiver = receiver;
            this.id = id;
        }
        protected byte[] doInBackground(String... urls) {
            byte [] image = null;
            Log.d("LEN_URLS", urls.length+"");
            String urldisplay = urls[0];
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                android.graphics.Bitmap b = BitmapFactory.decodeStream(in);
                image = BitmapTransform.SerialBitmap.toArray(b);

            } catch (Exception e) {
                Log.e("Error_New_Message", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        protected void onPostExecute(byte[] result) {
            if(contact != null){
                contact.setImageBytes(result);
            }
            else {
                Log.d("FINISH_DOWNLOAD", "DONE");
                receiver.onImageDownloadResult(id, result);
            }
        }

    }
    public interface DownloadResultCallback{
        void onImageDownloadResult(int id, byte[] result);
    }
}
