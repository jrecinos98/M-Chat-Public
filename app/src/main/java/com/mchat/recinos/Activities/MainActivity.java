package com.mchat.recinos.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.mchat.recinos.Adapters.ListAdapters.CallListAdapter;
import com.mchat.recinos.Adapters.ListAdapters.ChatListAdapter;
import com.mchat.recinos.Adapters.ViewPagerAdapter;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.Backend.Entities.Call;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.User;
import com.mchat.recinos.Backend.ViewModels.ChatViewModel;
import com.mchat.recinos.Fragments.ChatsFragment;
import com.mchat.recinos.MyApplication;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.Authentication;
import com.mchat.recinos.Util.CONSTANTS;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ChatListAdapter chatAdapter;
    private CallListAdapter callAdapter;

    private ChatViewModel chatViewModel;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private ChatsFragment chatsFragment;
    private FloatingActionButton floatingButton;
    private ViewPagerAdapter viewPagerAdapter;

    private boolean callsEmpty;
    private boolean chatsEmpty;
    private String uid;
    private String current_frag;

    //public static initClient
    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        uid= MyApplication.getClient().getUserID();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.callsEmpty = sharedPreferences.getBoolean(CONSTANTS.CALL_LIST_EMPTY+ MyApplication.getClient().getUserID(), true);
        this.chatsEmpty = sharedPreferences.getBoolean(CONSTANTS.CHAT_LIST_EMPTY+ MyApplication.getClient().getUserID(), true);

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.getOrderedChats().observe(this, this::handleChatChanges);

        chatAdapter = new ChatListAdapter(this, chatViewModel);
        callAdapter = new CallListAdapter(this);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), chatsEmpty,callsEmpty, chatAdapter, callAdapter );
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        chatsFragment = new ChatsFragment(chatAdapter);
        progressBar= findViewById(R.id.progressbar);
        floatingButton = findViewById(R.id.fab);
        floatingButton.setOnClickListener(new NewMessageFabListener());
        //Will persist configuration changes
       MyApplication.startAsync(progressBar);
        Toast.makeText(this, "Welcome, " + User.getLogInUser().getUsername(), Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {super.onDestroy();}
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Util.saveToBundle(outState, gameGrid, difficulty, time, GAME_STATE);
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onResume(){
        //MyApplication.startAsync(getApplicationContext(), progressBar);
        super.onResume();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main,  menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent toSettings = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(toSettings);
            return true;
        }
        else if(id== R.id.sign_out){
            signOut();
            return true;
        }
        else if(id == R.id.delete_acct){
            progressBar.setVisibility(View.VISIBLE);
            //Disconnect the client from server
            MyApplication.resetClient();
            Authentication.googleSignOut(getApplicationContext()).addOnCompleteListener((task)-> {
                progressBar.setVisibility(View.INVISIBLE);
                CloudDatabase.getInstance().deleteUser(MyApplication.getClient().getPrimaryUser());
                CloudDatabase.getInstance().signOut();
                restart();
            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleChatChanges(List<Chat> chats) {
        if (chatsEmpty && chats != null && !chats.isEmpty()) {
            viewPagerAdapter.setListStatus(ViewPagerAdapter.CHAT_FRAGMENT, false);
            sharedPreferences.edit().putBoolean(CONSTANTS.CHAT_LIST_EMPTY + uid, false).apply();
            chatsEmpty = false;
        }
        else if(!chatsEmpty && (chats == null || chats.isEmpty())){
            viewPagerAdapter.setListStatus(ViewPagerAdapter.CHAT_FRAGMENT, true);
            //Update value to empty
            sharedPreferences.edit().putBoolean(CONSTANTS.CHAT_LIST_EMPTY + uid, true).apply();
            chatsEmpty = true;
        }
        chatAdapter.setChats(chats);
    }
    private void handleCallChages(List<Call> calls){

    }
    //return to entry activity. Used when logging out.
    private void restart(){
        Intent toEntry = new Intent(this, EntryActivity.class);
        startActivity(toEntry);
        this.finishAffinity();
    }
    //Disconnect the client from the server and remove linked google account
    private void signOut() {
        CloudDatabase.getInstance().signOut();
        //Disconnect the client from server
        MyApplication.resetClient();
        //Disconnect the linked google account
        Authentication.googleSignOut(this).addOnCompleteListener(this, (task)-> restart());

    }
    public class NewMessageFabListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(MyApplication.getClient() == null || !MyApplication.getClient().isConnected()) {
                Snackbar.make(view, "Not connected to server. Try again later", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            else{
                Intent toNewMessage = new Intent(getApplicationContext(), NewMessageActivity.class);
                //This flag makes the new Message activity to not be kept in the stack so after navigating away the view disappears.
                toNewMessage.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(toNewMessage);
            }
        }
    }
}
