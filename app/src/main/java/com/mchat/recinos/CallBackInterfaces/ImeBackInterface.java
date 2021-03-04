package com.mchat.recinos.CallBackInterfaces;

import com.mchat.recinos.CustomViews.KeyboardDismissEditText;

/**
 * "SAM" interface. Used to pass a callback method to receive keyboard inputs.
 */
public interface ImeBackInterface {
    void onImeBack(KeyboardDismissEditText ctrl, String text);
}
