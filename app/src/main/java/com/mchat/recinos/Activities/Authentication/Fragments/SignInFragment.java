package com.mchat.recinos.Activities.Authentication.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.Activities.Authentication.Interfaces.AuthInterface;
import com.mchat.recinos.R;
import com.mchat.recinos.Backend.Authentication;
import com.mchat.recinos.Util.Constants;
import com.shobhitpuri.custombuttons.GoogleSignInButton;


public class SignInFragment extends Fragment implements CloudDatabase.AuthInterface {
    private Context mContext;
    private EditText emailBox;
    private EditText passwordBox;
    private AppCompatButton logInButton;
    private AuthInterface listener;
    private GoogleSignInClient mGoogleSignInClient;
    //6ZIPAUVKvuQ2vvRTsepocAFV
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            //Necessary to make callbacks on the parent activity which implements AuthInterface
            //We can hide/show toolbar, show/hide ationbar, etc, from the fragment.
            listener = (AuthInterface) context;
        } catch (ClassCastException castException) {
            /* The activity does not implement the listener. */
        }
    }
    //onCreate is called before onCreateView. It is called before the layout is even inflated.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);
    }
    //Called to inflate the layout of the fragment. Here we can perform graphical operations
    //Such as inflating a layout and instantiating views.
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        listener.hideToolbar();

        View rootView = inflater.inflate(R.layout.frag_sign_in, container, false);
        GoogleSignInButton googleSignInButton = rootView.findViewById(R.id.google_sign_in);
        emailBox = rootView.findViewById(R.id.input_email);
        passwordBox = rootView.findViewById(R.id.input_password);
        logInButton = rootView.findViewById(R.id.sign_in_button);
        TextView textView = rootView.findViewById(R.id.create_acct);

        //set listeners
        logInButton.setOnClickListener((view)-> emailSignIn());
        googleSignInButton.setOnClickListener((view)-> googleSignIn());
        //If sign up text pressed then tell AuthActivity(listener) to update the current fragment to email sign up fragment.
        textView.setOnClickListener((view)-> listener.emailSignUp());
        return rootView;
    }
    @Override
    public void onStart(){
        super.onStart();
    }

    //LogInButton listener. Performs text validation on the passwordBox and the emailBox.
    private void emailSignIn(){
        if(!Authentication.isPasswordValid(passwordBox)){
            return;
        }
        String password = passwordBox.getText().toString();
        String user = emailBox.getText().toString();
        if(Authentication.isEmail(user)){
            if(!Authentication.isEmailValid(emailBox)){
                return;
            }
            //If email is valid, disable the button to not send multiple requests. It will be re-enabled if signIn fails.
            logInButton.setClickable(false);
            //Sign in user. Pass a reference of the fragment so that it will be notified of the status of the transaction.
            Authentication.getInstance().emailSignIn(user, password, SignInFragment.this);
        }
        //If not email then username was provided.
        else{
            //Verify it is a valid username
            if(!Authentication.isUserNameValid(emailBox)){
                return;
            }
            //Disable button to only allow one request. It will be re-enabled if signIn fails.
            logInButton.setClickable(false);
            Authentication.getInstance().userNameSignIn(user, password,SignInFragment.this);
        }
    }
    //Starts sign in with google process.
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        //This method is like startActivity but this declares that we expect the new activity to send a result back.
        startActivityForResult(signInIntent, Constants.RESULT_ID.GOOGLE_LOG_IN);
    }
    //This is a callback method that gives us the result of the GoogleSignIn activity.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == Constants.RESULT_ID.GOOGLE_LOG_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(mContext, "Google Sign In Failed", Toast.LENGTH_LONG).show();
                Log.w("SIGN_IN_ERROR", "signInResult:failed code=" + e.getStatusCode());
                listener.returnToEntry(null);
            }
        }
    }
    //Authenticate with Firebase using Google credentials.
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        listener.showProgressBar();
        Authentication.getInstance().getAuth().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            // Check if this is a new account. If it is then lead them to the sign up page to select a username.
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                // If new user, we need to make user choose username.
                                // So delete the user from firebase UNTIL the user selects a username and uploads a pic
                                // After that has been done it is safe to create a user.
                                Authentication.getInstance().deleteAuth().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        listener.googleSignUp(acct);
                                        listener.hideProgressBar();
                                    }
                                });
                            }
                            //If existing user pass on the user to the listener callback.
                            else {
                                FirebaseUser user = Authentication.getInstance().getCurrentUser();
                                listener.hideProgressBar();
                                listener.returnToEntry(user);

                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FIREBASE", "signInWithCredential:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            listener.returnToEntry(null);
                        }
                    }
                });
    }
    //This is the callback method called by email sign in.
    @Override
    public void onAuthSuccessful(boolean success){
        if(success){
            listener.returnToEntry(Authentication.getInstance().getCurrentUser());
        }
        else{
            logInButton.setClickable(true);
        }
    }

}