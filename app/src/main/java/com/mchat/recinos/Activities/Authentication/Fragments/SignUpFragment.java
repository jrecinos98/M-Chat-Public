package com.mchat.recinos.Activities.Authentication.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.mchat.recinos.CallBackInterfaces.DownloadResultCallback;
import com.mchat.recinos.CallBackInterfaces.ImeBackInterface;
import com.mchat.recinos.CallBackInterfaces.OnFocusChangeInterface;
import com.mchat.recinos.CustomViews.ViewListeners.UserNameImeBackListener;
import com.mchat.recinos.Tasks.ImageDownloadTask;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.CustomViews.KeyboardDismissEditText;
import com.mchat.recinos.Activities.Authentication.Interfaces.AuthInterface;
import com.mchat.recinos.R;
import com.mchat.recinos.Backend.Authentication;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.Constants;
import com.mchat.recinos.Util.Util;
import com.mchat.recinos.CustomViews.ViewListeners.UserNameOnFocusChangeListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;
//TODO Remove all direct use of Backend classes from the sign up process. Create a new ViewModel to handle the interactions.
public class SignUpFragment extends Fragment implements CloudDatabase.AuthInterface {
    private Context mContext;
    private EditText nameText;
    private KeyboardDismissEditText usernameText;
    private EditText emailText;
    private EditText passwordText;
    private EditText confirmPasswordText;
    private ImageView imageView;
    private Button   mButton;

    private GoogleSignInAccount gAccount;
    private AuthInterface parent;
    private String photoURL;
    private ByteArrayOutputStream imageBytes;

    //If a Google Account sign in is performed for the first time this constructor will be called
    //And the google credentials will be passed in.
    public SignUpFragment(AuthInterface parent, GoogleSignInAccount acct){
        this.parent = parent;
        gAccount = acct;
    }
    //If user is signing up with email then no credentials are provided.
    public SignUpFragment(AuthInterface parent){
        this.parent = parent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoURL = Constants.PUBLIC_DATA_ENTRY.DEFAULT_IMAGE_URL;
        setHasOptionsMenu(true);

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parent.showActionBar("Sign Up");
        View rootView = inflater.inflate(R.layout.frag_sign_up, container, false);

        if(gAccount != null){
            //Autofill fields if a google account has been provided.
            googleUI(rootView);
        }

        nameText = rootView.findViewById(R.id.sign_up_name);
        usernameText = rootView.findViewById(R.id.sign_up_username);
        emailText = rootView.findViewById(R.id.sign_up_email);
        passwordText = rootView.findViewById(R.id.sign_up_password);
        confirmPasswordText = rootView.findViewById(R.id.sign_up_confirm_password);
        imageView = rootView.findViewById(R.id.sign_up_image);
        mButton= rootView.findViewById(R.id.sign_up_btn);

        usernameText.setOnEditTextImeBackListener(new UserNameImeBackListener(onKeyboardDismiss()));
        usernameText.setOnFocusChangeListener(new UserNameOnFocusChangeListener(userNameFocusChange()));
        mButton.setOnClickListener((v)->  signUp());
        imageView.setOnClickListener((v)-> updateImage());
        return rootView;
    }

    //Called as soon as the activity is destroyed either by OS or user.
    //Helps ensure that no half-finished accounts are left.
    //TODO Devise a more foolproof way to ensure half-finished accounts are not left on device.
    @Override
    public void onDestroy(){
        googleSignOut();
        Log.d("SIGN_UP_FRAGMENT", "On destroyed called.");
        super.onDestroy();
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_login, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.reset){
            //TODO reset all the fields
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    //correctBitmap rotates bitmap if needed and scales it to a desired size.
                    Bitmap image = BitmapTransform.correctBitmap(mContext, BitmapFactory.decodeStream(imageStream), imageUri, 650);

                    //Operations for bitmap/image shown locally on device.
                    image = BitmapTransform.scaleUpBitmap(image, 250, mContext);
                    imageBytes = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 75, imageBytes);
                    BitmapTransform.updateRoundImage(imageView, mContext.getResources(), imageBytes.toByteArray());

                    //Operations for imageByte/image that gets uploaded to firebase storage. Compress the image by 30%
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

    @Override
    public void onAuthSuccessful(boolean success) {
        if (success) {
            parent.hideProgressBar();
            //Return to entry activity.
            parent.returnToEntry(Authentication.getInstance().getCurrentUser());
        } else {
            //Remove user from firebase auth to allow user to sign up again at later time.
            Authentication.getInstance().deleteAuth();
            Toast.makeText(mContext, "Sign Up Failed. Try Again.", Toast.LENGTH_LONG).show();
            mButton.setClickable(true);
        }
    }
    /**
     * Defines the UsernameOnFocusChange callback
     * @return The "SAM" callback interface method definition.
     */
    private OnFocusChangeInterface userNameFocusChange(){
        return (v, hasFocus)->{
            if(!hasFocus) {
                String username = ((KeyboardDismissEditText) v).getText().toString();
                if (!username.equals("") && Authentication.isUserNameValid(usernameText)) {
                    CloudDatabase.getInstance()
                            .getUserInfoFromUserName(username)
                            .addOnCompleteListener(this::handleUserNameTask);
                }
            }
        };
    }

