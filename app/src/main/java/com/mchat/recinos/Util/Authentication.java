package com.mchat.recinos.Util;

import android.content.Context;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

public class Authentication {
    public static boolean isEmail(String email){
        return true;
    }
    public static boolean isNameValid(EditText p){
        String name = p.getText().toString();
        if(name.equals("")){
            p.setError("Name field is blank");
            return false;
        }
        //Check that all chars are letters
        for (int i=0; i < name.length();i++){
            if (!Character.isLetter(name.charAt(i))){
                if((name.charAt(i)+"").equals(" ")){
                    continue;
                }
                p.setError("Name cannot contain digits or new lines.");
                return false;
            }
        }
        return true;
    }
    public static boolean isPasswordValid(EditText p){
        String pass1 = p.getText().toString();
        if (pass1.equals("")){
            p.setError("Password field cannot be empty.");
            return false;
        }
        if(pass1.length() < 6){
            p.setError("Password must be at least 6 characters long");
            return false;
        }
        //TODO Check if password contains numbers, symbols, upper case, etc..
        return true;
    }
    public static boolean isNewPasswordValid(EditText p1, EditText p2){
        String pass1 = p1.getText().toString();
        String pass2 = p2.getText().toString();
        if(!isPasswordValid(p1)){
            return false;
        }
        if(!isPasswordValid(p2)){
            return false;
        }
        if(!pass1.equals(pass2)){
            p1.setError("Passwords do not match.");
            return false;
        }
        return true;
    }
    public static boolean isUserNameValid(EditText p){
        String uname = p.getText().toString();
        if(uname.length() < 8 || uname.length() > 16){
            p.setError("Username must be between 8-16 characters.");
            return false;
        }
        //Check that all inputs are valid (no emoji)
        return true;
    }
    public static boolean isEmailValid(EditText p){
        String email = p.getText().toString();
        //TODO check that there is a @ symbol in there and only valid chars used.
        return true;
    }
    public static Task<Void> googleSignOut(Context context){
        //Disconnect the linked google account
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        return mGoogleSignInClient.signOut();
    }
}
