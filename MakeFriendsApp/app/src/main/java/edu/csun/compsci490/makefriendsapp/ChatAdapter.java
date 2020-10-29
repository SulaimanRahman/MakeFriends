package edu.csun.compsci490.makefriendsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private ArrayList<ChatItem> mChatItems;

    public ChatAdapter(ArrayList<ChatItem> chatItems){
        mChatItems = chatItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mChatImage;
        public TextView mName, mChatPreview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mChatImage = itemView.findViewById(R.id.chatImage);
            mName = itemView.findViewById(R.id.tvChatName);
            mChatPreview = itemView.findViewById(R.id.tvChatPreview);
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
        ChatItem currentItem = mChatItems.get(position);
        holder.mChatImage.setImageResource(currentItem.getImgResource());
        holder.mName.setText(currentItem.getName());
        holder.mChatPreview.setText(currentItem.getChatPreview());
    }

    @Override
    public int getItemCount() {
        return mChatItems.size();
    }

}