    /**
     * Performs validation on the giv
     * @return The single method definition for the ImeBackInterface.
     */
    private ImeBackInterface onKeyboardDismiss(){
        return (ctrl, username)->{
            if(!username.equals("")) {
                if (Authentication.isUserNameValid(usernameText)) {
                    CloudDatabase.getInstance()
                            .getUserInfoFromUserName(username)
                            .addOnCompleteListener(this::handleUserNameTask);
                }
            }
        };
    }

    /**
     * Handles the task after a username request to database has concluded.
     * @param task
     */
    public void handleUserNameTask(Task<DocumentSnapshot> task){
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            //If not null then username already taken
            if (document != null && document.exists()) {
                usernameText.setError("Username taken");
            }
            /*
            else {
                //If null then it is available
            }
             */
        }
    }
    //Perform authentication depending on the method used to sign in.
    private void signUp(){
        if(gAccount != null) {
            googleSignUp();
        }
        else{
            emailSignUp();
        }
    }
    //This method populates data that can be extracted from a google account such as images and name.
    private void googleUI(View rootView){
        //Fetch the google image.
        photoURL = Util.getGoogleImageURL(gAccount);
        //Init a thread to handle downloading the image.
        ImageDownloadTask.DownloadImageTask download = new ImageDownloadTask.DownloadImageTask(ImageDownloadTask.DEFAULT_ID, onImageDownloadResult());
        download.execute(photoURL);

        //Set the name based on info from Google credentials.
        nameText.setText(gAccount.getDisplayName());

        rootView.findViewById(R.id.email_wrappper).setVisibility(View.GONE);

        //Remove the email and password field as not needed
        emailText.setVisibility(View.GONE);
        rootView.findViewById(R.id.password_wrappper).setVisibility(View.GONE);
        passwordText.setVisibility(View.GONE);
        rootView.findViewById(R.id.confirm_password_wrappper).setVisibility(View.GONE);
        confirmPasswordText.setVisibility(View.GONE);
    }

    //Finalize sign up using Google Credentials and create a user on the database.
    private void googleSignUp(){
        if(!Authentication.isNameValid(nameText) || !Authentication.isUserNameValid(usernameText)){
            return;
        }
        String name = nameText.getText().toString();
        String uName= usernameText.getText().toString();

        //Check whether the username is available or not. Reading from database is allowed since this information is public and does not require auth
        CloudDatabase.getInstance().getUserInfoFromUserName(uName).addOnCompleteListener((task)-> {
            if(task.isSuccessful()){
                parent.showProgressBar();
                DocumentSnapshot data = task.getResult();
                //If taken show error
                if(data != null && data.exists()){
                    usernameText.setError("Username taken");
                }
                else{
                    mButton.setClickable(false);
                    AuthCredential credential = GoogleAuthProvider.getCredential(gAccount.getIdToken(), null);
                    Authentication.getInstance().googleSignUp(credential).addOnCompleteListener((auth_task)-> {
                        //If user signUp is successful create a user in the database.
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

    //TODO send and require email verification.
    private void emailSignUp(){
        //Check all fields are valid.
        if (!Authentication.isNameValid(nameText) || !Authentication.isUserNameValid(usernameText) || !Authentication.isEmailValid(emailText) || !Authentication.isNewPasswordValid(passwordText, confirmPasswordText)){
            return;
        }
        parent.showProgressBar();
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
                    Authentication.getInstance().emailSignUp(email, password).addOnCompleteListener((auth_task)-> {
                        if (auth_task.isSuccessful()) {
                            Log.d("FIREBASE", "createUserWithEmail:success");
                            CloudDatabase.getInstance().createUser(mContext, CloudDatabase.getInstance().getUID(), name, userName, email, photoURL, imageBytes, SignUpFragment.this);
                            //createUser(name, username, photoURL);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FIREBASE", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Failed to create account with given email", Toast.LENGTH_LONG).show();
                            parent.hideProgressBar();
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
    //Callback for when the thread responsible for downloading image concludes. The image bytes are passed in as argument.

    public DownloadResultCallback onImageDownloadResult(){
       return (id,image)->{
           //id is not used in this case.
           imageBytes = new ByteArrayOutputStream(image.length);
           imageBytes.write(image, 0, image.length);
           BitmapTransform.updateRoundImage(imageView, mContext.getResources(), image);
       };
    }



    /**
     * Launches the image picker.
     */
    private void updateImage(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        //Only allow images.
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Constants.RESULT_ID.IMAGE);
    }

    //Sign out user if they are signing up with google account.
    private void googleSignOut(){
        //Disconnect the linked google account
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);
        mGoogleSignInClient.signOut();
    }

}
