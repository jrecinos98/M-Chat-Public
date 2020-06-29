package com.mchat.recinos.Util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.Blob;
import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Backend.Entities.Message;
import com.mchat.recinos.Backend.Entities.User;
import com.mchat.recinos.MyApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Protobuf.ProtoMessage;


public class Util {

    public static String generateFileNameWithExtension( String name, String extension){
        //TODO check user preference to determine if stored on internal or external storage and add accordingly
        //Use the time and a random number to name the file
        int random = (int) (100*Math.random());
        //First char is for location, the next 13 chars is the time, the last two chars before the period are the random numbers]
        return CONSTANTS.LOCATIONS.INTERNAL+ name + random+ "." +extension ;
    }
    public static String generateFileNameWithoutExtension( String name){
        //TODO check user preference to determine if stored on internal or external storage and add accordingly
        //Use the time and a random number to name the file
        int random = (int) (100*Math.random());
        //First char is for location, the next 13 chars is the time, the last two chars before the period are the random numbers]
        return CONSTANTS.LOCATIONS.INTERNAL+ name + random;
    }
    public static String getExtension(String filename){
        String extension = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i+1);
        }
        return extension;
    }
    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    public static int newChatID(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int id = sharedPreferences.getInt(CONSTANTS.LAST_CHAT_ID+ MyApplication.getClient().getUserID(), 0) + 1;
        if (id == Integer.MAX_VALUE) {
            id = 0;
        }
        sharedPreferences.edit().putInt(CONSTANTS.LAST_CHAT_ID+ MyApplication.getClient().getUserID(), id).apply();
        return id;
    }
    public static long newMessageID(Context context, String cid) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long id = sharedPreferences.getLong(CONSTANTS.LAST_MSG_ID+ MyApplication.getClient().getUserID()+cid, 0) + 1;
        if (id == Integer.MAX_VALUE) {
            id = 0;
        }
        sharedPreferences.edit().putLong(CONSTANTS.LAST_MSG_ID+ MyApplication.getClient().getUserID()+cid, id).apply();
        return id;
    }

    public static String getGoogleImageURL(GoogleSignInAccount user){
        String photoURL = "";
        if(user != null) {
            if (user.getPhotoUrl() != null) {
                //Default quality is trash
                String low_res ="s96-c";
                String higher_res="s550-c";
                photoURL = user.getPhotoUrl().toString().replace(low_res,higher_res);
            }
        }
        return  photoURL;
    }
    public static long getTime(){
        return Calendar.getInstance().getTime().getTime();
    }
    public static String formatDate(long millis){
        // Ex: Tue
        String pattern = "EEE MM/dd/yyy kk:mm:ss.SSSS z";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date(millis));
    }



    public static ProtoMessage.Message makeProtoMessage(String destID, Message message){
        //Turn the message to payload and add the additional data like the destination and encryption key
        ProtoMessage.Payload protoPayload = message.toProtoBufPayload()
                .toBuilder()
                //.setKey()
                .build();
        //Create the Message and set all necessary fields.
        ProtoMessage.Message protoMessage= ProtoMessage.Message.newBuilder()
                //TODO getId() always returns 0 (not returning the id assigned by ROOM)
                .setType(CONSTANTS.PROTO_MESSAGE_DATA_TYPES.MESSAGE)
                .setMsgId(message.getId())
                .setDestUid(destID)
                .setMessage(protoPayload)
                .build();
        return protoMessage;
    }

    public static Map<String, Object> makeUserObj(String username){
        Map<String, Object> userObj = new HashMap<>();
        userObj.put(CONSTANTS.USERS_ENTRY.USERNAME, username);
        return userObj;
    }
    public static Map<String, Object> makePublicUserData(String uid, String name, String email, byte[] public_key, String photoURL){
        Blob p_key = Blob.fromBytes(public_key);
        Map<String, Object> nameObj = new HashMap<>();
        nameObj.put(CONSTANTS.PUBLIC_DATA_ENTRY.UID, uid);
        nameObj.put(CONSTANTS.PUBLIC_DATA_ENTRY.NAME, name);
        nameObj.put(CONSTANTS.PUBLIC_DATA_ENTRY.EMAIL, email);
        nameObj.put(CONSTANTS.PUBLIC_DATA_ENTRY.KEY, p_key);
        nameObj.put(CONSTANTS.PUBLIC_DATA_ENTRY.PHOTO_URL, photoURL);
        return nameObj;
    }
    public static Contact createFriendUser(Map<String, Object> data, String username){
        String uid = (String) data.get(CONSTANTS.PUBLIC_DATA_ENTRY.UID);
        String name = (String) data.get(CONSTANTS.PUBLIC_DATA_ENTRY.NAME);
        String photoURL = (String) data.get(CONSTANTS.PUBLIC_DATA_ENTRY.PHOTO_URL);
        Blob key = (Blob) data.get(CONSTANTS.PUBLIC_DATA_ENTRY.KEY);
        String public_key ="public_key";
        if(key != null){
            public_key = new String(key.toBytes());
        }
        User temp= new User(uid, name, username, photoURL);
        return new Contact(temp, public_key);
    }
    private static void removeView(View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if(parent != null) {
            parent.removeView(view);
        }
    }

}
