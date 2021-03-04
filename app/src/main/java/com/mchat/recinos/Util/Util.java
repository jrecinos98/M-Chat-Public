package com.mchat.recinos.Util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.Blob;
import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Backend.Entities.Messages.Message;
import com.mchat.recinos.Backend.Entities.User;
import com.mchat.recinos.MyApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import Protobuf.ProtoMessage;


public class Util {
    /**
     * @param filename Name of a valid file.
     * @return Returns the file extension as a string.
     */
    public static String getExtension(String filename){
        String extension = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i+1);
        }
        return extension;
    }

    /**
     * Mainly used for debugging and mesuring completion time from tasks.
     * @param millis Converts a date given in milliseconds to a human readable format.
     * @return Returns the Date in a human readable string.
     */
    public static String formatDate(long millis){
        // Ex: Tue
        String pattern = "EEE MM/dd/yyy kk:mm:ss.SSSS z";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
        return formatter.format(new Date(millis));
    }

    /**
     * Creates map that gets uploaded to Firebase to the USER collection (document name is UID).
     * It links to a username which can be used to acquire the users public data.
     * @param username The username chosen by the User.
     * @return A map containing the username as its only field.
     */
    public static Map<String, Object> makeUserObj(String username){
        Map<String, Object> userObj = new HashMap<>();
        userObj.put(Constants.USERS_ENTRY.USERNAME, username);
        return userObj;
    }

    /**
     * Creates a Map that contains all of a User's public data.
     * @param uid User's Unique ID
     * @param name Name of the User.
     * @param email Email of the user.
     * @param public_key The User's public key. It must contain a private key counterpart which is kept local on the device.
     * @param photoURL URL of the user's profile image.
     * @return A Map containing all the relevant information
     */
    public static Map<String, Object> makePublicUserData(String uid, String name, String email, byte[] public_key, String photoURL){
        Blob p_key = Blob.fromBytes(public_key);
        Map<String, Object> nameObj = new HashMap<>();
        nameObj.put(Constants.PUBLIC_DATA_ENTRY.UID, uid);
        nameObj.put(Constants.PUBLIC_DATA_ENTRY.NAME, name);
        nameObj.put(Constants.PUBLIC_DATA_ENTRY.EMAIL, email);
        nameObj.put(Constants.PUBLIC_DATA_ENTRY.KEY, p_key);
        nameObj.put(Constants.PUBLIC_DATA_ENTRY.PHOTO_URL, photoURL);
        return nameObj;
    }

    /**
     * Creates a new Contact from a Map and a username.
     * @param data Map containing all the user data.
     * @param username Username of the friend
     * @return A contact object that contains all necessary info.
     */
    public static Contact createFriendUser(Map<String, Object> data, String username){
        Blob key = (Blob) data.get(Constants.PUBLIC_DATA_ENTRY.KEY);
        //Extract key if not null or use placeholder key.
        String public_key = key != null?  new String(key.toBytes()) : "public_key";
        return new Contact(new User(data, username), public_key);
    }

    /**
     * @return The time in milliseconds since epoch.
     */
    public static long getTime(){
        return Calendar.getInstance().getTime().getTime();
    }

    public static String generateFileNameWithExtension( String name, String extension){
        //TODO check user preference to determine if stored on internal or external storage and add accordingly
        //Use the time and a random number to name the file
        int random = (int) (100*Math.random());
        //First char is for location, the next 13 chars is the time, the last two chars before the period are the random numbers]
        return Constants.LOCATIONS.INTERNAL+ name + random+ "." +extension ;
    }
    public static String generateFileNameWithoutExtension( String name){
        //TODO check user preference to determine if stored on internal or external storage and add accordingly
        //Use the time and a random number to name the file
        int random = (int) (100*Math.random());
        //First char is for location, the next 13 chars is the time, the last two chars before the period are the random numbers]
        return Constants.LOCATIONS.INTERNAL+ name + random;
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
        int id = sharedPreferences.getInt(Constants.LAST_CHAT_ID+ MyApplication.getClient().getUserID(), 0) + 1;
        if (id == Integer.MAX_VALUE) {
            id = 0;
        }
        sharedPreferences.edit().putInt(Constants.LAST_CHAT_ID+ MyApplication.getClient().getUserID(), id).apply();
        return id;
    }
    public static long newMessageID(Context context, String cid) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long id = sharedPreferences.getLong(Constants.LAST_MSG_ID+ MyApplication.getClient().getUserID()+cid, 0) + 1;
        if (id == Integer.MAX_VALUE) {
            id = 0;
        }
        sharedPreferences.edit().putLong(Constants.LAST_MSG_ID+ MyApplication.getClient().getUserID()+cid, id).apply();
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

    public static boolean isPermissionGranted(Activity activity, String permission , int id){
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation? Do so here.
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Toast.makeText(activity, "Storage Permission is needed to access images", Toast.LENGTH_LONG).show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity, new String[]{permission}, id);
            }
            return false;
        }
        return true;

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
                .setType(Constants.PROTO_MESSAGE_DATA_TYPES.MESSAGE)
                .setMsgId(message.getId())
                .setDestUid(destID)
                .setMessage(protoPayload)
                .build();
        return protoMessage;
    }

    private static void removeView(View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if(parent != null) {
            parent.removeView(view);
        }
    }

}
