package com.mchat.recinos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mchat.recinos.AsyncTasks.ImageDownloadTask;
import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Backend.Entities.Message;
import com.mchat.recinos.Backend.Entities.TextMessage;
import com.mchat.recinos.Backend.ViewModels.MessageViewModel;
import com.mchat.recinos.CustomViews.EditTextBackEvent;
import com.mchat.recinos.MyApplication;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.Authentication;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.CONSTANTS;
import com.mchat.recinos.Util.Util;

import java.io.Serializable;
import java.util.Map;

//TODO replace blue placeholder image and use the profile icon instead
public class NewMessageActivity extends AppCompatActivity implements ImageDownloadTask.DownloadResultCallback {
    private EditTextBackEvent usernameText;
    private EditText messageText;
    private ImageView contactImage;
    private ImageButton sendButton;
    private ProgressBar progressBar;

    private Contact friend;
    private MessageViewModel messageViewModel;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        Toolbar toolbar = findViewById(R.id.new_msg_bar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(R.string.new_msg);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }
        usernameText = findViewById(R.id.new_msg_username);
        usernameText.setOnEditTextImeBackListener(new UserNameImeBackListener());
        usernameText.setOnFocusChangeListener(new UserNameOnFocusChangeListener());

        messageText = findViewById(R.id.new_msg_text);
        contactImage = findViewById(R.id.new_msg_img);
        progressBar = findViewById(R.id.new_progressbar);

        sendButton = findViewById(R.id.new_msg_send);
        sendButton.setEnabled(false);
        sendButton.setClickable(false);
        sendButton.setOnClickListener(new SendButtonOnClickListener());

        messageViewModel = ViewModelProviders.of(this, new MessageViewModel.MessageViewModelFactory(this.getApplication())).get(MessageViewModel.class);

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    public void handleUserNameTask(Task<DocumentSnapshot> task, String username){
        DocumentSnapshot document = task.getResult();
        //If not null then the user linked to the user name exists
        if (document != null && document.exists()) {
            Map<String, Object> data = document.getData();
            if(data != null)
                friend = Util.createFriendUser(data, username);
            if (!friend.isImageInit()) {
                progressBar.setVisibility(View.VISIBLE);
                //Download the image, updates the view and sets the imageBytes in friend reference
                ImageDownloadTask.DownloadImageTask download = new ImageDownloadTask.DownloadImageTask(ImageDownloadTask.DEFAULT_ID,NewMessageActivity.this);
                download.execute(friend.getPhoto_URL());
            }
            sendButton.setEnabled(true);
            sendButton.setClickable(true);

        } else {
            contactImage.setBackgroundResource(R.drawable.round_btn);
            usernameText.setError("User not found.");
        }
    }
    //This listener is called when the keyboard is dismissed
    public class UserNameImeBackListener implements EditTextBackEvent.EditTextImeBackListener {
        @Override
        public void onImeBack(EditTextBackEvent ctrl, String username) {
            sendButton.setEnabled(false);
            sendButton.setClickable(false);
            if (Authentication.isUserNameValid(usernameText)) {
                CloudDatabase.getInstance().getUserInfoFromUserName(username).addOnCompleteListener((task)-> {
                    if (task.isSuccessful()) {
                        handleUserNameTask(task, username);
                    }
                    Log.d("MY_FIREBASE", "Error obtaining user");
                });
            } else {
                usernameText.setError("Enter a valid username");
            }

        }
    }
    //This listener kicks in when the user changes from usernameText to a different view
    public class UserNameOnFocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus){
            sendButton.setEnabled(false);
            sendButton.setClickable(false);
            if(!hasFocus && usernameText.getText() != null){
                String username = usernameText.getText().toString();
                if (Authentication.isUserNameValid(usernameText)) {
                    CloudDatabase.getInstance().getUserInfoFromUserName(username).addOnCompleteListener((task)-> {
                        if (task.isSuccessful()) {
                            handleUserNameTask(task, username);
                        }
                        contactImage.setBackgroundResource(R.drawable.round_btn);
                        Log.d("MY_FIREBASE", "Error obtaining user");
                    });
                } else {
                    usernameText.setError("Enter a valid username");
                }
            }
        }
    }
    public class SendButtonOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(friend != null ){
                if(friend.isImageInit()) {
                    String friendUID = friend.getUID();
                    String message = messageText.getText().toString();
                    //Chat ID is the user ID.
                    int cid;
                    //find or generate a new chat id
                    if(!messageViewModel.chatExists(friendUID)){
                        cid = Util.newChatID(getApplicationContext());
                        Chat chat = new Chat(cid, friend, message, Util.getTime());
                        messageViewModel.insert(chat);
                    }else{
                        cid= messageViewModel.getChatID(friendUID);
                        messageViewModel.updateChatPreview(friendUID, message, Util.getTime(), 0);
                    }
                    Intent toChat = new Intent(getApplicationContext(), ChatActivity.class);
                    //Need to specify serializable since contact also implements parcelable
                    toChat.putExtra(CONSTANTS.INTENT_ID.CONTACT_INFO, (Serializable) friend);
                    toChat.putExtra(CONSTANTS.INTENT_ID.CHAT_ID, cid);
                    if (!message.equals("")) {
                        Message msg = new TextMessage(cid, message,  Util.getTime(), true);
                        messageViewModel.insert(msg);
                        MyApplication.getClient().sendMessage(friend.getUID(), msg);
                        //toChat.putExtra(CONSTANTS.INTENT_ID.MESSAGE, (Serializable) msg);
                    }
                    Client.CURRENT_CHAT = cid;
                    startActivity(toChat);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please wait. Try again shortly.", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(getApplicationContext(), "An Error occured try again.", Toast.LENGTH_LONG).show();
            }

        }

    }
    @Override
    public void onImageDownloadResult(int id, byte[] image){
        friend.setImageBytes(image);
        BitmapTransform.updateRoundImage(contactImage, getResources(), image);
        progressBar.setVisibility(View.GONE);
    }

}
