package edu.csun.compsci490.makefriendsapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatAdapter extends RecyclerView.Adapter {

    private static ArrayList<ChatItem> mChatItems;
    private OnChatClickListener mListener;
    private UserSingleton userSingleton;
    private DatabaseManager databaseManager;
    private BottomNavigationView bottomNavigationView;
    private BadgeDrawable badgeDrawable;

    public interface OnChatClickListener {
        void onChatClick(int position);
        void onDeleteClick(int position);

        void onBlockingClick(int position, ImageView icBlock);
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
        userSingleton = UserSingleton.getInstance();
        databaseManager = new DatabaseManager();

    }

//    public Context getContext() {
//        return this.getContext();
//    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        bottomNavigationView = parent.getRootView().findViewById(R.id.bottom_navigation_bar);
        badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.chatsFragment);
        badgeDrawable.setBackgroundColor(parent.getResources().getColor(R.color.notification_dot));
//        badgeDrawable.setBadgeTextColor(parent.getResources().getColor(R.color.white));
//        badgeDrawable.setNumber(numberOfUnreadMessages);
        badgeDrawable.setVisible(false);
        View view;
        if( viewType == 0){
            view = layoutInflater.inflate(R.layout.app_message_item, parent, false);
            return new AppMessageViewHolder(view);
        }
        view = layoutInflater.inflate(R.layout.recycle_view_chat_item, parent, false);

        return new ChatViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final ChatItem currentItem = mChatItems.get(position);

        if(currentItem.isAppMessage()){
            // bind to appMessageVH
            AppMessageViewHolder appMessageViewHolder = (AppMessageViewHolder)holder;
//            ((AppMessageViewHolder) holder).mAppMessageUserImage.setImageResource(R.drawable.ic_baseline_account_circle_24);
//            ((AppMessageViewHolder) holder).mAppMessage.setText("USER FOUND!");
        } else {
            // bind to chatVH
            ChatViewHolder chatViewHolder = (ChatViewHolder)holder;
            if (currentItem.getImgResource() == null) {
                if (currentItem.getContactEmail() == null) {
                    ((ChatViewHolder) holder).mChatImage.setVisibility(View.INVISIBLE);
                    ((ChatViewHolder) holder).imgChatAvator.setVisibility(View.INVISIBLE);
                    ((ChatViewHolder) holder).mDeleteIcon.setVisibility(View.INVISIBLE);
                    ((ChatViewHolder) holder).icBlock.setVisibility(View.INVISIBLE);
                    ((ChatViewHolder) holder).notificationDot.setVisibility(View.INVISIBLE);
                    ((ChatViewHolder) holder).mName.setText(currentItem.getName());
                    ((ChatViewHolder) holder).mChatPreview.setText(currentItem.getChatPreview());
                } else {
                    ((ChatViewHolder) holder).mChatImage.setImageResource(R.drawable.ic_launcher_foreground);

                    ((ChatViewHolder) holder).mName.setText(currentItem.getName());
                    ((ChatViewHolder) holder).mChatPreview.setText(currentItem.getChatPreview());

                    if (currentItem.isConversationEnded()) {
                        if (currentItem.isUserBlocked()) {
                            ((ChatViewHolder) holder).icBlock.setVisibility(View.INVISIBLE);
                        }
                    }
                    if (currentItem.isAllMessagesBeenRead()) {
                        badgeDrawable.setVisible(false);
                        ((ChatViewHolder) holder).notificationDot.setVisibility(View.INVISIBLE);
                    } else {
                        badgeDrawable.setVisible(true);
                        ((ChatViewHolder) holder).notificationDot.setVisibility(View.VISIBLE);
                    }
                }

            } else {
                Glide.with(((ChatViewHolder) holder).mChatImage.getContext())
                        .load(currentItem.getImgResource().toString())
                        .into(((ChatViewHolder) holder).mChatImage);
                ((ChatViewHolder) holder).mName.setText(currentItem.getName());
                ((ChatViewHolder) holder).mChatPreview.setText(currentItem.getChatPreview());

                if (currentItem.isConversationEnded()) {
                    if (currentItem.isUserBlocked()) {
                        ((ChatViewHolder) holder).icBlock.setVisibility(View.INVISIBLE);
                    }
                }

                if (currentItem.isAllMessagesBeenRead()) {
                    badgeDrawable.setVisible(false);
                    ((ChatViewHolder) holder).notificationDot.setVisibility(View.INVISIBLE);
                } else {
                    badgeDrawable.setVisible(true);
                    ((ChatViewHolder) holder).notificationDot.setVisibility(View.VISIBLE);
                }

            }

            //holder.mChatImage.setImageResource(currentItem.getImgResource());

            try {
                //adding action listener to last message
                String contactChatDocPath = userSingleton.getEmail() + "/Contacts/" + currentItem.getContactEmail() + "/Chat";
                databaseManager.getDocumentReference(contactChatDocPath).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.d("Chat Adapter", "Something went wrong: listen failed. ", error);
                        }

                        if (snapshot != null && snapshot.exists()) {
                            HashMap<String, Object> data = (HashMap) snapshot.getData();

                            ArrayList<String> dataKeys = new ArrayList<>();
                            dataKeys.addAll(data.keySet());

                            int totalKeys = dataKeys.size();
                            String lastKey = "";
                            for (int i = 0; i < dataKeys.size(); i++) {
                                if (dataKeys.get(i).contains("Me" + (totalKeys - 1)) || dataKeys.get(i).contains("Recipient" + (totalKeys - 1))) {
                                    lastKey = dataKeys.get(i);
                                    break;
                                } else if (dataKeys.get(i).equals("Note0")) {
                                    lastKey = dataKeys.get(i);
                                }
                            }

                            if (lastKey.equals("Note0")) {
                                ((ChatViewHolder) holder).mChatPreview.setText("Friendship Found!");
                            } else {
                                ((ChatViewHolder) holder).mChatPreview.setText(data.get(lastKey).toString());
                            }
//                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
//                                    .setSmallIcon(R.drawable.launcher_icon2)
//                                    .setContentTitle(currentItem.getName())
//                                    .setContentText("New message");
//
//                            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(getContext().NOTIFICATION_SERVICE);
//                            notificationManager.notify(0, builder.build());

                            //((ChatViewHolder) holder).notificationDot.setVisibility(View.VISIBLE);

                        }
                    }
                });

                //adding action listener to contact more info
                String moreInfoChatDocPath = userSingleton.getEmail() + "/Contacts/" + currentItem.getContactEmail() + "/More Info";
                databaseManager.getDocumentReference(moreInfoChatDocPath).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.d("Chat Adapter", "Something went wrong: listen failed. ", error);
                        }

                        if (snapshot != null && snapshot.exists()) {
                            try {
                                String conversationEndedStatus = snapshot.get("Conversation Ended").toString();
                                String conversationEndedByMeStatus = snapshot.get("Conversation Ended From My Side").toString();
                                String userBlockedStatus = snapshot.get("Blocked User").toString();
                                String otherUserDeactivatedAccount = snapshot.get("OtherUserDeactivatedAccount").toString();
                                String allMessagesBeenRead = snapshot.get("All Messages Been Read").toString();



                                if (conversationEndedStatus.equals("true")) {
                                    ((ChatViewHolder) holder).mChatPreview.setText("This conversation has ended");
                                    badgeDrawable.setVisible(true);
                                    ((ChatViewHolder) holder).notificationDot.setVisibility(View.VISIBLE);
                                }



                                if (otherUserDeactivatedAccount.equals("true")) {
                                    badgeDrawable.setVisible(true);
                                    ((ChatViewHolder) holder).notificationDot.setVisibility(View.VISIBLE);
                                    ((ChatViewHolder) holder).mChatPreview.setText("User has deactivated their account");
                                }

                                if (allMessagesBeenRead.equals("false")){
                                    badgeDrawable.setVisible(true);
                                    ((ChatViewHolder) holder).notificationDot.setVisibility(View.VISIBLE);
                                }

                                if (allMessagesBeenRead.equals("true")){
                                    badgeDrawable.setVisible(false);
                                    ((ChatViewHolder) holder).notificationDot.setVisibility(View.INVISIBLE);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });

            } catch (Exception e) {
                Log.d("Chat Adapter", "Thear are no Contacts for this user to set listeners to");
            }
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
        public ImageView mDeleteIcon, icBlock, notificationDot;
        public CardView imgChatAvator;



        public ChatViewHolder(@NonNull View itemView, final OnChatClickListener listener) {
            super(itemView);
            mChatImage = itemView.findViewById(R.id.chatImage);
            mName = itemView.findViewById(R.id.tvChatName);
            mChatPreview = itemView.findViewById(R.id.tvChatPreview);
            mDeleteIcon = itemView.findViewById(R.id.icDelete);
            icBlock = itemView.findViewById(R.id.icBlock);
            notificationDot = itemView.findViewById(R.id.notificationDot);
            imgChatAvator = itemView.findViewById(R.id.imgChatAvatar);



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
                        chatSingleton.setConversationEnded(mChatItems.get(position).isConversationEnded());
                        chatSingleton.setConversationEndedByMe(mChatItems.get(position).isConversationEndedByMe());
                        chatSingleton.setUserBlocked(mChatItems.get(position).isUserBlocked());
                        chatSingleton.setOtherUserAccountDeactivated(mChatItems.get(position).isOtherUserAccountDeactivated());

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
            
            icBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onBlockingClick(position, icBlock);
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
