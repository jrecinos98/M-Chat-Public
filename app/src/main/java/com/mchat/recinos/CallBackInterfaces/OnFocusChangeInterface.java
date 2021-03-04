package com.mchat.recinos.CallBackInterfaces;

import android.view.View;

/**
 * This is a "SAM" type interface. Essentially it contains only one abstract method.
 * It allows us to pass the method it requires as a lambda function to any method that expects
 * a parameter of type OnFocusChangeInterface.
 */
public interface OnFocusChangeInterface {
    /**
     * Callback method called when passed to a view's onFocusChange event.
     * @param v The view whose focus changed.
     * @param hasFocus Whether the view currently has focus or if it lost it.
     */
    void onFocusChange(View v, boolean hasFocus);
}
