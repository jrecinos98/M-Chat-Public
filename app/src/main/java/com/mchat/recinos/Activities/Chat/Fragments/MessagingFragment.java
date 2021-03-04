package com.mchat.recinos.Activities.Chat.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mchat.recinos.Activities.Chat.Adapters.MessageListAdapter;
import com.mchat.recinos.Activities.Chat.MessagingActivity;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Backend.Entities.Messages.ImageMessage;
import com.mchat.recinos.Backend.Entities.Messages.Message;
import com.mchat.recinos.Backend.Entities.Messages.TextMessage;
import com.mchat.recinos.Backend.LocalStorage;
import com.mchat.recinos.Backend.ViewModels.MessagingViewModel;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.Constants;
import com.mchat.recinos.Util.IO;
import com.mchat.recinos.Util.Util;

import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CLIPBOARD_SERVICE;

public class MessagingFragment extends Fragment {

    //TODO keeping this can cause race condition if photos are sent consecutively (Not likely but can happen)
    private Uri photo_uri;
    private Contact contact;
    private int chatID;
    private MessagingViewModel messagingViewModel;
    private Context context;
    private View pendingSave;

    private EditText editText;
    private ImageButton sendButton;
    private ListView listView;
    private ImageButton sendImage;
    private ImageButton takePhoto;

    private MessageListAdapter mAdapter;

