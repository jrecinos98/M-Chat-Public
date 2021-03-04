package com.mchat.recinos.Activities.Chat;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.mchat.recinos.Activities.Chat.Adapters.MessageListAdapter;
import com.mchat.recinos.Activities.Chat.Fragments.MessagingFragment;
import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Backend.Entities.Messages.Message;
import com.mchat.recinos.Backend.ViewModels.MessagingViewModel;
import com.mchat.recinos.MyApplication;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.Constants;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    private Contact contact;
    private int chatID;

    private MessagingViewModel messagingViewModel;
    private MessagingFragment mFragment;


    public List<Message> messages;
    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        //MyApplication.broadcastManager.unregisterReceiver(messageReceiver);
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messsaging);
                //If not null then the activity is being resumed
        if(savedInstanceState != null){

        }
        contact = (Contact) getIntent().getExtras().getSerializable(Constants.INTENT_ID.CONTACT_INFO);
        chatID = getIntent().getExtras().getInt(Constants.INTENT_ID.CHAT_ID);

        //Will persist configuration changes. NOTE: We pass the chat id to the viewholder so that it only notifies of relevant messages
        messagingViewModel = ViewModelProviders.of(this, new MessagingViewModel.MessageViewModelFactory(this.getApplication(), MyApplication.getClient(),  this, chatID)).get(MessagingViewModel.class);

        mFragment = new MessagingFragment(messagingViewModel, contact,chatID);
        //Update the current fragment.
        updateFragment(mFragment, Constants.FRAG_TAGS.MESSAGE_TAG);
    }
    //Updates the current fragment shown on the Activity. The current fragment is added to the back of the stack.
    private void updateFragment (Fragment frag, String tag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment, add to stack and commit the transaction
        ft.replace(R.id.frag_holder, frag).addToBackStack(tag).commit();
    }


    @Override
    public void onBackPressed() {
        Client.CURRENT_CHAT = 0;
        //If the fragment stack isn't empty pop the one on top
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        }
        //Otherwise we are on the bottom most fragment and we can pop the Activity itself.
        else {
            //The finish() method ensures that the current activity won't be added to the back of the stack
            //Instead it will be removed entirely and the user won't be able to navigate back to it
            this.finish();
            //super.onBackPressed();
        }
    }

}
