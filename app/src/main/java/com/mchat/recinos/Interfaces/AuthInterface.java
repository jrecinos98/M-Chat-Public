package com.mchat.recinos.Interfaces;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

public interface AuthInterface {
    void googleSignUp(GoogleSignInAccount acct);
    void emailSignUp();
    void returnToEntry(FirebaseUser account);
    void hideToolbar();
    void showProgressBar();
    void hideProgressBar();
    void showActionBar(String title);
}
