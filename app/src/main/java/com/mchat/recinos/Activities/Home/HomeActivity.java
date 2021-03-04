package com.mchat.recinos.Activities.Home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.mchat.recinos.Activities.EntryActivity;
import com.mchat.recinos.Activities.Home.Adapters.CallListAdapter;
import com.mchat.recinos.Activities.Home.Adapters.ChatListAdapter;
import com.mchat.recinos.Activities.Home.Adapters.ViewPagerAdapter;
import com.mchat.recinos.Activities.Home.Fragments.CallsFragment;
import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.Entities.Call;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.User;
import com.mchat.recinos.Backend.ViewModels.HomeViewModel;
import com.mchat.recinos.Activities.Home.Fragments.ChatsFragment;
import com.mchat.recinos.MyApplication;
import com.mchat.recinos.R;
import com.mchat.recinos.Backend.Authentication;
import com.mchat.recinos.Util.Constants;

import java.util.List;


public class HomeActivity extends AppCompatActivity {
    private ChatListAdapter chatAdapter;
    private CallListAdapter callAdapter;

    private HomeViewModel homeViewModel;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private FloatingActionButton floatingButton;
    private ViewPagerAdapter viewPagerAdapter;

    private boolean callsEmpty;
    private boolean chatsEmpty;
    private String uid;
    private String current_frag;

    private Client client;

    //public static initClient
    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        client = MyApplication.getClient();
        uid = client.getUserID();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.callsEmpty = sharedPreferences.getBoolean(Constants.CALL_LIST_EMPTY+ uid, true);
        this.chatsEmpty = sharedPreferences.getBoolean(Constants.CHAT_LIST_EMPTY+ uid, true);

        homeViewModel = ViewModelProviders.of(this, new HomeViewModel
                .HomeViewModelFactory(this.getApplication(), client))
                .get(HomeViewModel.class);
        homeViewModel.getOrderedChats().observe(this, this::handleChatChanges);
        chatAdapter = new ChatListAdapter(this, homeViewModel);
        callAdapter = new CallListAdapter(this);

        ViewPager viewPager = findViewById(R.id.view_pager);

        viewPagerAdapter = new ViewPagerAdapter(
                getSupportFragmentManager(),
                chatsEmpty,callsEmpty,
                new ChatsFragment(chatAdapter),
                new CallsFragment(callAdapter)
        );
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
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
            homeViewModel.deleteUser(progressBar);
            //TODO make sure to call restart after making sure the user is deleted.
            restart();
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleChatChanges(List<Chat> chats) {
        if (chatsEmpty && chats != null && !chats.isEmpty()) {
            viewPagerAdapter.setListStatus(ViewPagerAdapter.CHAT_FRAGMENT, false);
            sharedPreferences.edit().putBoolean(Constants.CHAT_LIST_EMPTY + uid, false).apply();
            chatsEmpty = false;
        }
        //If all chats get deleted from the database.
        else if(!chatsEmpty && (chats == null || chats.isEmpty())){
            viewPagerAdapter.setListStatus(ViewPagerAdapter.CHAT_FRAGMENT, true);
            //Update value to empty
            sharedPreferences.edit().putBoolean(Constants.CHAT_LIST_EMPTY + uid, true).apply();
            chatsEmpty = true;
        }
        chatAdapter.setChats(chats);
    }
    private void handleCallChanges(List<Call> calls){

    }

    //return to entry activity. Used when logging out.
    private void restart(){
        Intent toEntry = new Intent(this, EntryActivity.class);
        startActivity(toEntry);
        this.finishAffinity();
    }
    //Disconnect the client from the server and remove linked google account
    private void signOut() {
        Authentication.getInstance().signOut();
        //Disconnect the client from server
        MyApplication.resetClient();
        //Disconnect the linked google account
        Authentication.googleSignOut(this).addOnCompleteListener(this, (task)-> restart());

    }
    public class NewMessageFabListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(client == null || !client.isConnected()) {
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
