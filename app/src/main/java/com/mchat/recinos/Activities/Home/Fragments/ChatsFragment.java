package com.mchat.recinos.Activities.Home.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mchat.recinos.Activities.Home.Adapters.ChatListAdapter;
import com.mchat.recinos.R;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public  class ChatsFragment extends Fragment {

    private RecyclerView mRecycleView;
    private LinearLayout linearLayout;
    private Context mContext;
    private ChatListAdapter mAdapter;
    public ChatsFragment(ChatListAdapter adapter){
        mAdapter= adapter;
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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.frag_chat_list, container, false);
        //TODO add decorator between list items.
        mRecycleView = rootView.findViewById(R.id.recycle_view);
        //Improves performance if size doesnt change.
        mRecycleView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(getActivity());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setAdapter(mAdapter);
        mRecycleView.setItemAnimator(new SlideInUpAnimator());

        return rootView;
    }

}
