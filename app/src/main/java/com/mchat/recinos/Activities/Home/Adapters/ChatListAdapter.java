package com.mchat.recinos.Activities.Home.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.mchat.recinos.Activities.Chat.MessagingActivity;
import com.mchat.recinos.Backend.Client.Client;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Backend.ViewModels.HomeViewModel;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.Constants;
import com.mchat.recinos.Util.Util;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private WeakReference<Context> contextRef;
    private List<Chat> chats;
    private HomeViewModel homeViewModel;
    public ChatListAdapter(Context context, HomeViewModel viewModel){
        contextRef = new WeakReference<>(context);
        chats = new ArrayList<>();
        homeViewModel = viewModel;
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
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        LinearLayout container = viewHolder.getListItem().findViewById(R.id.name_preview_wrapper);
        container.setOnClickListener(new ChatClickListener(position));
        if(!chats.isEmpty()) {
            Chat chat = chats.get(position);
            ImageView img = viewHolder.getListItem().findViewById(R.id.contact_image);
            TextView name = viewHolder.getListItem().findViewById(R.id.contact_name);
            TextView preview = viewHolder.getListItem().findViewById(R.id.msg_preview);
            TextView indicator = viewHolder.getListItem().findViewById(R.id.indicator);
            if (contextRef.get() != null) {
                RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(contextRef.get().getResources(), chat.getImageBitmap());
                dr.setCircular(true);
                img.setBackground(dr);
            }
            if(chats.get(position).getUnreadCount() != 0){
                indicator.setText(String.valueOf(chats.get(position).getUnreadCount()));
                indicator.setVisibility(View.VISIBLE);
            }
            else{
                indicator.setVisibility(View.INVISIBLE);
            }
            name.setText(chat.getTitle());
            preview.setText(chat.getPreview());
        }
    }
    @Override
    public int getItemCount() {
        return chats.size();
    }
    class ChatClickListener implements View.OnClickListener{
        int position;
        public ChatClickListener(int pos){
            position= pos;
        }
        @Override
        public void onClick(View v){
            Context mContext = contextRef.get();
            if(mContext != null) {
                if(!chats.isEmpty()) {
                    Chat chat = chats.get(position);
                    Intent toChat = new Intent(mContext, MessagingActivity.class);
                    Contact friend = new Contact(chat.getContact());
                    //Need to specify serializable since contact also implements parcelable
                    toChat.putExtra(Constants.INTENT_ID.CONTACT_INFO, (Serializable) friend);
                    toChat.putExtra(Constants.INTENT_ID.CHAT_ID, chat.getCid());
                    //Necessary for an Activity to be launched outside the context of an activity (The application context or a service)
                    //toChat.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if(chat.getUnreadCount() > 0){
                        homeViewModel.updateChatPreview(chat.getUID(), chat.getPreview(), Util.getTime(), 0);
                        chat.resetUnread();
                        //May not be called on time
                        //notifyDataSetChanged();
                    }
                    Client.CURRENT_CHAT = chat.getCid();
                    mContext.startActivity(toChat);
                }
            }
        }
    }
    public void setChats(List<Chat> chats){
        this.chats = chats;
        //instead of using this we can simply update the item that changed with:
        //notifyItemChanged(pos);
        notifyDataSetChanged();
    }
    static class ImageClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){

        }
    }
}
