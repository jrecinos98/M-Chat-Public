package com.mchat.recinos.Activities.Home.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.mchat.recinos.R;

public class EmptyHomeFragment extends Fragment {
    public static String CHAT = "chat";
    public static String CALL = "call";
    private Context mContext;
    private LinearLayout linearLayout;
    private String type;
    public EmptyHomeFragment(String type){
        this.type = type;
    }
    public EmptyHomeFragment(){
        this.type = "";
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        mContext = context;
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(type.equals(""))
            return new View(mContext);
        return type.equals(CHAT)? generateNoChatView() : generateNoCallView();
    }

    public View generateNoChatView(){
        LinearLayout.LayoutParams params;
        //Create Image View
        AppCompatImageView image = new AppCompatImageView(mContext);

        image.setImageResource(R.drawable.ic_textsms_black_24dp);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        //int width = displaymetrics.widthPixels;
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity= Gravity.CENTER;
        params.bottomMargin = 10;
        //Let these be 1/4 th of the display width
        params.height = width / 4;
        params.width = width / 4;
        image.setLayoutParams(params);

        //Create Text View
        TextView text = new TextView(mContext);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.rightMargin = width/8;
        params.leftMargin = width/8;
        params.topMargin = 10;
        text.setTextSize(15);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setTextColor(mContext.getResources().getColor(R.color.tab_inactive));
        text.setText(getResources().getText(R.string.no_chats_message));
        text.setLayoutParams(params);

        //Linear layout (holder)
        linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = RelativeLayout.CENTER_VERTICAL;
        //Add a margin on top of 1/4 th of the display height. This way it will be more or less centered.
        params.topMargin = height/4;
        linearLayout.setLayoutParams(params);
        linearLayout.addView(image);
        linearLayout.addView(text);

        return linearLayout;
    }
    public View generateNoCallView(){
        LinearLayout.LayoutParams params;
        //Create Image View
        AppCompatImageView image = new AppCompatImageView(mContext);

        image.setImageResource(R.drawable.ic_phone_black_24dp);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        //int width = displaymetrics.widthPixels;
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity= Gravity.CENTER;
        params.bottomMargin = 10;
        //Let these be 1/4 th of the display width
        params.height = width / 4;
        params.width = width / 4;
        image.setLayoutParams(params);

        //Create Text View
        TextView text = new TextView(mContext);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.rightMargin = width/8;
        params.leftMargin = width/8;
        params.topMargin = 10;
        text.setTextSize(15);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setTextColor(mContext.getResources().getColor(R.color.tab_inactive));
        text.setText(getResources().getText(R.string.no_calls_message));
        text.setLayoutParams(params);

        //Linear layout (holder)
        linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = RelativeLayout.CENTER_VERTICAL;
        //Add a margin on top of 1/4 th of the display height. This way it will be more or less centered.
        params.topMargin = height/4;
        linearLayout.setLayoutParams(params);
        linearLayout.addView(image);
        linearLayout.addView(text);

        return linearLayout;
    }
}

