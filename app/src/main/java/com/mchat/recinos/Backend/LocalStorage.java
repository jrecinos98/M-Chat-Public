package com.mchat.recinos.Backend;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.mchat.recinos.Util.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Objects;

public class LocalStorage {

    //TODO Verify it works
    public static Uri saveToPublicExternalStorage(Context context, String fileType, String extension, Bitmap image, String filename) {
        ContentResolver resolver = context.getContentResolver();
        Uri imageUri = IO.createPlaceHolder(context, fileType, filename, extension);
        try {
            OutputStream fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
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
    /**
     * Saves the serializable object to a file in internal storage.
     */
    public static <T extends Serializable> void saveInternalFile(Context context, String directory, String fileName, T objectToSave) {
        try {
            File subDir= new File(context.getFilesDir() + File.separator+ directory );
            //mkdir and mkdirs are different. mkdirs creates the full path??
            subDir.mkdirs();
            Log.d("WRITE_IO", "Directory: " + subDir + " Filename: " + fileName);
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
}
