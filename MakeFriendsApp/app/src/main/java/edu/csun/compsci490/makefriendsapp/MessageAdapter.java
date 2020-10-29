package edu.csun.compsci490.makefriendsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    private ArrayList<MessageItem> mMessageItems;

    public MessageAdapter(ArrayList<MessageItem> messageItems){
        mMessageItems = messageItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messageBody, messageName, messageTime;
        public ImageView messageImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageBody = itemView.findViewById(R.id.text_message_body);
            messageName = itemView.findViewById(R.id.text_message_name);
            messageTime = itemView.findViewById(R.id.text_message_time);
            messageImage = itemView.findViewById(R.id.image_message_profile);

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycle_view_chat_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageItem currentItem = mMessageItems.get(position);
        holder.messageImage.setImageResource(currentItem.getMessageImgResource());
        holder.messageName.setText(currentItem.getMessageName());
        holder.messageBody.setText(currentItem.getMessageBody());
        holder.messageTime.setText(currentItem.getMessageTime());
    }

    @Override
    public int getItemCount() {
        return mMessageItems.size();
    }

}

