package com.mchat.recinos.Fragments;

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
import com.mchat.recinos.Interfaces.AuthInterface;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.Authentication;
import com.mchat.recinos.Util.CONSTANTS;
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
            listener = (AuthInterface) context;
        } catch (ClassCastException castException) {
            /* The activity does not implement the listener. */
        }
    }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        listener.hideToolbar();
        View rootView = inflater.inflate(R.layout.frag_sign_in, container, false);
        GoogleSignInButton googleSignInButton = rootView.findViewById(R.id.google_sign_in);
        emailBox = rootView.findViewById(R.id.input_email);
        passwordBox = rootView.findViewById(R.id.input_password);
        logInButton = rootView.findViewById(R.id.sign_in_button);
        TextView textView = rootView.findViewById(R.id.create_acct);
        //set listeners
        logInButton.setOnClickListener((v)-> emailSignIn());
        googleSignInButton.setOnClickListener((v)-> googleSignIn());
        textView.setOnClickListener((v)-> listener.emailSignUp());
        return rootView;
    }
    @Override
    public void onStart(){
        super.onStart();
    }
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
            logInButton.setClickable(false);
            CloudDatabase.getInstance().emailSignIn(user, password, SignInFragment.this);
        }
        else{
            if(!Authentication.isUserNameValid(emailBox)){
                return;
            }
            logInButton.setClickable(false);
            CloudDatabase.getInstance().userNameSignIn(user, password,SignInFragment.this);
        }

    }
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        //This method is like startActivity but we only create it to get a result
        startActivityForResult(signInIntent, CONSTANTS.RESULT_ID.GOOGLE_LOG_IN);
    }
    //This is a callback method that gives us the return of the activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == CONSTANTS.RESULT_ID.GOOGLE_LOG_IN) {
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
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        listener.showProgressBar();
        CloudDatabase.getInstance().getAuth().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d("FIREBASE", "signInWithCredential:success");
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                //If new user delete
                                CloudDatabase.getInstance().deleteAuth().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        listener.googleSignUp(acct);
                                        listener.hideProgressBar();
                                    }
                                });
                            }
                            else {
                                FirebaseUser user = CloudDatabase.getInstance().getCurrentUser();
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
    @Override
    public void onAuthSuccessful(boolean success){
        if(success){
            listener.returnToEntry(CloudDatabase.getInstance().getCurrentUser());
        }
        else{
            logInButton.setClickable(true);
        }
    }

}