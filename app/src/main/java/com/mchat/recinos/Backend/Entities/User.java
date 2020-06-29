package com.mchat.recinos.Backend.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.auth.FirebaseUser;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.Util.CONSTANTS;

@Entity(tableName = "user")
public class User {
    @NonNull
    @PrimaryKey
    @ColumnInfo (name = "uid")
    private String userID = "primary";

    @ColumnInfo (name = CONSTANTS.CONTACT_FIELDS.NAME)
    private String name;

    @ColumnInfo (name = CONSTANTS.CONTACT_FIELDS.USERNAME)
    private String username;

    @ColumnInfo (name = CONSTANTS.CONTACT_FIELDS.IMAGE)
    private String photoURL;

    public static User getLogInUser(){
        if(CloudDatabase.getInstance().getCurrentUser() != null)
            return new User(CloudDatabase.getInstance().getCurrentUser());
        else
            return null;
    }
    public User(){
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

    public String getName(){return name;}
    public void setName(String name){ this.name =name;}
    public String getUserID() {return userID;}
    public void setUserID(String uid){ userID = uid;}
    public String getPhotoURL(){return photoURL;}
    public void setPhotoURL(String url){ photoURL = url;}
    public String getUsername(){return username;}
    public void setUsername(String username){this.username = username;}
}
