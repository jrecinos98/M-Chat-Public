package com.mchat.recinos.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;



public class KeyboardDismissEditText extends AppCompatEditText {

    private EditTextImeBackListener mOnImeBack;

    public KeyboardDismissEditText(Context context) {
        super(context);
    }

    public KeyboardDismissEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardDismissEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBack != null)
                mOnImeBack.onImeBack(this, this.getText().toString());
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
        mOnImeBack = listener;
    }
    public interface EditTextImeBackListener {
        void onImeBack(KeyboardDismissEditText ctrl, String text);
    }

}

