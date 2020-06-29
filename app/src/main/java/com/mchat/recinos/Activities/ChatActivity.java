package com.mchat.recinos.Activities;


import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;

import com.mchat.recinos.Adapters.ListAdapters.MessageListAdapter;
import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Backend.Entities.ImageMessage;
import com.mchat.recinos.Backend.Entities.Message;
import com.mchat.recinos.Backend.Entities.TextMessage;
import com.mchat.recinos.Backend.LocalDatabase;
import com.mchat.recinos.Backend.ViewModels.MessageViewModel;
import com.mchat.recinos.MyApplication;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.CONSTANTS;
import com.mchat.recinos.Util.IO;
import com.mchat.recinos.Util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private EditText editText;
    private ImageButton sendButton;
    private ListView listView;
    private ImageButton sendImage;
    private ImageButton takePhoto;
    private View pendingSave;
    //TODO keeping this can cause race condition if photos are sent consecutively (Not likely but can happen)
    private Uri photo_uri;

    private Contact contact;
    private int chatID;

    public static MessageListAdapter mAdapter;

    private MessageViewModel messageViewModel;

    public SendMessageListener sendMessageListener;

    public static List<Message> messages;
    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        //MyApplication.broadcastManager.unregisterReceiver(messageReceiver);
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            //bar.setTitle(title);
            bar.setDisplayShowTitleEnabled(false);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }

        editText = findViewById(R.id.editText);
        sendButton = findViewById(R.id.send_button);
        sendMessageListener= new SendMessageListener(editText);
        sendButton.setOnClickListener(sendMessageListener);
        sendImage = findViewById(R.id.send_image);
        sendImage.setOnClickListener(new IntentLauncherListener(CONSTANTS.RESULT_ID.IMAGE));
        takePhoto = findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(new IntentLauncherListener(CONSTANTS.RESULT_ID.PHOTO));
        listView = findViewById(R.id.message_list);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("LONG_CLICK_LISTEN", "Position: "+ position);
                Message message =(Message) mAdapter.getItem(position);
                if(message.getType() == CONSTANTS.MESSAGE_DATA_TYPES.TEXT){
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("mchat_text"+ (int) (100*Math.random()), message.getData());
                    if(clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getApplicationContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(message.getType() == CONSTANTS.MESSAGE_DATA_TYPES.IMAGE){
                    pendingSave = view;
                    //Check the app has permission to write to external storage
                    if (isPermissionGranted(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, CONSTANTS.PERMISSION.SAVE_EXTERNAL)
                            && isPermissionGranted(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, CONSTANTS.PERMISSION.SAVE_EXTERNAL)) {

                        MessageListAdapter.ImageMessageViewHolder imageHolder = (MessageListAdapter.ImageMessageViewHolder) view.getTag();
                        Bitmap image = ((BitmapDrawable)imageHolder.image.getDrawable()).getBitmap();
                        IO.saveToPublicExternalStorage(getApplicationContext(), CONSTANTS.MIME_TYPES.IMAGE_PATH, imageHolder.extension, image, String.valueOf(Util.getTime()));
                        Toast.makeText(getApplicationContext(), "Saved image to external storage", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }

        });
        //If not null then the activity is being resumed
        if(savedInstanceState != null){
        }
        else {
           contact = (Contact) getIntent().getExtras().getSerializable(CONSTANTS.INTENT_ID.CONTACT_INFO);
           chatID = getIntent().getExtras().getInt(CONSTANTS.INTENT_ID.CHAT_ID);
           if(contact != null){
               String title = contact.getUserName();
               TextView t_text= findViewById(R.id.chat_title);
               t_text.setText(title);
               /* Use if decide to have image up on the tool bar*/
               //imageView.setBackground(BitmapTransform.roundedBitmapDrawable(getResources(), contact.getImageBytes()););
               mAdapter = new MessageListAdapter(getApplicationContext(), contact.getImageBitmap(), contact.getUID());
           }
        }
        listView.setAdapter(mAdapter);
        //Will persist configuration changes. NOTE: We pass the chat id to the viewholder so that it only notifies of relevant messages
        messageViewModel = ViewModelProviders.of(this, new MessageViewModel.MessageViewModelFactory(this.getApplication(), this, chatID)).get(MessageViewModel.class);
        //Here we add observer. When a message for this conversation is added this callbakc will trigger
        messageViewModel.getCurrentChatMessages().observe(this, (m)-> {
            messages = m;
            mAdapter.setMessages(messages);
            // Update the cached copy of the messages in the adapter.
            mAdapter.notifyDataSetChanged();
            //Scroll to bottom
            listView.smoothScrollToPosition(mAdapter.getCount());
        });
    }
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final Uri imageUri = reqCode == CONSTANTS.RESULT_ID.IMAGE? data.getData() : photo_uri;
            long time = Util.getTime();
            String fileName = Util.generateFileNameWithExtension(String.valueOf(time) , Util.getMimeType(getApplicationContext(),imageUri));
            final Bitmap.CompressFormat compressionType = CONSTANTS.BITMAP_FORMATS.get(Util.getExtension(fileName));
            Bitmap internal = null;
            Bitmap network = null;
            if(reqCode == CONSTANTS.RESULT_ID.IMAGE || reqCode == CONSTANTS.RESULT_ID.PHOTO){
                try {
                    //Read image from stream
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    // decode image size (decode metadata only, not the whole image)
                    BitmapFactory.Options options = BitmapTransform.readMetaData(imageStream);
                    String imageType = options.outMimeType;
                    options.inSampleSize = BitmapTransform.calculateInSampleSize(options, 600, 600);
                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;
                    imageStream = getContentResolver().openInputStream(imageUri);
                    // decode full image
                    internal = BitmapFactory.decodeStream(imageStream, null, options);

                    /*
                     * Necessary to redo for network one to reduce size
                     */
                    imageStream= getContentResolver().openInputStream(imageUri);
                    // decode image size (decode metadata only, not the whole image)
                    options = BitmapTransform.readMetaData(imageStream);
                    options.inSampleSize= BitmapTransform.calculateInSampleSize(options, 350, 350);
                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;
                    imageStream = getContentResolver().openInputStream(imageUri);
                    // decode full image
                    network = BitmapFactory.decodeStream(imageStream, null, options);

                }catch(IOException e){
                    e.printStackTrace();
                }

            }
            final Bitmap tempInternal= internal;
            final Bitmap tempNetwork = network;
            //TODO show a temporary loading image
            LocalDatabase.IOWriteExectutor.execute(()->{
                //Afterwards rotate the image if necessary
                Bitmap internalBitmap = BitmapTransform.correctBitmap(getApplicationContext(), tempInternal, imageUri, tempInternal.getWidth());

                ImageMessage new_msg = new ImageMessage( chatID, internalBitmap, fileName, time, true );
                //Add message to database
                saveMessage(new_msg);
            });
            /*
             Sends a message over the network.
            */
            LocalDatabase.IOWriteExectutor.execute(()->{
                //Afterwards rotate the image if necessary
                Bitmap netBitmap= BitmapTransform.correctBitmap(getApplicationContext(), tempNetwork, imageUri, tempNetwork.getWidth() );
                //Compress the bitmap quality to reduce size and send to the network
                //Bitmap networkBitmap = BitmapTransform.scaleDownBitmap(correctedBitmap, 300, getApplicationContext());
                Bitmap networkBitmap = BitmapTransform.compressBitmap(netBitmap, compressionType, 60);
                ImageMessage netMessage = new ImageMessage(chatID, networkBitmap, fileName, time, true);
                //Send message
                MyApplication.getClient().sendMessage(contact.getUID(), netMessage);
            });
        }else {
            Toast.makeText(this, "No Image Selected",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case CONSTANTS.PERMISSION.READ_EXTERNAL:
                    launchFilePicker();
                    return;
                case CONSTANTS.PERMISSION.PHOTO: {
                    dispatchTakePictureIntent();
                    return;
                }
                case CONSTANTS.PERMISSION.SAVE_EXTERNAL: {
                    if(pendingSave != null) {
                        MessageListAdapter.ImageMessageViewHolder imageHolder = (MessageListAdapter.ImageMessageViewHolder) pendingSave.getTag();
                        Bitmap image = ((BitmapDrawable) imageHolder.image.getDrawable()).getBitmap();
                        IO.saveToPublicExternalStorage(getApplicationContext(), CONSTANTS.MIME_TYPES.IMAGE_PATH, imageHolder.extension, image, String.valueOf(Util.getTime()));
                        Toast.makeText(getApplicationContext(), "Saved image to external storage", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.delete_convo){
            messageViewModel.deleteChat(chatID, this.messages);
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed(){
        Client.CURRENT_CHAT = 0;
        super.onBackPressed();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onStart(){
        super.onStart();
    }
    private boolean isPermissionGranted(Activity activity, String permission , int id){
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Toast.makeText(activity, "Storage Permission is needed to access images", Toast.LENGTH_SHORT).show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity, new String[]{permission}, id);
            }
            return false;
        }
        return true;

    }
    /**
     * Stores messages on Local DB. This in turn triggers the observers and the UI is refreshed.
     * @param message message to save.
     */
    public void saveMessage(Message message){
        if(message.getType() == CONSTANTS.MESSAGE_DATA_TYPES.TEXT) {
            //Inserting will trigger UI update due to observer
            messageViewModel.insert(message);
            //Need to update the chat preview after every message
            messageViewModel.updateChatPreview(contact.getUID(), message.getPreview(), Util.getTime(), 0);
        }else{
            ImageMessage imageMessage = (ImageMessage) message;
            //Compress the image by 25%
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //Use the file extension to determine the type of compression to use
            imageMessage.getImage().compress(CONSTANTS.BITMAP_FORMATS.get(imageMessage.getExtension()), 75, out);
            //Update the image message
            imageMessage.setImage(BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray())));

            //Add to database
            messageViewModel.insert(imageMessage);
            messageViewModel.updateChatPreview(contact.getUID(), message.getPreview(), Util.getTime(), 0);
        }
    }
    public void launchFilePicker(){
        Log.d("LAUNCH", "HUH");
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");

        //Prevents Exposed URI Exception on Nexux xx
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        startActivityForResult(photoPickerIntent, CONSTANTS.RESULT_ID.IMAGE);
    }

    /***
     * Listener to send imageButtons
     */
    public class SendMessageListener implements View.OnClickListener{
        private EditText text;
        SendMessageListener(EditText text){
            this.text=text;
        }
        /***
         * Only used to send text messages
         * @param v The View that was pressed
         */
        @Override
        public void onClick(View v){
            String stringMsg = text.getText().toString();
            if(!stringMsg.equals("")) {
                text.setText("");
                //TODO encrypt message
                //stringMsg = Encryption.encryptMessage(stringMsg, contact.getPublicKey());
                Message message = new TextMessage(chatID, stringMsg, Util.getTime(), true);

                //Send message to destination client
                MyApplication.getClient().sendMessage(contact.getUID(), message);

                Log.d("CHAT_ACTIVITY", "Sent to server");

                //saves to local db, which in turn triggers observers which update UI
                saveMessage(message);
            }
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            String fileName = Util.generateFileNameWithoutExtension(String.valueOf(Util.getTime()));
            //Extension is added when saved in
            photo_uri=  IO.createPlaceHolder(ChatActivity.this, CONSTANTS.MIME_TYPES.IMAGE_PATH, fileName, "jpg" );
            takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photo_uri);

            //Prevents Exposed URI exception
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            startActivityForResult(takePictureIntent, CONSTANTS.RESULT_ID.PHOTO);
        }
    }
    public class IntentLauncherListener implements View.OnClickListener{
        int result_ID;
        IntentLauncherListener(int result_ID){
            this.result_ID = result_ID;
        }
        @Override
        public void onClick(View v) {
            v.setEnabled(false);
            v.setClickable(false);
            if(result_ID == CONSTANTS.RESULT_ID.IMAGE) {
                if (isPermissionGranted(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, CONSTANTS.PERMISSION.READ_EXTERNAL)
                        && isPermissionGranted(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, CONSTANTS.PERMISSION.WRITE_EXTERNAL)) {

                    launchFilePicker();
                }
            }
            else if(result_ID == CONSTANTS.RESULT_ID.PHOTO){
                if (isPermissionGranted(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, CONSTANTS.PERMISSION.PHOTO)
                        && isPermissionGranted(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, CONSTANTS.PERMISSION.PHOTO)) {

                    dispatchTakePictureIntent();
                }
            }
            v.setEnabled(true);
            v.setClickable(true);
        }


    }
}
