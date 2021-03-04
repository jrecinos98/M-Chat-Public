package com.mchat.recinos.CustomViews.ViewListeners;

import android.view.View;

import com.mchat.recinos.CallBackInterfaces.OnFocusChangeInterface;

/**
 * onChangeListener for the username field box.
 * Used to verify whether a given username is available or taken.
 */
public class UserNameOnFocusChangeListener implements View.OnFocusChangeListener{
    OnFocusChangeInterface listener;
    public UserNameOnFocusChangeListener(OnFocusChangeInterface listener){
        this.listener = listener;
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus){
        listener.onFocusChange(v, hasFocus);
    }
}