    public MessagingFragment(MessagingViewModel messagingViewModel, Contact contact, int id){
        this.messagingViewModel = messagingViewModel;
        this.contact = contact;
        this.chatID = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //tells the system that your fragment would like to receive menu-related callbacks.
        setHasOptionsMenu(true);
        mAdapter = new MessageListAdapter(context.getApplicationContext(), contact.getImageBitmap(), contact.getUID());

        //Here we add observer. When a message for this conversation is added this callback will trigger
        messagingViewModel.getCurrentChatMessages().observe(this, (messages)-> {
            //TODO call notifyDataSetChanged from this method instead.
            mAdapter.setMessages(messages);
            // Update the cached copy of the messages in the adapter.
            mAdapter.notifyDataSetChanged();
            //Scroll to bottom
            listView.smoothScrollToPosition(mAdapter.getCount());
        });
    }
    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_messaging, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.chat_toolbar);
        editText = rootView.findViewById(R.id.editText);
        if(getActivity() instanceof  AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (bar != null) {
                //bar.setTitle(title);
                bar.setDisplayShowTitleEnabled(false);
                bar.setDisplayHomeAsUpEnabled(true);
                bar.setDisplayShowHomeEnabled(true);
            }
        }
        if(contact != null){
            TextView t_text= rootView.findViewById(R.id.chat_title);
            t_text.setText(contact.getUserName());
            /* Use if decide to have image up on the tool bar*/
            ImageView imageView = rootView.findViewById(R.id.chat_contact_image);
            imageView.setBackground(BitmapTransform.roundedBitmapDrawable(getResources(), contact.getImage()));
        }

        sendButton = rootView.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new SendMessageListener(editText));

        sendImage = rootView.findViewById(R.id.send_image);
        sendImage.setOnClickListener(new IntentLauncherListener(Constants.RESULT_ID.IMAGE));

        takePhoto = rootView.findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(new IntentLauncherListener(Constants.RESULT_ID.PHOTO));

        listView = rootView.findViewById(R.id.message_list);
        listView.setOnItemLongClickListener(new ItemLongClick());
        listView.setAdapter(mAdapter);

        return rootView;
    }

    /**
     *  allows us to add options to the toolbar menu
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_chat, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        //If back arrow was pressed call the activity's onBackPress
        if(id == android.R.id.home){
            if(getActivity() instanceof  MessagingActivity)
                getActivity().onBackPressed();
        }
        //Delete all the messages for the conversation.
        if(id == R.id.delete_convo){
            //TODO add a warning Dialog
            messagingViewModel.deleteChat(chatID, messagingViewModel.getCurrentChatMessages().getValue());
            if(getActivity() instanceof  MessagingActivity)
                getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called by file picker activity once the user has finished making a selection.
     * @param reqCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //Retrieve URI
            final Uri imageUri = reqCode == Constants.RESULT_ID.IMAGE? data.getData() : photo_uri;
            long time = Util.getTime();
            String fileName = Util.generateFileNameWithExtension(
                    String.valueOf(time) ,
                    Util.getMimeType(context.getApplicationContext(),imageUri));
            ImageMessage imageMessage = new ImageMessage( chatID, null, imageUri, fileName, time, true );
            //If reqCode is an image from the Gallery or a Camera picture (respectively)
            if(reqCode == Constants.RESULT_ID.IMAGE || reqCode == Constants.RESULT_ID.PHOTO){
                messagingViewModel.sendMessage(imageMessage);
            }
        }else {
            Toast.makeText(context.getApplicationContext(), "No Image Selected",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * It is necessary to request permission from the user to get access to storage.
     * If the app explicitly requested a permission this method will be called right after
     * the permission has been accepted (or denied).
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case Constants.PERMISSION.READ_EXTERNAL:
                    launchFilePicker();
                    return;
                case Constants.PERMISSION.PHOTO: {
                    dispatchTakePictureIntent();
                    return;
                }
                //TODO generalize so that it will be able to handle any file type.
                case Constants.PERMISSION.SAVE_EXTERNAL: {
                    if(pendingSave != null) {
                        MessageListAdapter.ImageMessageViewHolder imageHolder = (MessageListAdapter.ImageMessageViewHolder) pendingSave.getTag();
                        Bitmap image = ((BitmapDrawable) imageHolder.image.getDrawable()).getBitmap();
                        LocalStorage.saveToPublicExternalStorage(context.getApplicationContext(), Constants.MIME_TYPES.IMAGE_PATH, imageHolder.extension, image, String.valueOf(Util.getTime()));
                        Toast.makeText(context.getApplicationContext(), "Saved image to external storage", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }

    /**
     * Initiates the activity for the file picker.
     */
    public void launchFilePicker(){
        Log.d("LAUNCH", "HUH");
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");

        //Prevents Exposed URI Exception on Nexus xx
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        startActivityForResult(photoPickerIntent, Constants.RESULT_ID.IMAGE);
    }

    /**
     * Initiates the camera to allow taking a picture.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            String fileName = Util.generateFileNameWithoutExtension(String.valueOf(Util.getTime()));
            //Extension is added when saved in
            photo_uri=  IO.createPlaceHolder(context, Constants.MIME_TYPES.IMAGE_PATH, fileName, "jpg" );
            takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photo_uri);

            //Prevents Exposed URI exception
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            startActivityForResult(takePictureIntent, Constants.RESULT_ID.PHOTO);
        }
    }
    //TODO extract inner classes and put them in separate files.

    /***
     * Listener to send textMessages
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

                //saves to local database AND sends it over the network, which in turn triggers observers to update UI
                messagingViewModel.sendMessage(message);
            }
        }
    }

    /**
     * Handles long-clicking an item. Media is saved to external storage and text is copied to clipboard.
     */
    public class ItemLongClick implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d("LONG_CLICK_LISTEN", "Position: "+ position);
            Message message =(Message) mAdapter.getItem(position);
            //Text gets copied to clipboard.
            if(message.getType() == Constants.MESSAGE_DATA_TYPES.TEXT){
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                //Add a unique label.
                ClipData clip = ClipData.newPlainText("mchat_text"+ (int) (100*Math.random()), message.getData());
                if(clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context.getApplicationContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            }
            else if(message.getType() == Constants.MESSAGE_DATA_TYPES.IMAGE){
                pendingSave = view;
                //Check the app has permission to write to external storage
                if (Util.isPermissionGranted(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE, Constants.PERMISSION.SAVE_EXTERNAL)
                        && Util.isPermissionGranted((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.PERMISSION.SAVE_EXTERNAL)) {

                    MessageListAdapter.ImageMessageViewHolder imageHolder = (MessageListAdapter.ImageMessageViewHolder) view.getTag();
                    Bitmap image = ((BitmapDrawable)imageHolder.image.getDrawable()).getBitmap();
                    LocalStorage.saveToPublicExternalStorage(context.getApplicationContext(), Constants.MIME_TYPES.IMAGE_PATH, imageHolder.extension, image, String.valueOf(Util.getTime()));
                    Toast.makeText(context.getApplicationContext(), "Saved image to external storage", Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        }
    }

    /**
     * Handles clicking on icons on the Message bar.
     */
    public class IntentLauncherListener implements View.OnClickListener{
        int result_ID;
        IntentLauncherListener(int result_ID){
            this.result_ID = result_ID;
        }
        @Override
        public void onClick(View v) {
            v.setEnabled(false);
            v.setClickable(false);
            //Check app permissions before hand to prevent an exception.
            if(result_ID == Constants.RESULT_ID.IMAGE) {
                if (Util.isPermissionGranted(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE, Constants.PERMISSION.READ_EXTERNAL)
                        && Util.isPermissionGranted(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.PERMISSION.WRITE_EXTERNAL)) {

                    launchFilePicker();
                }
            }
            else if(result_ID == Constants.RESULT_ID.PHOTO){
                if (Util.isPermissionGranted( getActivity() , Manifest.permission.READ_EXTERNAL_STORAGE, Constants.PERMISSION.PHOTO)
                        && Util.isPermissionGranted(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.PERMISSION.PHOTO)) {

                    dispatchTakePictureIntent();
                }
            }
            v.setEnabled(true);
            v.setClickable(true);
        }
    }
}