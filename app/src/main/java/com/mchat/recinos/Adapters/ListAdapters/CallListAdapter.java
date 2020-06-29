package com.mchat.recinos.Adapters.ListAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mchat.recinos.Backend.Entities.Call;
import com.mchat.recinos.R;

import java.util.ArrayList;

public class CallListAdapter extends RecyclerView.Adapter<CallListAdapter.ViewHolder> {
    private ArrayList<Call> calls;
    private Context mContext;

    public CallListAdapter(Context context){
        mContext = context;
        calls = new ArrayList<>();
    }
    //A wrapper around the view we will use as the items in list
    public static class ViewHolder extends RecyclerView.ViewHolder{
        //This is the view that will be used for items in the list
        private final View itemView;
        public ViewHolder(View v){
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
                }
            });
            itemView = v;
        }
        public View getListItem(){
            return itemView;
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

    }

    @Override
    public int getItemCount() {
        return calls.size();
    }

}

