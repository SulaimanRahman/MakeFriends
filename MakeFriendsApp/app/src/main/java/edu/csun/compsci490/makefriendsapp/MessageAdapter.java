package edu.csun.compsci490.makefriendsapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<MessageItem> mMessageItems;
    private Context mContext;
    private static final int MESSAGE_RECEIVED_TYPE = 0;
    private static final int MESSAGE_SENT_TYPE = 1;
    private static final int RESULT_MESSAGE = 2;

    private static final int IMAGE_SENT_TYPE = 3;
    private static final int IMAGE_RECEIVED_TYPE = 4;

    private static final int VIDEO_SENT_TYPE = 5;

    private static final int VIDEO_RECEIVED_TYPE = 6;

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
        } else if (viewType == RESULT_MESSAGE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_result_message, parent, false);
            return new ResultViewHolder(view);
        } else if (viewType == IMAGE_SENT_TYPE) {
            Log.d("MessagingAdapter", "Type is IMAGE_SENT_TYPE");
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_image, parent, false);
            return  new SentImageViewHolder(view);
        } else if (viewType == IMAGE_RECEIVED_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_image, parent, false);
            return new ReceivedImageViewHolder(view);
        } else if (viewType == VIDEO_SENT_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_video, parent, false);
            return  new SentVideoViewHolder(view);
        } else if (viewType == VIDEO_RECEIVED_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_video, parent, false);
            return new ReceivedVideoViewHolder(view);
        }
        else {
            Log.d("MessagingAdapter", "Type is none");
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final int messageType = getItemViewType(position);
        Log.d("Line 66", "Message Type is: " + messageType);
        if(messageType == MESSAGE_RECEIVED_TYPE) {
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
        } else if (messageType == IMAGE_SENT_TYPE) {//sending a image
            Log.d("Adapter", "Image Loaded");
            SentImageViewHolder sentImageViewHolder = (SentImageViewHolder) holder;
            sentImageViewHolder.imageTime.setText(mMessageItems.get(position).getMessageTime());
            Glide.with(((SentImageViewHolder) holder).imageView.getContext())
                    .load(mMessageItems.get(position).getMessageContentUri().toString())
                    .into(((SentImageViewHolder) holder).imageView);
        } else if (messageType == IMAGE_RECEIVED_TYPE) {
            ReceivedImageViewHolder receivedImageViewHolder = (ReceivedImageViewHolder) holder;
            Glide.with(((ReceivedImageViewHolder) holder).profileImage.getContext())
                    .load(mMessageItems.get(position).getMessageImgResource().toString())
                    .into(((ReceivedImageViewHolder) holder).profileImage);

            receivedImageViewHolder.messageName.setText(mMessageItems.get(position).getMessageName());
            receivedImageViewHolder.imageTime.setText(mMessageItems.get(position).getMessageTime());

            Glide.with(((ReceivedImageViewHolder) holder).imageView.getContext())
                    .load(mMessageItems.get(position).getMessageContentUri().toString())
                    .into(((ReceivedImageViewHolder) holder).imageView);
        } else if (messageType == VIDEO_SENT_TYPE) {
            SentVideoViewHolder sentVideoViewHolder = (SentVideoViewHolder) holder;
            sentVideoViewHolder.videoTime.setText(mMessageItems.get(position).getMessageTime());
            VideoView videoView = sentVideoViewHolder.videoView;
            sentVideoViewHolder.videoView.setVideoURI(mMessageItems.get(position).getMessageContentUri());
            MediaController mediaController = new MediaController(videoView.getContext());
            videoView.setMediaController(mediaController);
        } else if (messageType == VIDEO_RECEIVED_TYPE) {
            ReceivedVideoViewHolder receivedVideoViewHolder = (ReceivedVideoViewHolder) holder;
            Glide.with(((ReceivedVideoViewHolder) holder).profileImage.getContext())
                    .load(mMessageItems.get(position).getMessageImgResource().toString())
                    .into(((ReceivedVideoViewHolder) holder).profileImage);
            receivedVideoViewHolder.messageName.setText(mMessageItems.get(position).getMessageName());
            receivedVideoViewHolder.videoTime.setText(mMessageItems.get(position).getMessageTime());

            VideoView videoView = receivedVideoViewHolder.videoView;
            receivedVideoViewHolder.videoView.setVideoURI(mMessageItems.get(position).getMessageContentUri());
            MediaController mediaController = new MediaController(videoView.getContext());
            videoView.setMediaController(mediaController);
        }
        else {
            Log.d("MessagingAdapter", "OnBindNothing");
        }
    }

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

    class SentImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView imageTime;
        public ConstraintLayout parentLayout;

        public SentImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sent_image_body);
            imageTime = itemView.findViewById(R.id.sent_image_time);
            parentLayout = itemView.findViewById(R.id.image_sent_parent_layout);
        }
    }

    class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage, imageView;
        public TextView messageName, imageTime;
        public ConstraintLayout parentLayout;

        public ReceivedImageViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.image_recieved_profile_avatar);
            messageName = itemView.findViewById(R.id.received_image_name);
            imageView = itemView.findViewById(R.id.received_image_body);
            imageTime = itemView.findViewById(R.id.received_image_time);
            parentLayout = itemView.findViewById(R.id.image_received_parent_layout);
        }
    }

    class SentVideoViewHolder extends RecyclerView.ViewHolder {
        public VideoView videoView;
        public TextView videoTime;
        public ConstraintLayout parentLayout;

        public SentVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.sent_video_body);
            videoTime = itemView.findViewById(R.id.sent_video_time);
            parentLayout = itemView.findViewById(R.id.video_sent_parent_layout);
        }
    }

    class ReceivedVideoViewHolder extends RecyclerView.ViewHolder {
        public VideoView videoView;
        public ImageView profileImage;
        public TextView messageName, videoTime;
        public ConstraintLayout parentLayout;

        public ReceivedVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.video_recieved_profile_avatar);
            messageName = itemView.findViewById(R.id.received_video_name);
            videoView = itemView.findViewById(R.id.received_video_body);
            videoTime = itemView.findViewById(R.id.received_video_time);
            parentLayout = itemView.findViewById(R.id.video_received_parent_layout);
        }
    }

    class SentFileViewHolder extends RecyclerView.ViewHolder {
        public ImageView fileView;
        public TextView fileTime;
        public ConstraintLayout parentLayout;

        public SentFileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileTime = itemView.findViewById(R.id.sent_file_time);
            parentLayout = itemView.findViewById(R.id.file_sent_parent_layout);
        }
    }


    @Override
    public int getItemViewType(int position){
        int type = -1;
        if(mMessageItems.get(position).getMessageType() == "received"){
            type = 0;
        } else if (mMessageItems.get(position).getMessageType() == "sent"){
            Log.d("MessagingAdapter", "messaging type is sent");
            type = 1;
        } else if (mMessageItems.get(position).getMessageType() == "result"){
            type = 2;
        } else if (mMessageItems.get(position).getMessageType().equals("imageSent")) {
            Log.d("MessagingAdapter", "messaging type is image");
            type = 3;
        } else if (mMessageItems.get(position).getMessageType().equals("imageReceived")) {
            type = 4;
        } else if (mMessageItems.get(position).getMessageType().equals("videoSent")) {
            type = 5;
        } else if (mMessageItems.get(position).getMessageType().equals("videoReceived")) {
            type = 6;
        }
        else {
            Log.d("MessagingAdapter", "getItemViewType didn't work");
        }
        return type;
    }

    @Override
    public int getItemCount() {
        return mMessageItems.size();
    }

}

