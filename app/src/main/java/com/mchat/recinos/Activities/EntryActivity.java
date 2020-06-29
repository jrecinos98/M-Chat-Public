package com.mchat.recinos.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.FirebaseUser;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.BuildConfig;
import com.mchat.recinos.Util.Authentication;


/**
 * Entry class to decide which activity to display. No need to inflate a layout so fast and it keeps the logic clean
 * IMPORTANT: This Activity is not kept in the stack to prevent issues when back key pressed in Main or Login
 */
//TODO Make this activity to a fragment. It is ess resoure intensive so it helps with start up
public class EntryActivity extends Activity {
    @Override
    protected void onStart(){
        super.onStart();
        //TODO Figure out why it crashes without this. (Crashes on phone but not emulator with lower sdk, may have to do with the theme used for this activity)
        //There's an error if this is not set. Need further digging
        setVisible(true);
    }
    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        //CHECK IF IT IS A FIRST TIME LOGIN
        checkFirstRun();

        FirebaseUser currentUser = CloudDatabase.getInstance().getCurrentUser();
        // Check for existing Google Sign In account, if the user is already signed in
        if (currentUser != null) {
            startMain();
        }
        else{
            startLogin();
        }
    }

    public void startMain(){
        //I can use this to pass it to an activity but getLastSignIn works anywhere
        //account.writeToParcel();
        Intent toMain = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(toMain);
    }
    public void startLogin(){
        Intent toLogIn = new Intent(getApplicationContext(), AuthActivity.class);
        //Check if a previous an incomplete google sign in occured and sign user out before showing sign in screen.
        if(GoogleSignIn.getLastSignedInAccount(getApplicationContext()) != null){
            Authentication.googleSignOut(getApplicationContext()).addOnCompleteListener((task)-> {
                if(task.isSuccessful()){
                    startActivity(toLogIn);
                }
            });
        }
        else{
            startActivity(toLogIn);
        }

    }

    private void checkFirstRun() {

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;
        // Get saved version code
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            return;
        } else if (savedVersionCode == DOESNT_EXIST) {

            // TODO This is a new install (or the user cleared the shared preferences)
            Toast.makeText(getApplicationContext(),"Welcome new user!", Toast.LENGTH_LONG).show();

        } else if (currentVersionCode > savedVersionCode) {

            // TODO This is an upgrade
            Toast.makeText(getApplicationContext(),"Welcome, enjoy the upgrade", Toast.LENGTH_LONG).show();
        }
        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }



}
