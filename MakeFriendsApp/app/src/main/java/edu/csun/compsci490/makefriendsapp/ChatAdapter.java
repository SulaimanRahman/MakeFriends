package edu.csun.compsci490.makefriendsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView chatAvatar;
        public TextView name, chatPreview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatAvatar = itemView.findViewById(R.id.imgChatAvatar);
            name = itemView.findViewById(R.id.tvChatName);
            chatPreview = itemView.findViewById(R.id.tvChatPreview);
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

    }

    @Override
    public int getItemCount() {
        return 10;
    }

}
