package com.mchat.recinos.Util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.mchat.recinos.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Objects;

public  class IO {
    public static String formatSubDirectory(String parent, String cid){
        return parent + File.separator + cid;
    }

    public static byte[] readFromStream(InputStream input, int size){
        byte[] data = new byte[size];
        int recvd = 0;
        int old_recvd;
        while (recvd < size){
            try {
                old_recvd = recvd;
                byte[] temp = new byte[size-recvd];
                recvd += input.read(temp, 0, temp.length);
                if(recvd == 0){
                    return null;
                }
                System.arraycopy(temp, 0, data, old_recvd, temp.length);
            }catch (IOException e){
                e.printStackTrace();
                return null;
            }
        }

        return data;
    }

    /**
     * Make the method synchronized so that there is only one writer into the output stream at a time.
     * @param output The stream to write to
     * @param messageBytes The message that is to be sent as a byte array
     */
    public static synchronized void writeToStream(OutputStream output, byte[] messageBytes){
        try {
            //Turn size into 4 bytes big endian
            ByteBuffer size = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            size.putInt(messageBytes.length);
            output.write(size.array());
            output.write(messageBytes);
        }catch (IOException i){
            Log.d("CLIENT", "Error_writing to server");
        }
    }

    public static Uri createPlaceHolder(Context context, String fileType, String filename, String extension){
        Uri imageUri = Uri.EMPTY;
        //If Android version is greater than Android Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename + "." + extension);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, fileType+extension);
            //Specify the path to store image
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + context.getResources().getString(R.string.app_name));

            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            //Create directory specific for my app
            File dir = new File(imagesDir, File.separator+  context.getResources().getString(R.string.app_name));
            dir.mkdirs();

            File file = new File(dir.toString(), filename + "." + extension);
            imageUri = Uri.fromFile(file);
        }
        return imageUri;
    }

    /**
     * Checks whether a save file exists.
     * @return true if save file exists and false otherwise
     */
    private static boolean fileExists(String fileName, String[] files){
        for (String file : files) {
            Log.d("FILES", "File: " + file);
            if (file.equals(fileName)) {
                return true;
            }
        }
        return false;
    }
}
