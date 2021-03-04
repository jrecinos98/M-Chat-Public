package com.mchat.recinos.Backend.Entities;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.Constants;
import com.mchat.recinos.Util.Encryption;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Arrays;

@Entity (tableName = "contacts")
public final class Contact implements Serializable, Parcelable {
    @ColumnInfo(name="uid")
    private String UID;

    @ColumnInfo (name="contact_name")
    private String name;

    @ColumnInfo(name ="user_name")
    private String userName;

    private String public_key;

    //Has to be stored as a blob in database. (ONLY SAVE CONTACT IMAGES IN DATABASE)
    @ColumnInfo(name = "image",typeAffinity = ColumnInfo.BLOB)
    private byte[] image;

    @ColumnInfo(name = "photo_url")
    private String photo_URL;

    public Contact(){}

    //Used when retrieving a friend from database. Fetching Image is asynchronous so image gets set later.
    public Contact(User user, String key){
        UID = user.getUserID();
        name = user.getName();
        userName = user.getUsername();
        public_key = key;
        photo_URL= user.getPhotoURL();
        image= null;
    }
    public Contact(Contact c){
        UID = c.getUID();
        name = c.getName();
        userName = c.getUserName();
        public_key = c.getPublic_key();
        photo_URL = c.getPhoto_URL();
        image= c.getImage();
    }
    public void setImageBytes(byte[] src){
        if(src != null) {
            image = Arrays.copyOf(src, src.length);
        }
    }
    @Ignore
    public User getUser(){
        return new User(this.getUID(), this.getName(), this.getUserName(), this.getPhoto_URL());
    }

    public Contact(Parcel savedState){
        if (savedState != null) {
            Bundle saved = savedState.readBundle(getClass().getClassLoader());
            if(saved != null) {
                UID = saved.getString(Constants.CONTACT_FIELDS.UID);
                name = saved.getString(Constants.CONTACT_FIELDS.NAME);
                userName = saved.getString(Constants.CONTACT_FIELDS.USERNAME);
                public_key = saved.getString(Constants.CONTACT_FIELDS.PUBLIC_KEY);
                image = saved.getByteArray(Constants.CONTACT_FIELDS.IMAGE);
            }
        }
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle data = new Bundle();
        data.putSerializable(Constants.CONTACT_FIELDS.UID, getUID());
        data.putByteArray(Constants.CONTACT_FIELDS.IMAGE, getImage());
        data.putSerializable(Constants.CONTACT_FIELDS.NAME, getName());
        data.putSerializable(Constants.CONTACT_FIELDS.USERNAME, getUserName());
        data.putSerializable(Constants.CONTACT_FIELDS.PUBLIC_KEY, getPublic_key());
        dest.writeBundle(data);
    }
    public static final Parcelable.Creator<Contact> CREATOR
            = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact [size];
        }
    };

    public byte[] getImage(){return image;}
    public android.graphics.Bitmap getImageBitmap(){
        if(isImageInit())
            return BitmapTransform.SerialBitmap.toBitmap(image);
        return null;
    }
    public boolean isImageInit(){
        return image != null && image.length != 0;
    }
    public String getUID(){return UID;}
    public String getName(){
        return name;
    }
    public String getUserName() { return userName;}
    public String getPublic_key(){return public_key;}
    @Ignore
    public PublicKey getPublicKey(){return Encryption.publicKeyFromBytes(public_key.getBytes());}
    public String getPhoto_URL(){return  photo_URL;}

    public void setUID(String uid){ this.UID = uid;}
    public void setName(String name){ this.name = name;}
    public void setUserName(String userName){ this.userName = userName;}
    public void setPublic_key(String public_key){ this.public_key = public_key;}
    public void setImage(byte[] image){ this.image = image;}
    public void setPhoto_URL(String photo_URL){ this.photo_URL = photo_URL;}


}
