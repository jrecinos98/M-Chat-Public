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

    //TODO Verify it works
    public static Uri saveToPublicExternalStorage(Context context, String fileType, String extension, Bitmap image, String filename) {
        ContentResolver resolver = context.getContentResolver();
        Uri imageUri =createPlaceHolder(context, fileType, filename, extension);
        try {
            OutputStream fos = fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            //When saving to external storage do not use lossless compression. Want to keep the best quality possible
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Objects.requireNonNull(fos).close();
        }catch(IOException e) {
            e.printStackTrace();
            // Don't leave an orphan entry in the MediaStore
            resolver.delete(imageUri, null, null);
        }
        return imageUri;
    }

    //TODO Used when saving data to apps private external storage directory
    public static Uri saveToPrivateExternalStorage(){
        return null;
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
            dir.mkdir();
            File file = new File(dir.toString(), filename + "." + extension);
            imageUri = Uri.fromFile(file);
        }
        return imageUri;
    }
    /**
     * Saves the serializable object to a file in internal storage.
     */
    public static <T extends Serializable> void saveInternalFile(Context context, String directory, String fileName, T objectToSave) {
        try {
            File subDir= new File(context.getFilesDir() + File.separator+ directory );
            subDir.mkdirs();
            Log.d("WRITE_IO", "Directory: "+ subDir + " Filename: " +  fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(subDir, fileName));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(objectToSave);
            objectOutputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            Log.d("ERROR_WRITE", e.toString());
            e.printStackTrace();
        }
    }
    /**
     * Loads a serializable object.
     *
     * @param context The application context.
     * @param <T> The object type.
     *
     * @return the serializable object.
     */
    public static<T extends Serializable> T readInternalFile(Context context, String directory, String fileName) {
        File file= new File(context.getFilesDir()+ File.separator+ directory + File.separator + fileName);
        if (!file.exists()){
            Log.d("NOT_FOUND", "FILE NOT FOUND");
            return null;
        }
        T save = null;
        try {
            Log.d("READ_IO", "Directory: "+ file + "Filename: "+ fileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            save = (T) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
        }
        return save;
    }
    /**
     * Removes a specified file.
     *
     * @param context The application context.
     */
    public static void deleteInternalFile(Context context,String directory, String fileName) {
        File file= new File(context.getFilesDir()+ File.separator+ directory + File.separator + fileName );
        Log.d("DELETE_IO", "Directory: "+ file);
        if(file.exists()) {
            if(file.delete())
                Log.d("DELETE_IO", "DELETED");
        }
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
