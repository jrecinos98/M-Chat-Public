package com.mchat.recinos.Adapters.ListAdapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.mchat.recinos.Backend.Entities.ImageMessage;
import com.mchat.recinos.Backend.Entities.Message;
import com.mchat.recinos.Backend.Entities.TextMessage;
import com.mchat.recinos.R;
import com.mchat.recinos.Util.BitmapTransform;
import com.mchat.recinos.Util.CONSTANTS;
import com.mchat.recinos.Util.IO;
import com.mchat.recinos.Util.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MessageListAdapter extends BaseAdapter {

    private List<Message> messages;
    private WeakReference<Context> contextRef;
    private Bitmap avatarImage;
    private String uid;

    public MessageListAdapter(Context context, Bitmap image, String uid) {
        this.contextRef = new WeakReference<>(context);
        this.avatarImage = image;
        messages = new ArrayList<>();
        this.uid = uid;
    }

    public void add(Message message) {
        this.messages.add(message);
        notifyDataSetChanged(); // to render the list we need to notify
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if(contextRef.get() != null) {
            LayoutInflater messageInflater = (LayoutInflater) contextRef.get().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            Message m = messages.get(i);
            if(m.getType() == CONSTANTS.MESSAGE_DATA_TYPES.TEXT) {
                TextMessageViewHolder holder = new TextMessageViewHolder();
                TextMessage message = m.toTextMessage();
                //Util.logMessage(message);
                if (message.getBelongsToUser()) { // this message was sent by us so let's create a basic chat bubble on the right
                    convertView = messageInflater.inflate(R.layout.my_chat_bubble, null);
                    holder.messageBody = convertView.findViewById(R.id.message_body);
                    holder.messageBody.setText(message.getData());
                    convertView.setTag(holder);
                } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
                    convertView = messageInflater.inflate(R.layout.other_message_bubble, null);
                    holder.avatar = convertView.findViewById(R.id.avatar);
                    holder.messageBody = convertView.findViewById(R.id.message_body);
                    holder.messageBody.setText(message.getData());
                    RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(contextRef.get().getResources(), avatarImage);
                    dr.setCircular(true);
                    holder.avatar.setBackground(dr);
                    convertView.setTag(holder);
                }
            }
            else if (m.getType() == CONSTANTS.MESSAGE_DATA_TYPES.IMAGE){
                ImageMessageViewHolder imgHolder = new ImageMessageViewHolder();
                ImageMessage message =  m.toImageMessage();
                if(message.getImage() == null){
                    Log.d("ADAPTER", ""+message.getData());
                    //final InputStream imageStream = contextRef.get().getContentResolver().openInputStream(Uri.parse(message.getUri()));
                    BitmapTransform.SerialBitmap serialBitmap =  IO.readInternalFile(contextRef.get(),IO.formatSubDirectory(CONSTANTS.INTERNAL_DIRECTORY.MESSAGE_IMAGES, String.valueOf(message.getOwner_cid())),   message.getUri());
                    if(serialBitmap != null)
                        message.setImage(serialBitmap.getBitmap());
                }
                convertView = message.getBelongsToUser()? messageInflater.inflate(R.layout.my_image_message, null): messageInflater.inflate(R.layout.other_image_message, null);
                imgHolder.image = convertView.findViewById(R.id.img_msg);
                imgHolder.extension =  message.getExtension();
                //If image not found then use a default image instead
                if(message.getImage() != null) {
                    imgHolder.image.setImageBitmap(message.getImage());
                }
                if (!message.getBelongsToUser()) { // this message was sent by us so let's create a basic chat bubble on the right
                    imgHolder.avatar = convertView.findViewById(R.id.avatar);
                    RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(contextRef.get().getResources(), avatarImage);
                    dr.setCircular(true);
                    imgHolder.avatar.setBackground(dr);
                }
                convertView.setTag(imgHolder);

            }
        }
        return convertView;
    }
    public void setMessages(List<Message> messages){
        this.messages = messages;
        notifyDataSetChanged();
    }

    public static class TextMessageViewHolder {
        public ImageView avatar;
        public TextView messageBody;
    }
    public static class ImageMessageViewHolder {
        public ImageView avatar;
        public ImageView image;
        public String extension;
    }

}


