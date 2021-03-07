package edu.csun.compsci490.makefriendsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<MessageItem> mMessageItems;
    private Context mContext;
    private static final int MESSAGE_RECEIVED_TYPE = 0;
    private static final int MESSAGE_SENT_TYPE = 1;
    private static final int RESULT_MESSAGE = 2;

    public MessageAdapter(Context context, ArrayList<MessageItem> messageItems){
        mContext = context;
        mMessageItems = messageItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = null;
        // check here the viewType and return RecyclerView.ViewHolder based on view type
        if (viewType == MESSAGE_RECEIVED_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_message, parent, false);
            return new ReceivedViewHolder(view);
        } else if (viewType == MESSAGE_SENT_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_message, parent, false);
            return new SentViewHolder(view);
        } else if (viewType == RESULT_MESSAGE){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_result_message, parent, false);
            return new ResultViewHolder(view);
        } else {
            return  null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final int messageType = getItemViewType(position);
        if(messageType == MESSAGE_RECEIVED_TYPE){
            ReceivedViewHolder receivedViewHolder = (ReceivedViewHolder) holder;
            Glide.with(((ReceivedViewHolder) holder).messageImage.getContext())
                    .load(mMessageItems.get(position).getMessageImgResource().toString())
                    .into(((ReceivedViewHolder) holder).messageImage);
            //receivedViewHolder.messageImage.setImageResource(mMessageItems.get(position).getMessageImgResource());
            receivedViewHolder.messageName.setText(mMessageItems.get(position).getMessageName());
            receivedViewHolder.messageBody.setText(mMessageItems.get(position).getMessageBody());
            receivedViewHolder.messageTime.setText(mMessageItems.get(position).getMessageTime());
        } else if(messageType == MESSAGE_SENT_TYPE) {
            SentViewHolder sentViewHolder = (SentViewHolder) holder;
            sentViewHolder.messageBody.setText(mMessageItems.get(position).getMessageBody());
            sentViewHolder.messageTime.setText(mMessageItems.get(position).getMessageTime());
        } else if(messageType == RESULT_MESSAGE){
            ResultViewHolder resultViewHolder = (ResultViewHolder) holder;
            resultViewHolder.messageBody.setText(mMessageItems.get(position).getMessageBody());
        }
    }

//    class CallActivity extends RecyclerView.ViewHolder{
//
//        Button button;
//
//        public CallActivity(@NonNull View itemView) {
//            super(itemView);
//            button = itemView.findViewById(R.id.btn_audio);
//        }
//    }

    class ReceivedViewHolder extends RecyclerView.ViewHolder {
        public TextView messageBody, messageName, messageTime;
        public ImageView messageImage;
        public ConstraintLayout parentLayout;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            messageBody = itemView.findViewById(R.id.received_message_body);
            messageName = itemView.findViewById(R.id.received_message_name);
            messageTime = itemView.findViewById(R.id.received_message_time);
            messageImage = itemView.findViewById(R.id.image_received_message_avatar);
            parentLayout = itemView.findViewById(R.id.message_received_parent_layout);
        }
    }
    class SentViewHolder extends RecyclerView.ViewHolder {
        public TextView messageBody, messageTime;
        public ConstraintLayout parentLayout;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            messageBody = itemView.findViewById(R.id.sent_message_body);
            messageTime = itemView.findViewById(R.id.sent_message_time);
            parentLayout = itemView.findViewById(R.id.message_sent_parent_layout);

        }

    }

    class ResultViewHolder extends RecyclerView.ViewHolder {
        public TextView messageBody, messageTime;
        public ConstraintLayout parentLayout;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            messageBody = itemView.findViewById(R.id.result_message_body);
        }

    }

    @Override
    public int getItemViewType(int position){
        int type = -1;
        if(mMessageItems.get(position).getMessageType() == "received"){
            type = 0;
        } else if (mMessageItems.get(position).getMessageType() == "sent"){
            type = 1;
        } else if (mMessageItems.get(position).getMessageType() == "result"){
            type = 2;
        }
        return type;
    }

    @Override
    public int getItemCount() {
        return mMessageItems.size();
    }

}

