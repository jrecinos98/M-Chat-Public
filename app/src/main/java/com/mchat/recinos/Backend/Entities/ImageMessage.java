package com.mchat.recinos.Backend.Entities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.google.protobuf.ByteString;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.CONSTANTS;
import com.mchat.recinos.Util.Util;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import Protobuf.ProtoMessage;

//TODO update entity to be able to hold multiple URI for when sending multiple files. Also correct the protobuf after this is done

public class ImageMessage extends Message implements Serializable {
    //Only need one field
    @Ignore
    private Bitmap image;

    //When all is known on creation
    public ImageMessage(int cid, Bitmap image, String uri, long time,  boolean belongToUser){
        super(cid, uri,  CONSTANTS.PLACEHOLDER.IMAGE_TEXT, time, CONSTANTS.MESSAGE_DATA_TYPES.IMAGE, belongToUser);
        this.image = image;
    }
    //CID and URI are not known (will be set later)
    public ImageMessage(Bitmap image, long time,  boolean belongToUser){
        super("", CONSTANTS.PLACEHOLDER.IMAGE_TEXT, time, CONSTANTS.MESSAGE_DATA_TYPES.IMAGE, belongToUser);
        this.image = image;
    }
    //URI is not known (will be set later)
    public ImageMessage(int cid, Bitmap image, long time,  boolean belongToUser){
        super(cid, "",CONSTANTS.PLACEHOLDER.IMAGE_TEXT, time, CONSTANTS.MESSAGE_DATA_TYPES.IMAGE, belongToUser);
        this.image = image;
    }
    public ImageMessage(Message message){
        super(message);
    }
    public ImageMessage(){
        super();
    }

    public void setImage(Bitmap image){
        this.image = image;
    }
    public void setUri(String uri){
        super.setData(uri);
    }
    public android.graphics.Bitmap getImage(){
        return this.image;
    }
    public byte[] getImageBytes(){ return  BitmapTransform.SerialBitmap.toArray(this.image);}
    public String getUri(){return super.getData();}
    public String getPreview() {return CONSTANTS.PLACEHOLDER.IMAGE_TEXT;}
    public String getExtension(){
        return Util.getExtension(getUri());
    }

    public ProtoMessage.Payload toProtoBufPayload(){
        final Bitmap.CompressFormat compressionType = CONSTANTS.BITMAP_FORMATS.get(getExtension());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //Use the file extension to determine the type of compression to use
        image.compress(compressionType, 60, out);
        ProtoMessage.Payload payload = ProtoMessage.Payload.newBuilder()
                .setOpCode(CONSTANTS.MESSAGE_DATA_TYPES.IMAGE)
                .setData(ByteString.copyFrom(getImageBytes()))
                .setMimeType(getExtension())
                .build();
        return payload;
    }
}
