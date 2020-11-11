package edu.csun.compsci490.makefriendsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private static ArrayList<ChatItem> mChatItems;
    private OnChatClickListener mListener;

    public interface OnChatClickListener {
        void onChatClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnChatClickListener(OnChatClickListener listener){
        mListener = listener;
    }

    public ChatAdapter(ArrayList<ChatItem> chatItems){
        mChatItems = chatItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mChatImage;
        public TextView mName, mChatPreview;
        public ImageView mDeleteIcon;

        public ViewHolder(@NonNull View itemView, final OnChatClickListener listener) {
            super(itemView);
            mChatImage = itemView.findViewById(R.id.chatImage);
            mName = itemView.findViewById(R.id.tvChatName);
            mChatPreview = itemView.findViewById(R.id.tvChatPreview);
            mDeleteIcon = itemView.findViewById(R.id.icDelete);

            final ChatSingleton chatSingleton = ChatSingleton.getInstance();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onChatClick(position);
                        }

                        chatSingleton.setContactEmail(mChatItems.get(position).getContactEmail());
                        chatSingleton.setContactName(mChatItems.get(position).getName());
                        chatSingleton.setContactProfilePicUri(mChatItems.get(position).getImgResource());



                    }
                }
            });

            mDeleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycle_view_chat_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatItem currentItem = mChatItems.get(position);

        if (currentItem.getImgResource() == null) {
            holder.mChatImage.setImageResource(R.drawable.ic_launcher_foreground);
        } else {
            Glide.with(holder.mChatImage.getContext())
                    .load(currentItem.getImgResource().toString())
                    .into(holder.mChatImage);
        }

        //holder.mChatImage.setImageResource(currentItem.getImgResource());
        holder.mName.setText(currentItem.getName());
        holder.mChatPreview.setText(currentItem.getChatPreview());

    }

    @Override
    public int getItemCount() {
        return mChatItems.size();
    }

}
