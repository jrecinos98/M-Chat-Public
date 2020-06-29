package com.mchat.recinos.Activities;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.Fragments.SignInFragment;
import com.mchat.recinos.Fragments.SignUpFragment;
import com.mchat.recinos.Interfaces.AuthInterface;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.CONSTANTS;


public class AuthActivity extends AppCompatActivity implements AuthInterface{
    private GoogleSignInAccount gAccount;
    private SignInFragment signInFragment;
    private SignUpFragment signUpFragment;
    private ProgressBar progressBar;
    private Intent toEntry;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = CloudDatabase.getInstance().getCurrentUser();
        if(currentUser != null){
            returnToEntry(currentUser);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        //Prevents keyboard from popping up automatically when there is editbox
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }
        //Set initial fragment
        signInFragment = new SignInFragment();
        updateFragment(signInFragment, CONSTANTS.FRAG_TAGS.LOG_IN_TAG);
        progressBar = findViewById(R.id.auth_progress_bar);
        toEntry= new Intent(this, EntryActivity.class);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.reset){
            //TODO reset all the fields
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            this.finish();
            super.onBackPressed();
        }
    }
    private void updateFragment (Fragment frag, String tag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment, add to stack and commit the transaction
        ft.replace(R.id.fragment_placeholder, frag).addToBackStack(tag).commit();
    }

    /***
     * AuthInterface methods. Essentially just callbacks to the activity from the fragment
     */
    @Override
    public void showActionBar(String title){
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().show();
    }
    @Override
    public void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }
    @Override
    public void hideProgressBar(){
        progressBar.setVisibility(View.GONE);
    }
    @Override
    public void googleSignUp(GoogleSignInAccount acct) {
        gAccount = acct;
        signUpFragment = new SignUpFragment(this, gAccount);
        updateFragment(signUpFragment, CONSTANTS.FRAG_TAGS.SIGN_UP_TAG);
        //showToolBar("Sign Up");
    }
    @Override
    public void emailSignUp() {
        signUpFragment = new SignUpFragment(this);
        updateFragment(signUpFragment, CONSTANTS.FRAG_TAGS.SIGN_UP_TAG);
        //showToolBar("Sign Up");
    }
    //This activity is launched by Entry Activity and it expects a result so we must set it.
    @Override
    public void returnToEntry(FirebaseUser account){
        if(account != null){
            setResult(RESULT_OK);
        }
        else{
            //If user presses back before login then set exit code for EntryActivity
            setResult(RESULT_CANCELED);
        }
        startActivity(toEntry);
        this.finish();
    }
    @Override
    public void hideToolbar(){
        getSupportActionBar().hide();
    }
}
