package com.mchat.recinos.Backend;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Charsets;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.Entities.User;
import com.mchat.recinos.Util.CONSTANTS;
import com.mchat.recinos.Util.Encryption;
import com.mchat.recinos.Util.Util;

import java.io.ByteArrayOutputStream;
import java.util.Map;

//Singleton class
public class CloudDatabase {
    //These are the default values
    private static String SERVER_IP = "10.0.2.2";
    private static int SERVER_PORT = 18000;

    private static final CloudDatabase ourInstance = new CloudDatabase();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public static CloudDatabase getInstance() {
        return ourInstance;
    }

    private CloudDatabase() {
        mAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance("gs://m-chat-be7c0.appspot.com/");
    }
    public Task<AuthResult> emailSignUp( String email, String password){
        return mAuth.createUserWithEmailAndPassword(email, password);
    }
    public Task<AuthResult> googleSignUp(AuthCredential credential){
        return mAuth.signInWithCredential(credential);
    }

    public void createUser(Context context, String uid, String name, String userName, String email, String photoURL, ByteArrayOutputStream image, AuthInterface listener){
        String imageName = image == null ? CONSTANTS.DEFAULT : uid;
        uploadUserImage(imageName, image).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                String url =photoURL;
                //If image was successfully uploaded update url else use default
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null)
                        url = downloadUri.toString();
                }
                Map<String, Object> user_obj = Util.makeUserObj(userName);
                byte[] public_key = "public_key".getBytes(Charsets.UTF_8);
                //Generate public and private keys and stores them with provider
                boolean success = Encryption.generateUserKeys(context);
                if(success){
                    //Turn public key to byte array to upload to firestore.
                    public_key = Encryption.getKey(CONSTANTS.KEY.PUBLIC).getEncoded();
                }
                //Create public data with uid, actual name, email, public key and photoURL
                Map<String, Object> usr_public_obj = Util.makePublicUserData(uid, name, email,public_key, url);
                //Update profile photoURL and the displayName()
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(userName)
                        .setPhotoUri(Uri.parse(url)).build();
                //Update profile info
                updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        db.collection(CONSTANTS.COLLECTIONS.USERS).document(uid)
                                .set(user_obj)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("FIREBASE", "DocumentSnapshot successfully written!");
                                            createPublicData(usr_public_obj, userName, listener);
                                        } else {
                                            Log.d("MY_FIREBASE", "Error writing document");
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }
    private void createPublicData(Map<String, Object> p, String doc, AuthInterface listener){
        db.collection(CONSTANTS.COLLECTIONS.PUBLIC_DATA).document(doc)
                .set(p)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onAuthSuccessful(task.isSuccessful());
                        Log.d("MY_FIREBASE", "Created user public data successfully");
                    }
                });
    }
    private Task<Void> updateProfile(UserProfileChangeRequest profile){
        return mAuth.getCurrentUser().updateProfile(profile);
    }
    public void userNameSignIn(String userName, String password, AuthInterface listener){
        getUserInfoFromUserName(userName).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    Map<String, Object> data = doc.getData();
                    String email = (String) data.get(CONSTANTS.PUBLIC_DATA_ENTRY.EMAIL);
                    emailSignIn(email, password, listener);
                }else{
                    listener.onAuthSuccessful(false);
                }
            }
        });

    }
    public void emailSignIn(String email, String password, AuthInterface listener){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                listener.onAuthSuccessful(task.isSuccessful());
            }
        });
    }

    public void setUpServerConnection(final Client client){
        db.collection(CONSTANTS.COLLECTIONS.SERVER).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        updateServer(document.getData());
                        //Update the client information and connect
                        client.connectToServer(SERVER_IP, SERVER_PORT);
                        Log.d("MY_FIREBASE", "IP: " + SERVER_IP + " => PORT: " + SERVER_PORT);
                    }
                } else {
                    Log.w("ERROR", "Error getting documents.", task.getException());
                }
            }
        });
    }
    private void updateServer(Map<String, Object> m){
        //SET SERVER INFO
        SERVER_PORT = Integer.valueOf((String) m.get("PORT"));
        SERVER_IP = (String) m.get("IP");

    }
    public boolean firstLogIn(){
        return false;
    }
    public void signOut(){
        mAuth.signOut();
    }
    public FirebaseAuth getAuth(){return mAuth;}
    public FirebaseUser getCurrentUser(){
        return mAuth.getCurrentUser();
    }
    public Task<DocumentSnapshot> getUserInfoFromUserName(String username){
        return db.collection(CONSTANTS.COLLECTIONS.PUBLIC_DATA).document(username).get();
    }
    public Task<DocumentSnapshot> getUserName(String uid){
        return db.collection(CONSTANTS.COLLECTIONS.USERS).document(uid).get();
    }
    //This monster is needed to completely delete a user and their data.
    private void deleteMessage(String id, String uid){
        db.collection(CONSTANTS.COLLECTIONS.MESSAGES).document(uid).collection(CONSTANTS.COLLECTIONS.SENDERS).document(id).delete();
    }
    private Task<Void> deleteDocument(String collectionName, String docID){
        return  db.collection(collectionName).document(docID).delete();
    }
    private CollectionReference getMessageSubCollection(String uid){
        return db.collection(CONSTANTS.COLLECTIONS.MESSAGES).document(uid).collection(CONSTANTS.COLLECTIONS.SENDERS);
    }
    //In the future maybe send out a request to server so that server can deal with it.
    public void deleteUser(User user){
        //Delete messages
        getMessageSubCollection(user.getUserID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            //Delete every document in the SENDERS collection
                            deleteMessage(document.getId(), user.getUserID());
                        }
                    }
                    //After deleting sub-collection delete the user document
                    deleteDocument(CONSTANTS.COLLECTIONS.MESSAGES, user.getUserID()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task1) {
                            if (task1.isSuccessful()) {
                                //Delete user public data
                                deleteDocument(CONSTANTS.COLLECTIONS.PUBLIC_DATA, user.getUsername()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task2) {
                                        if (task2.isSuccessful()) {
                                            //Delete the user entry in USER
                                            deleteDocument(CONSTANTS.COLLECTIONS.USERS, user.getUserID()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task3) {
                                                    if (task3.isSuccessful()) {
                                                        //Lastly delete authenticated user
                                                        getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task4) {
                                                                if (task4.isSuccessful()) {
                                                                    Log.d("MY_FIREBASE", "Removed user successfully");
                                                                }
                                                                else if(task4.isCanceled()){
                                                                    Log.d("MY_FIREBASE_ERROR", "Failed to remove user");
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else if(task3.isCanceled()){
                                                        Log.d("MY_FIREBASE_ERROR", "Failed to delete USER entry");
                                                    }
                                                }
                                            });
                                        }
                                        else if(task2.isCanceled()){
                                            Log.d("MY_FIREBASE_ERROR", "Failed to delete public data");
                                        }
                                    }
                                });
                            }
                            else if(task1.isCanceled()){
                                Log.d("MY_FIREBASE_ERROR", "Failed to delete message entry for user");
                            }
                        }
                    });

                }
                else if(task.isCanceled()){
                    Log.d("MY_FIREBASE_ERROR", "Unable to get msg sub-collection");
                }
            }
        });
    }
    public Task<Void> deleteAuth(){
        return getCurrentUser().delete();
    }

    public Task<Uri> uploadUserImage(String imageName, ByteArrayOutputStream image){
        if(imageName.equals(CONSTANTS.DEFAULT)){
            return storage.getReference(CONSTANTS.COLLECTIONS.DEFAULT_IMAGES).getDownloadUrl();
        }
        StorageReference storageRef = storage.getReference(CONSTANTS.COLLECTIONS.PROFILE_IMAGES+ "/" + imageName);
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

    public void getPublicKey(){}

    public void getMessages(){}

    public String getUID(){ return getCurrentUser().getUid();}

    //This would connect to my server to signal availability of user
    public void initVoIP(){

    }

    public interface AuthInterface{
        void onAuthSuccessful(boolean success);
    }


}
