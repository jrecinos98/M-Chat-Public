package com.mchat.recinos.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mchat.recinos.Activities.EntryActivity;
import com.mchat.recinos.AsyncTasks.ImageDownloadTask;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.CustomViews.EditTextBackEvent;
import com.mchat.recinos.Interfaces.AuthInterface;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.Authentication;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.CONSTANTS;
import com.mchat.recinos.Util.Util;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

//TODO Ensure that a user completes signUp when they leave app and shit. Also make sure to notify user if username is already taken
public class SignUpFragment extends Fragment implements ImageDownloadTask.DownloadResultCallback, CloudDatabase.AuthInterface {
    private Context mContext;
    private EditText nameText;
    private EditTextBackEvent usernameText;
    private EditText emailText;
    private EditText passwordText;
    private EditText confirmPasswordText;
    private ImageView imageView;
    private Button   mButton;

    private GoogleSignInAccount gAccount;
    private AuthInterface listener;
    private String photoURL;
    private ByteArrayOutputStream imageBytes;

    public SignUpFragment(AuthInterface listener, GoogleSignInAccount acct){
        this.listener = listener;
        gAccount = acct;
    }
    public SignUpFragment(AuthInterface listener){
        this.listener = listener;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoURL = CONSTANTS.PUBLIC_DATA_ENTRY.DEFAULT_IMAGE_URL;

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listener.showActionBar("Sign Up");
        View rootView = inflater.inflate(R.layout.frag_sign_up, container, false);
        nameText = rootView.findViewById(R.id.sign_up_name);
        usernameText = rootView.findViewById(R.id.sign_up_username);

        //usernameText.setOnEditTextImeBackListener(new UserNameAvailableListener(mButton));
        usernameText.setOnFocusChangeListener(new UserNameOnFocusChangeListener());

        emailText = rootView.findViewById(R.id.sign_up_email);
        passwordText = rootView.findViewById(R.id.sign_up_password);
        confirmPasswordText = rootView.findViewById(R.id.sign_up_confirm_password);
        imageView = rootView.findViewById(R.id.sign_up_image);
        mButton= rootView.findViewById(R.id.sign_up_btn);
        if(gAccount != null){
            googleUI(rootView);
        }

        mButton.setOnClickListener((v)->  signUp());
        imageView.setOnClickListener((v)-> updateImage());
        return rootView;
    }
    @Override
    public void onDestroy(){
        googleSignOut();
        super.onDestroy();
    }
    private void updateImage(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, CONSTANTS.RESULT_ID.IMAGE);
    }
    //TODO Pass the image that we obtain so it won't be fetched again. Send high quality one for profile picture
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                if(imageUri != null) {
                    final InputStream imageStream = mContext.getContentResolver().openInputStream(imageUri);
                    //correctBitmap rotates bitmap if needed and scales it to desired size.
                    Bitmap image = BitmapTransform.correctBitmap(mContext, BitmapFactory.decodeStream(imageStream), imageUri, 650);

                    //Scale bitmap to max size of imageview
                    image = BitmapTransform.scaleUpBitmap(image, 250, mContext);
                    imageBytes = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 95, imageBytes);
                    BitmapTransform.updateRoundImage(imageView, mContext.getResources(), imageBytes.toByteArray());

                    //Compress the image by 30%
                    imageBytes = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, imageBytes);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext, "No Image Selected",Toast.LENGTH_LONG).show();
        }
    }
    private void signUp(){
        if(gAccount != null) {
            googleSignUp();
        }
        else{
            emailSignUp();
        }
    }

    private void googleUI(View rootView){
        //Fetch the google image.
        photoURL = Util.getGoogleImageURL(gAccount);
        ImageDownloadTask.DownloadImageTask download = new ImageDownloadTask.DownloadImageTask(ImageDownloadTask.DEFAULT_ID, this);
        download.execute(photoURL);

        nameText.setText(gAccount.getDisplayName());
        rootView.findViewById(R.id.email_wrappper).setVisibility(View.GONE);
        emailText.setVisibility(View.GONE);
        rootView.findViewById(R.id.password_wrappper).setVisibility(View.GONE);
        passwordText.setVisibility(View.GONE);
        rootView.findViewById(R.id.confirm_password_wrappper).setVisibility(View.GONE);
        confirmPasswordText.setVisibility(View.GONE);
    }
    private void googleSignUp(){
        if(!Authentication.isNameValid(nameText) || !Authentication.isUserNameValid(usernameText)){
            return;
        }
        String name = nameText.getText().toString();
        String uName= usernameText.getText().toString();
        //Read is allowed since this information is public and does not require auth
        CloudDatabase.getInstance().getUserInfoFromUserName(uName).addOnCompleteListener((task)-> {
            if(task.isSuccessful()){
                listener.showProgressBar();
                DocumentSnapshot data = task.getResult();
                if(data != null && data.exists()){
                    usernameText.setError("Username taken");
                }
                else{
                    mButton.setClickable(false);
                    AuthCredential credential = GoogleAuthProvider.getCredential(gAccount.getIdToken(), null);
                    CloudDatabase.getInstance().googleSignUp(credential).addOnCompleteListener((auth_task)-> {
                        if(auth_task.isSuccessful()){
                            CloudDatabase.getInstance().createUser(mContext, CloudDatabase.getInstance().getUID(), name, uName,gAccount.getEmail(), photoURL, imageBytes, SignUpFragment.this);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FIREBASE", "signInWithCredential:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            mButton.setClickable(true);
                        }
                    });
                }
            }
        });
    }
    //TODO send email verification
    private void emailSignUp(){
        //Check all fields are valid.
        if (!Authentication.isNameValid(nameText) || !Authentication.isUserNameValid(usernameText) || !Authentication.isEmailValid(emailText) || !Authentication.isNewPasswordValid(passwordText, confirmPasswordText)){
            return;
        }
        listener.showProgressBar();
        String name = nameText.getText().toString();
        String userName = usernameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        //Read is allowed since this information is public and does not require auth
        CloudDatabase.getInstance().getUserInfoFromUserName(userName).addOnCompleteListener((task)-> {
            if (task.isSuccessful()) {
                DocumentSnapshot data = task.getResult();
                if (data != null && data.exists()) {
                    usernameText.setError("Username taken");
                } else {
                    mButton.setClickable(false);
                    //Toast.makeText(mContext, "Please wait while the account is being created.", Toast.LENGTH_LONG).show();
                    CloudDatabase.getInstance().emailSignUp(email, password).addOnCompleteListener((auth_task)-> {
                        if (auth_task.isSuccessful()) {
                            Log.d("FIREBASE", "createUserWithEmail:success");
                            CloudDatabase.getInstance().createUser(mContext, CloudDatabase.getInstance().getUID(), name, userName, email, photoURL, imageBytes, SignUpFragment.this);
                            //createUser(name, username, photoURL);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FIREBASE", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Failed to create account with given email", Toast.LENGTH_LONG).show();
                            listener.hideProgressBar();
                            mButton.setClickable(true);
                        }
                    });
                }
            }
            else{
                Log.d("SIGN_UP", "onComplete: Failed=" + task.getException().getMessage());
            }
        });
    }
    private void completeSignUp(){
        Intent toEntry = new Intent(mContext, EntryActivity.class);
        startActivity(toEntry);
    }
    @Override
    public void onImageDownloadResult(int id, byte[] image){
        //id is not used in this case.
        imageBytes = new ByteArrayOutputStream(image.length);
        imageBytes.write(image, 0, image.length);
        BitmapTransform.updateRoundImage(imageView, mContext.getResources(), image);
    }
    @Override
    public void onAuthSuccessful(boolean success){
        if(success){
            listener.hideProgressBar();
            completeSignUp();
        }else{
            //Remove auth to allow user to sign up again at later time.
            CloudDatabase.getInstance().deleteAuth();
            Toast.makeText(mContext, "Sign Up Failed. Try Again.", Toast.LENGTH_LONG).show();
            mButton.setClickable(true);
        }
    }
    private void googleSignOut(){
        //Disconnect the linked google account
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);
        mGoogleSignInClient.signOut();
    }


    public class UserNameOnFocusChangeListener implements View.OnFocusChangeListener{
        @Override
        public void onFocusChange(View v, boolean hasFocus){
            if(!hasFocus) {
                String username = ((EditTextBackEvent) v).getText().toString();
                if (Authentication.isUserNameValid(usernameText)) {
                    CloudDatabase.getInstance().getUserInfoFromUserName(username).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                //If not null then username already taken
                                if (document != null && document.exists()) {
                                    usernameText.setError("Username taken");
                                } else {
                                    //If null then it is available
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}
