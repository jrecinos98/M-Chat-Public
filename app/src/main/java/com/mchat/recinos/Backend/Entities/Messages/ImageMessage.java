package com.mchat.recinos.Backend.Entities.Messages;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.room.Ignore;

import com.google.protobuf.ByteString;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.Constants;
import com.mchat.recinos.Util.Util;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import Protobuf.ProtoMessage;

//TODO update entity to be able to hold multiple URI for when sending multiple files. Also correct the protobuf after this is done

public class ImageMessage extends Message implements Serializable {
    //Only need one field
    @Ignore
    private Bitmap image;
    @Ignore
    private Uri uri;

    //When all persistent data is known on creation
    public ImageMessage(int cid, Bitmap image, String filename, long time,  boolean belongToUser){
        super(cid, filename,  Constants.PLACEHOLDER.IMAGE_TEXT, time, Constants.MESSAGE_DATA_TYPES.IMAGE, belongToUser);
        this.image = image;
    }
    //CID and URI are not known (will be set later)
    public ImageMessage(Bitmap image, long time,  boolean belongToUser){
        super("", Constants.PLACEHOLDER.IMAGE_TEXT, time, Constants.MESSAGE_DATA_TYPES.IMAGE, belongToUser);
        this.image = image;
    }
    //Complete Constructor
    public ImageMessage(int cid, Bitmap image, Uri uri, String filename, long time,  boolean belongToUser){
        super(cid, filename, Constants.PLACEHOLDER.IMAGE_TEXT, time, Constants.MESSAGE_DATA_TYPES.IMAGE, belongToUser);
        this.image = image;
        this.uri = uri;
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
    public void setFileName(String uri){
        super.setData(uri);
    }
    public android.graphics.Bitmap getImage(){
        return this.image;
    }
    public Uri getUri(){return uri;}
    public byte[] getImageBytes(){ return  BitmapTransform.SerialBitmap.toArray(this.image);}
    public String getFileName(){return super.getData();}
    public String getPreview() {return Constants.PLACEHOLDER.IMAGE_TEXT;}
    public String getExtension(){
        return Util.getExtension(getFileName());
    }

    public ProtoMessage.Payload toProtoBufPayload(){
        final Bitmap.CompressFormat compressionType = Constants.BITMAP_FORMATS.get(getExtension());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //Use the file extension to determine the type of compression to use
        image.compress(compressionType, 60, out);
        ProtoMessage.Payload payload = ProtoMessage.Payload.newBuilder()
                .setOpCode(Constants.MESSAGE_DATA_TYPES.IMAGE)
                .setData(ByteString.copyFrom(getImageBytes()))
                .setMimeType(getExtension())
                .build();
        return payload;
    }
}
