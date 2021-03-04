package com.mchat.recinos.Activities.Authentication;
import android.content.Intent;
import android.os.Bundle;
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
import com.mchat.recinos.Activities.EntryActivity;
import com.mchat.recinos.Backend.Authentication;
import com.mchat.recinos.Activities.Authentication.Fragments.SignInFragment;
import com.mchat.recinos.Activities.Authentication.Fragments.SignUpFragment;
import com.mchat.recinos.Activities.Authentication.Interfaces.AuthInterface;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.Constants;

/**
 * Activity responsible of handling authentication processes and managing the auth fragments.
 */
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
        FirebaseUser currentUser = Authentication.getInstance().getCurrentUser();
        if(currentUser != null){
            returnToEntry(currentUser);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        //Prevents keyboard from popping up automatically when there is EditBox
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
        updateFragment(signInFragment, Constants.FRAG_TAGS.LOG_IN_TAG);
        progressBar = findViewById(R.id.auth_progress_bar);
        //Init intent to change activity after authentication succeeds.
        toEntry= new Intent(this, EntryActivity.class);
    }

    //Enables a back arrow on the toolbar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    //Need to override the default onBackPressed so that navigation occurs properly with fragments.
    @Override
    public void onBackPressed() {
        //If the fragment stack isn't empty pop the one on top
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        }
        //Otherwise we are on the bottom most fragment and we can pop the Activity itself.
        else {
            //The finish() method ensures that the current activity won't be added to the back of the stack
            //Instead it will be removed entirely and the user won't be able to navigate back to it
            this.finish();
        }
    }
    //Updates the current fragment shown on the Activity. The current fragment is added to the back of the stack.
    private void updateFragment (Fragment frag, String tag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment, add to stack and commit the transaction
        ft.replace(R.id.fragment_placeholder, frag).addToBackStack(tag).commit();
    }

    /***
     * AuthInterface methods below. Essentially just callbacks to the activity from the fragment
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
        updateFragment(signUpFragment, Constants.FRAG_TAGS.SIGN_UP_TAG);
        //showToolBar("Sign Up");
    }
    @Override
    public void emailSignUp() {
        signUpFragment = new SignUpFragment(this);
        updateFragment(signUpFragment, Constants.FRAG_TAGS.SIGN_UP_TAG);
        //showToolBar("Sign Up");
    }
    //This method is a callback method used by fragments when a FirebaseUser is created (a user has sign in)    @Override
    //This passes a result back to EntryActivity about the success of authentication.
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
