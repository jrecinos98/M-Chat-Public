package com.mchat.recinos.Util;


import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

public abstract class CONSTANTS {

    public final static int BITMAP_LIMIT = 1500000;
    public final static String DEFAULT = "default";
    public final static String DEFAULT_KEY_ALIAS = "default_keys";
    public final static String KEYSTORE_PROVIDER= "AndroidKeyStore";

    public final static String LAST_CHAT_ID = "LAST_CHAT_ID";
    public final static String LAST_MSG_ID = "LAST_CHAT_ID";
    public final static String CHAT_LIST_EMPTY = "IS_CHAT_EMPTY_";
    public final static String CALL_LIST_EMPTY = "IS_CALL_EMPTY_";

    public final static class LOCATIONS{
        public final static String INTERNAL = "0";
        public final static String EXTERNAL = "0";
    }

    public final static class MESSAGE_DATA_TYPES{
        public final static int TEXT = 0;
        public final static int IMAGE = 1;
        public final static int AUDIO = 2;
        public final static int GIF = 3;
    }

    public final static class PROTO_MESSAGE_DATA_TYPES{
        public final static int AUTH = 0;
        public final static int MESSAGE = 1;
        public final static int ACK = 2;
    }
    public final static class MEDIA_TYPE{
        public final static int BITMAP = 0;
    }
    // Declaring the static map
    public static Map<String, Bitmap.CompressFormat> BITMAP_FORMATS;
    static{
        BITMAP_FORMATS = new HashMap<>();
        BITMAP_FORMATS.put("jpg", Bitmap.CompressFormat.JPEG);
        BITMAP_FORMATS.put("png", Bitmap.CompressFormat.PNG);
        BITMAP_FORMATS.put("webp", Bitmap.CompressFormat.WEBP);
    }

    public final static class MIME_TYPES{
        public final static String IMAGE_PATH = "image/";
        public final static String JPG = "jpg";
    }

    public enum KEY{
        PUBLIC,
        PRIVATE
    }

    public final static class COLLECTIONS{
        public final static String USERS = "USERS";
        public final static String PUBLIC_DATA= "PUBLIC_DATA";

        public final static String MESSAGES = "ENCRYPT_MSG";
        public final static String SENDERS= "SENDERS";

        public final static String SERVER = "SERVER";
        public final static String DEFAULT_IMAGES = "DEFAULT";
        public final static String PROFILE_IMAGES = "PROFILE_IMAGES";
    }
    //Username is name of document on firestore associated with the data so not stored.
    public final static class PUBLIC_DATA_ENTRY{
        public final static String KEY= "public_key";
        public final static String NAME= "name";
        public final static String PHOTO_URL= "photo_url";
        public final static String UID = "uid";
        public final static String EMAIL = "email";
        public final static String DEFAULT_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/m-chat-be7c0.appspot.com/o/DEFAULTS%2Fdefault_avatar.webp?alt=media&token=17e7cff4-a4e0-4d6a-85a8-7d9632b6f590";
    }
    //UID is the name of document on firestore associated with the data so not stored.
    public final static class USERS_ENTRY{
        public final static String USERNAME = "user_name";
        public final static String PHONE_NUMBER = "phone_num";
    }

    public final static class CONTACT_FIELDS{
        public final static String NAME = "name";
        public final static String USERNAME = "username";
        public final static String IMAGE= "image";
        public final static String PUBLIC_KEY = "public_key";
        public final static String UID = "uid";
    }

    public final static class INTENT_ID{
        public final static String CONTACT_INFO = "CONTACT_INFO";
        public final static String CHAT_ID = "CHAT_ID";
    }
    public final static class RESULT_ID{
        public final static int GOOGLE_LOG_IN=0;
        public final static int  IMAGE = 1;
        public final static int  PHOTO = 2;
        public final static int  AUDIO = 3;
    }
    public final static class FRAG_TAGS{
        public final static String LOG_IN_TAG = "log_in";
        public final static String SIGN_UP_TAG = "sign_up";
    }
    public final static class PLACEHOLDER{
        public final static String IMAGE_TEXT = "Image";
    }
    public final static class PERMISSION{
        //These are the same since they ar ein the same permission group
        public final static int READ_EXTERNAL = 0;
        public final static int WRITE_EXTERNAL = 0;

        public final static int PHOTO = 1;
        public final static int SAVE_EXTERNAL = 2;

    }
    public final static class INTERNAL_DIRECTORY{
        public final static String MESSAGE_IMAGES = "msg_img";
        public final static String CONTACT_IMAGES = "contact_img";
    }
}
