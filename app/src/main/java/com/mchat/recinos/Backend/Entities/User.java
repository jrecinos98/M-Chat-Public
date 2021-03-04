package com.mchat.recinos.Backend.Entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.auth.FirebaseUser;
import com.mchat.recinos.Backend.Authentication;
import com.mchat.recinos.Util.Constants;

import java.util.Map;

@Entity(tableName = "user")
public class User {
    @NonNull
    @PrimaryKey
    @ColumnInfo (name = "uid")
    private String userID = "primary";

    @ColumnInfo (name = Constants.CONTACT_FIELDS.NAME)
    private String name;

    @ColumnInfo (name = Constants.CONTACT_FIELDS.USERNAME)
    private String username;

    @ColumnInfo (name = Constants.CONTACT_FIELDS.IMAGE)
    private String photoURL;

    public static User getLogInUser(){
        FirebaseUser firebaseUser= Authentication.getInstance().getCurrentUser();
        if( firebaseUser != null)
            return new User(firebaseUser);
        else
            return null;
    }
    public User(){
    }

    /**
     * Creates a User object from Map data and a username.
     * @param data
     * @param username
     */
    public User(Map<String, Object> data, String username){
        this.userID = (String) data.get(Constants.PUBLIC_DATA_ENTRY.UID);
        this.name = (String) data.get(Constants.PUBLIC_DATA_ENTRY.NAME);
        this.photoURL = (String) data.get(Constants.PUBLIC_DATA_ENTRY.PHOTO_URL);
        this.username = username;
    }
    public User(FirebaseUser user) {
        if( user != null) {
            username = user.getDisplayName();
            userID = user.getUid();
            photoURL = user.getPhotoUrl().toString();
            name = user.getProviderData().get(0).getDisplayName();
        }
    }
    public User(@NonNull String uid, String n, String u, String pURL){
        userID=uid;
        name= n;
        username = u;
        photoURL = pURL;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof User) {
            User comp = (User) obj;
            return  this.userID.equals(comp.getUserID()) &&
                    this.username.equals(comp.getUsername()) &&
                    this.name.equals(comp.getName()) &&
                    this.photoURL.equals(comp.getPhotoURL());
        }
        return false;
    }

    public String getName(){return name;}
    public void setName(String name){ this.name =name;}
    public String getUserID() {return userID;}
    public void setUserID(String uid){ userID = uid;}
    public String getPhotoURL(){return photoURL;}
    public void setPhotoURL(String url){ photoURL = url;}
    public String getUsername(){return username;}
    public void setUsername(String username){this.username = username;}
}
