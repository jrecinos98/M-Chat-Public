package com.mchat.recinos.CustomViews.ViewListeners;

import com.mchat.recinos.CallBackInterfaces.ImeBackInterface;
import com.mchat.recinos.CustomViews.KeyboardDismissEditText;

//This listener is called when the keyboard is dismissed
public class UserNameImeBackListener implements KeyboardDismissEditText.EditTextImeBackListener {
    private ImeBackInterface listener;
    public UserNameImeBackListener(ImeBackInterface listener){
        this.listener = listener;
    }
    @Override
    public void onImeBack(KeyboardDismissEditText ctrl, String username) {
        listener.onImeBack(ctrl, username);
    }
}
