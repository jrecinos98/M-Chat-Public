package com.mchat.recinos.Backend.Entities;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.security.PublicKey;

//TODO create a Contact table or store the names in shared prefs.
@Entity (tableName = "chat")
public class Chat implements Serializable {
    @NonNull
    @PrimaryKey//Maybe has to be a foreign key.
    @ColumnInfo (name= "cid")
    private int cid;

    @Embedded
    private Contact contact;

    private String preview;

    //This variable helps to compare chats and order by ones that have been updated more recently
    @ColumnInfo(name = "last_update")
    private long lastUpdate;

    @ColumnInfo(name = "unread_count" )
    private int unreadCount;

    public Chat(){
    }

    @Ignore
    public Chat( int id, Contact contact, String preview, long update_time){
        this.cid = id;
        this.contact= contact;
        this.preview=preview;
        this.lastUpdate = update_time;
        unreadCount =0;
    }
    //Chat setters
    public void setCid(@NonNull int id){this.cid = id;}
    public void setContact(Contact contact){this.contact = contact;}
    public void setPreview(String p){
        this.preview = p;
    }
    public void setLastUpdate(long time){ this.lastUpdate = time;}
    public void setUnreadCount(int count){ this.unreadCount = count;}

    //Chat getters
    public int getCid() {
        return cid;
    }
    public String getFriendUid() {return contact.getUID();}
    public Contact getContact() {
        return contact;
    }
    public String getPreview() {
        return preview;
    }
    public long getLastUpdate() {return  lastUpdate;}
    public int getUnreadCount(){ return unreadCount;}

    //Contact getters
    public String getUID(){
        return contact.getUID();
    }
    public String getTitle(){
        return contact.getName();
    }
    public String getUserName(){
        return contact.getUserName();
    }
    public Bitmap getImageBitmap(){
        return contact.getImageBitmap();
    }
    public PublicKey getPublicKey(){
        return contact.getPublicKey();
    }

    public void resetUnread(){ unreadCount=0;}
    public void increaseUnread(){
        unreadCount +=1;
    }
}
