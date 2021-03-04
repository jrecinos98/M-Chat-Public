package com.mchat.recinos.Backend;

import android.content.Context;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mchat.recinos.Util.Constants;

import java.util.Map;

public class Authentication {

    private FirebaseAuth mAuth;
    private static Authentication instance;

    public static Authentication getInstance(){
        if (instance == null){
            instance = new Authentication();
        }
        return instance;
    }
    Authentication(){
        mAuth = FirebaseAuth.getInstance();
    }
    //TODO implement
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
    public void signOut(){
        mAuth.signOut();
    }
    public FirebaseAuth getAuth(){return mAuth;}
    public FirebaseUser getCurrentUser(){
        return mAuth.getCurrentUser();
    }

    public Task<Void> deleteAuth(){
        return getCurrentUser().delete();
    }
    public Task<AuthResult> emailSignUp(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email, password);
    }
    public Task<AuthResult> googleSignUp(AuthCredential credential){
        return mAuth.signInWithCredential(credential);
    }

    public Task<Void> updateProfile(UserProfileChangeRequest profile){
        return mAuth.getCurrentUser().updateProfile(profile);
    }
    public static Task<Void> googleSignOut(Context context){
        //Disconnect the linked google account
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        return mGoogleSignInClient.signOut();
    }

    public void userNameSignIn(String userName, String password, CloudDatabase.AuthInterface listener){
        CloudDatabase.getInstance()
                .getUserInfoFromUserName(userName)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    Map<String, Object> data = doc.getData();
                    String email = (String) data.get(Constants.PUBLIC_DATA_ENTRY.EMAIL);
                    emailSignIn(email, password, listener);
                }else{
                    listener.onAuthSuccessful(false);
                }
            }
        });

    }
    //Signs in user with email and password.
    //A listener can be provided so it can be notified of the success of the transaction
    public void emailSignIn(String email, String password, CloudDatabase.AuthInterface listener){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //Notify listener about the success/failure.
                listener.onAuthSuccessful(task.isSuccessful());
                if(task.isSuccessful()) {
                    //TODO use the token to authenticate with the server/API.
                    task.getResult().getUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            //We obtained the token
                            if (task.isSuccessful()) {
                                String token = task.getResult().getToken();
                                // 'token' is not a Google Access Token
                                //Send token to authenticate with server
                            }
                        }
                    });
                }
            }
        });
    }
    public Task<GetTokenResult> getFirebasetoken(){
        return mAuth.getCurrentUser().getIdToken(true);
        /*
         .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    // 'token' is not a Google Access Token
                }
            }
        });
         */
    }

    public void getPublicKey(){}

    public void getMessages(){}

    public String getUID(){ return getCurrentUser().getUid();}

    //This would connect to my server to signal availability of user
    public void initVoIP(){

    }

    public interface AuthInterface{
        void onAuthSuccessful(boolean success);
    }
}
