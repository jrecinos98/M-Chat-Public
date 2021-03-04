package com.mchat.recinos.Activities.Home;

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
import com.mchat.recinos.Activities.Chat.MessagingActivity;
import com.mchat.recinos.CallBackInterfaces.DownloadResultCallback;
import com.mchat.recinos.CallBackInterfaces.ImeBackInterface;
import com.mchat.recinos.CallBackInterfaces.OnClickListenerInterface;
import com.mchat.recinos.CallBackInterfaces.OnFocusChangeInterface;
import com.mchat.recinos.Tasks.ImageDownloadTask;
import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Backend.Entities.Messages.Message;
import com.mchat.recinos.Backend.Entities.Messages.TextMessage;
import com.mchat.recinos.Backend.ViewModels.MessagingViewModel;
import com.mchat.recinos.CustomViews.KeyboardDismissEditText;
import com.mchat.recinos.MyApplication;
import com.mchat.recinos.R;
import com.mchat.recinos.Backend.Authentication;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.Constants;
import com.mchat.recinos.Util.Util;
import com.mchat.recinos.CustomViews.ViewListeners.CustomOnClickListener;
import com.mchat.recinos.CustomViews.ViewListeners.UserNameImeBackListener;
import com.mchat.recinos.CustomViews.ViewListeners.UserNameOnFocusChangeListener;

import java.io.Serializable;
import java.util.Map;

//TODO turn into a fragment used on the home activity
public class NewMessageActivity extends AppCompatActivity {
    private KeyboardDismissEditText usernameText;
    private EditText messageText;
    private ImageView contactImage;
    private ImageButton sendButton;
    private ProgressBar progressBar;
    private Contact friend;
    private MessagingViewModel messagingViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_new_message);
        Toolbar toolbar = findViewById(R.id.new_msg_bar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(R.string.new_msg);
            //Displays the back arrow on app bar
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }
        usernameText = findViewById(R.id.new_msg_username);
        usernameText.setOnEditTextImeBackListener(new UserNameImeBackListener(onKeyboardDismiss()));
        usernameText.setOnFocusChangeListener(new UserNameOnFocusChangeListener(onUserNameChange()));

        messageText = findViewById(R.id.new_msg_text);
        contactImage = findViewById(R.id.new_msg_img);
        progressBar = findViewById(R.id.new_progressbar);

        sendButton = findViewById(R.id.new_msg_send);
        sendButton.setEnabled(false);
        sendButton.setClickable(false);
        sendButton.setOnClickListener(new CustomOnClickListener(sendButtonOnClick()));

        messagingViewModel = ViewModelProviders.of(this, new MessagingViewModel.MessageViewModelFactory(this.getApplication(), MyApplication.getClient())).get(MessagingViewModel.class);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Validates the username field upon keyboard dismiss.
     * @return An implementation of the single method in "SAM" interface.
     */
    private ImeBackInterface onKeyboardDismiss(){
        return (ctrl, username)->{
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

        };
    }

    /**
     * Handles all the events that should take place when the send button is pressed.
     * @return
     */
    private OnClickListenerInterface sendButtonOnClick(){
        return (v)->{
            if(friend != null ){
                if(friend.isImageInit()) {
                    String friendUID = friend.getUID();
                    String message = messageText.getText().toString();
                    //Chat ID is the user ID.
                    int cid;
                    //find or generate a new chat id
                    if(!messagingViewModel.chatExists(friendUID)){
                        cid = Util.newChatID(getApplicationContext());
                        Chat chat = new Chat(cid, friend, message, Util.getTime());
                        messagingViewModel.insert(chat);
                    }else{
                        cid= messagingViewModel.getChatID(friendUID);
                        messagingViewModel.updateChatPreview(friendUID, message, Util.getTime(), 0);
                    }
                    Intent toChat = new Intent(getApplicationContext(), MessagingActivity.class);
                    //Need to specify serializable since contact also implements parcelable
                    toChat.putExtra(Constants.INTENT_ID.CONTACT_INFO, (Serializable) friend);
                    toChat.putExtra(Constants.INTENT_ID.CHAT_ID, cid);
                    if (!message.equals("")) {
                        Message msg = new TextMessage(cid, message,  Util.getTime(), true);
                        messagingViewModel.sendMessage(msg);
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

        };
    }

    /**
     * Handle focus changes to username edit text.
     * @return Returns an instance of OnFocusChangeInterface which acts essentially as a callback.
     */
    private OnFocusChangeInterface onUserNameChange(){
        return (v, hasFocus)->{
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
        };
    }

    /**
     * Once a username has been resolved this function is called to initiate the download of the image.
     * @param task Contains the snapshot received
     * @param username The username the user wants to contact.
     */
    private void handleUserNameTask(Task<DocumentSnapshot> task, String username){
        DocumentSnapshot document = task.getResult();
        //If not null then the user linked to the user name exists
        if (document != null && document.exists()) {
            Map<String, Object> data = document.getData();
            if(data != null)
                friend = Util.createFriendUser(data, username);
            if (!friend.isImageInit()) {
                progressBar.setVisibility(View.VISIBLE);
                //Download the image, updates the view and sets the imageBytes in friend reference
                ImageDownloadTask.DownloadImageTask download = new ImageDownloadTask.DownloadImageTask(ImageDownloadTask.DEFAULT_ID,onImageDownloadResult());
                download.execute(friend.getPhoto_URL());
            }
            sendButton.setEnabled(true);
            sendButton.setClickable(true);

        } else {

            contactImage.setBackgroundResource(R.drawable.default_avatar);
            usernameText.setError("User not found.");
        }
    }

    /**
     * Once an image is downloaded this function is invoked to update the UI and conserve the image byte data
     * @return
     */
    public DownloadResultCallback onImageDownloadResult(){
        return (id,image)->{
            friend.setImageBytes(image);
            BitmapTransform.updateRoundImage(contactImage, getResources(), image);
            progressBar.setVisibility(View.GONE);
        };

    }

}
