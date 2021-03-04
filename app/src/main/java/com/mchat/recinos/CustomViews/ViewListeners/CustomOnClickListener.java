package com.mchat.recinos.CustomViews.ViewListeners;

import android.view.View;

import com.mchat.recinos.CallBackInterfaces.OnClickListenerInterface;

/**
 * A customized class to accept onClickListen events.
 * The listener can then be called to perform a variable onClick method.
 */
public class CustomOnClickListener implements View.OnClickListener{
    OnClickListenerInterface listener;
    public CustomOnClickListener(OnClickListenerInterface listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
    }

}
