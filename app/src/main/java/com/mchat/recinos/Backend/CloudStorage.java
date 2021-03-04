package com.mchat.recinos.Backend;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mchat.recinos.Util.Constants;

import java.io.ByteArrayOutputStream;

public class CloudStorage {
    private FirebaseStorage storage;
    private static CloudStorage instance;

    private CloudStorage(){
        storage = FirebaseStorage.getInstance("gs://m-chat-be7c0.appspot.com/");
    }

    public static CloudStorage getInstance(){
        if(instance == null){
            instance = new CloudStorage();
        }
        return instance;
    }
    public Task<Uri> uploadUserImage(String imageName, ByteArrayOutputStream image){
        if(imageName.equals(Constants.DEFAULT)){
            return storage.getReference(Constants.COLLECTIONS.DEFAULT_IMAGES).getDownloadUrl();
        }
        StorageReference storageRef = storage.getReference(Constants.COLLECTIONS.PROFILE_IMAGES+ "/" + imageName);
        UploadTask uploadTask= storageRef.putBytes(image.toByteArray());
        //After uploading initiate another task (a continuation) to fetch download url.
        return uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return storageRef.getDownloadUrl();
            }
        });
    }
}
