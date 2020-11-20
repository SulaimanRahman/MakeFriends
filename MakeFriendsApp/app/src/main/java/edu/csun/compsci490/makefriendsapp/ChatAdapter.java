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

public class ChatAdapter extends RecyclerView.Adapter {

    private static ArrayList<ChatItem> mChatItems;
    private OnChatClickListener mListener;

    public interface OnChatClickListener {
        void onChatClick(int position);
        void onDeleteClick(int position);
    }

    @Override
    public int getItemViewType(int position) {
        if(mChatItems.get(position).isAppMessage()){
            return 0;
        }
        return 1;
    }

    public void setOnChatClickListener(OnChatClickListener listener){
        mListener = listener;
    }

    public ChatAdapter(ArrayList<ChatItem> chatItems){
        mChatItems = chatItems;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;
        if( viewType == 0){
            view = layoutInflater.inflate(R.layout.app_message_item, parent, false);
            return new AppMessageViewHolder(view);
        }
        view = layoutInflater.inflate(R.layout.recycle_view_chat_item, parent, false);
        return new ChatViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem currentItem = mChatItems.get(position);

        if(currentItem.isAppMessage()){
            // bind to appMessageVH
            AppMessageViewHolder appMessageViewHolder = (AppMessageViewHolder)holder;
//            ((AppMessageViewHolder) holder).mAppMessageUserImage.setImageResource(R.drawable.ic_baseline_account_circle_24);
//            ((AppMessageViewHolder) holder).mAppMessage.setText("USER FOUND!");
        } else {
            // bind to chatVH
            ChatViewHolder chatViewHolder = (ChatViewHolder)holder;
            if (currentItem.getImgResource() == null) {
                ((ChatViewHolder) holder).mChatImage.setImageResource(R.drawable.ic_launcher_foreground);
            } else {
                Glide.with(((ChatViewHolder) holder).mChatImage.getContext())
                        .load(currentItem.getImgResource().toString())
                        .into(((ChatViewHolder) holder).mChatImage);
            }

            //holder.mChatImage.setImageResource(currentItem.getImgResource());
            ((ChatViewHolder) holder).mName.setText(currentItem.getName());
            ((ChatViewHolder) holder).mChatPreview.setText(currentItem.getChatPreview());
        }
    }

    @Override
    public int getItemCount() {
        return mChatItems.size();
    }

    /************************************************************************************/
    class ChatViewHolder extends RecyclerView.ViewHolder {
        public ImageView mChatImage;
        public TextView mName, mChatPreview;
        public ImageView mDeleteIcon;

        public ChatViewHolder(@NonNull View itemView, final OnChatClickListener listener) {
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

    class AppMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView mAppMessageUserImage, mAppMessageIcon;
        TextView mAppMessage;

        public AppMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mAppMessageUserImage = itemView.findViewById(R.id.app_message_userImage);
            mAppMessage = itemView.findViewById(R.id.app_message);
            mAppMessageIcon = itemView.findViewById(R.id.app_message_icon);
        }
    }

}
