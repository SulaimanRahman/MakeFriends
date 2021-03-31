package edu.csun.compsci490.makefriendsapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

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

    private static final int IMAGE_SENT_TYPE = 3;
    private static final int IMAGE_RECEIVED_TYPE = 4;
    private static final int VIDEO_SENT_TYPE = 5;
    private static final int VIDEO_RECEIVED_TYPE = 6;

    private static final int PDF_SENT_TYPE = 7;
    private static final int PDF_RECEIVED_TYPE = 8;
    private static final int TEXT_SENT_TYPE = 9;
    private static final int TEXT_RECEIVED_TYPE = 10;
    private static final int WORD_SENT_TYPE = 11;
    private static final int WORD_RECEIVED_TYPE = 12;
    private static final int EXCEL_SENT_TYPE = 13;
    private static final int EXCEL_RECEIVED_TYPE = 14;
    private static final int POWERPOINT_SENT_TYPE = 15;
    private static final int POWERPOINT_RECEIVED_TYPE = 16;

    private static final int FILE_SENT_TYPE = 17;

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
//            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_image, parent, false);
//            return  new SentImageViewHolder(view);
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_file, parent, false);
            return  new SentFileViewHolder(view);
        } else if (viewType == IMAGE_RECEIVED_TYPE) {
//            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_image, parent, false);
//            return new ReceivedImageViewHolder(view);
            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_file, parent, false);
            return  new ReceivedFileViewHolder(view);
        } else if (viewType == VIDEO_SENT_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_file, parent, false);
            return  new SentFileViewHolder(view);
        } else if (viewType == VIDEO_RECEIVED_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_file, parent, false);
            return  new ReceivedFileViewHolder(view);
        } else if (viewType == PDF_SENT_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_file, parent, false);
            return  new SentFileViewHolder(view);
        } else if (viewType == PDF_RECEIVED_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_file, parent, false);
            return  new ReceivedFileViewHolder(view);
        } else if (viewType == TEXT_SENT_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_file, parent, false);
            return  new SentFileViewHolder(view);
        } else if (viewType == TEXT_RECEIVED_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_file, parent, false);
            return  new ReceivedFileViewHolder(view);
        } else if (viewType == WORD_SENT_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_file, parent, false);
            return  new SentFileViewHolder(view);
        } else if (viewType == WORD_RECEIVED_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_file, parent, false);
            return  new ReceivedFileViewHolder(view);
        } else if (viewType == EXCEL_SENT_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_file, parent, false);
            return  new SentFileViewHolder(view);
        } else if (viewType == EXCEL_RECEIVED_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_file, parent, false);
            return  new ReceivedFileViewHolder(view);
        } else if (viewType == POWERPOINT_SENT_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_file, parent, false);
            return  new SentFileViewHolder(view);
        } else if (viewType == POWERPOINT_RECEIVED_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_file, parent, false);
            return  new ReceivedFileViewHolder(view);
        }
        else if (viewType == FILE_SENT_TYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_file, parent, false);
            return new SentFileViewHolder(view);
        }
        else {
            Log.d("MessagingAdapter", "Type is none");
            return null;
        }

//                else if (viewType == VIDEO_SENT_TYPE) {
//            view = LayoutInflater.from(mContext).inflate(R.layout.item_sent_video, parent, false);
//            return  new SentVideoViewHolder(view);
//        } else if (viewType == VIDEO_RECEIVED_TYPE) {
//            view = LayoutInflater.from(mContext).inflate(R.layout.item_received_video, parent, false);
//            return new ReceivedVideoViewHolder(view);
//        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final int messageType = getItemViewType(position);
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
            final Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            final SentFileViewHolder sentFileViewHolder = (SentFileViewHolder) holder;
            sentFileViewHolder.fileTime.setText(messageTime);
            sentFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);
            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes
            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            sentFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            sentFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                sentFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                sentFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                sentFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            ImageView fileView = sentFileViewHolder.fileView;

            Glide.with(((SentFileViewHolder) holder).fileView.getContext())
                    .load(mMessageItems.get(position).getMessageContentUri().toString())
                    .into(((SentFileViewHolder) holder).fileView);

            fileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(sentFileViewHolder.fileView.getContext(), ImageViewPopActivity.class);
                    intent.putExtra("imageUri", contentUri.toString());
                    mContext.startActivity(intent);
                }
            });
            sentFileViewHolder.fileView.setScaleType(ImageView.ScaleType.MATRIX);

        } else if (messageType == IMAGE_RECEIVED_TYPE) {
            Uri contactProfilePicUri = mMessageItems.get(position).getMessageImgResource();
            String contactName = mMessageItems.get(position).getMessageName();
            final Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            final ReceivedFileViewHolder receivedFileViewHolder = (ReceivedFileViewHolder) holder;
            Glide.with(receivedFileViewHolder.profileView.getContext())
                    .load(contactProfilePicUri).into(receivedFileViewHolder.profileView);
            receivedFileViewHolder.contactName.setText(contactName);
            receivedFileViewHolder.fileTime.setText(messageTime);

            receivedFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            receivedFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                receivedFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                receivedFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                receivedFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            Glide.with(receivedFileViewHolder.fileView.getContext())
                    .load(contentUri.toString()).into(receivedFileViewHolder.fileView);

            receivedFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);

            receivedFileViewHolder.fileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(receivedFileViewHolder.fileView.getContext(), ImageViewPopActivity.class);
                    intent.putExtra("imageUri", contentUri.toString());
                    mContext.startActivity(intent);
                }
            });
            receivedFileViewHolder.fileView.setScaleType(ImageView.ScaleType.MATRIX);

        } else if (messageType == VIDEO_SENT_TYPE) {
            final Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            final SentFileViewHolder sentFileViewHolder = (SentFileViewHolder) holder;
            sentFileViewHolder.fileTime.setText(messageTime);

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            sentFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            sentFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                sentFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                sentFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                sentFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            Glide.with(((SentFileViewHolder) holder).fileView.getContext())
                    .load(contentUri.toString())
                    .into(((SentFileViewHolder) holder).fileView);
            sentFileViewHolder.fileView.setScaleType(ImageView.ScaleType.MATRIX);
            sentFileViewHolder.mediaPlayerIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(sentFileViewHolder.fileView.getContext(), VideoViewPopActivity.class);
                    intent.putExtra("videoUri", contentUri.toString());
                    mContext.startActivity(intent);
                }
            });
        } else if (messageType == VIDEO_RECEIVED_TYPE) {
            Uri contactProfilePicUri = mMessageItems.get(position).getMessageImgResource();
            String contactName = mMessageItems.get(position).getMessageName();
            final Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            final ReceivedFileViewHolder receivedFileViewHolder = (ReceivedFileViewHolder) holder;
            Glide.with(receivedFileViewHolder.profileView.getContext())
                    .load(contactProfilePicUri).into(receivedFileViewHolder.profileView);
            receivedFileViewHolder.contactName.setText(contactName);
            receivedFileViewHolder.fileTime.setText(messageTime);

            receivedFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            receivedFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                receivedFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                receivedFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                receivedFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            Glide.with(receivedFileViewHolder.fileView.getContext())
                    .load(contentUri.toString()).into(receivedFileViewHolder.fileView);
            receivedFileViewHolder.fileView.setScaleType(ImageView.ScaleType.MATRIX);
            receivedFileViewHolder.mediaPlayerIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(receivedFileViewHolder.mediaPlayerIcon.getContext(), VideoViewPopActivity.class);
                    intent.putExtra("videoUri", contentUri.toString());
                    mContext.startActivity(intent);
                }
            });
        } else if (messageType == PDF_SENT_TYPE) {
            Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            SentFileViewHolder sentFileViewHolder = (SentFileViewHolder) holder;
            sentFileViewHolder.fileTime.setText(messageTime);
            sentFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            sentFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            sentFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                sentFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                sentFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                sentFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            sentFileViewHolder.fileView.setImageResource(R.drawable.pdf_file_icon);
        } else if (messageType == PDF_RECEIVED_TYPE) {
            Uri contactProfilePicUri = mMessageItems.get(position).getMessageImgResource();
            String contactName = mMessageItems.get(position).getMessageName();
            final Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            final ReceivedFileViewHolder receivedFileViewHolder = (ReceivedFileViewHolder) holder;
            Glide.with(receivedFileViewHolder.profileView.getContext())
                    .load(contactProfilePicUri).into(receivedFileViewHolder.profileView);
            receivedFileViewHolder.contactName.setText(contactName);
            receivedFileViewHolder.fileTime.setText(messageTime);

            receivedFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            receivedFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                receivedFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                receivedFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                receivedFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            receivedFileViewHolder.fileView.setImageResource(R.drawable.pdf_file_icon);

            receivedFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);

        } else if (messageType == TEXT_SENT_TYPE) {
            Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            SentFileViewHolder sentFileViewHolder = (SentFileViewHolder) holder;
            sentFileViewHolder.fileTime.setText(messageTime);
            sentFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            sentFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            sentFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                sentFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                sentFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                sentFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            sentFileViewHolder.fileView.setImageResource(R.drawable.text_file_icon);
        } else if (messageType == TEXT_RECEIVED_TYPE) {
            Uri contactProfilePicUri = mMessageItems.get(position).getMessageImgResource();
            String contactName = mMessageItems.get(position).getMessageName();
            final Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            final ReceivedFileViewHolder receivedFileViewHolder = (ReceivedFileViewHolder) holder;
            Glide.with(receivedFileViewHolder.profileView.getContext())
                    .load(contactProfilePicUri).into(receivedFileViewHolder.profileView);
            receivedFileViewHolder.contactName.setText(contactName);
            receivedFileViewHolder.fileTime.setText(messageTime);

            receivedFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            receivedFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                receivedFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                receivedFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                receivedFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            receivedFileViewHolder.fileView.setImageResource(R.drawable.text_file_icon);

            receivedFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);
        } else if (messageType == WORD_SENT_TYPE) {
            Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            SentFileViewHolder sentFileViewHolder = (SentFileViewHolder) holder;
            sentFileViewHolder.fileTime.setText(messageTime);
            sentFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            sentFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            sentFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                sentFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                sentFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                sentFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            sentFileViewHolder.fileView.setImageResource(R.drawable.word_file_icon);
        } else if (messageType == WORD_RECEIVED_TYPE) {
            Uri contactProfilePicUri = mMessageItems.get(position).getMessageImgResource();
            String contactName = mMessageItems.get(position).getMessageName();
            final Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            final ReceivedFileViewHolder receivedFileViewHolder = (ReceivedFileViewHolder) holder;
            Glide.with(receivedFileViewHolder.profileView.getContext())
                    .load(contactProfilePicUri).into(receivedFileViewHolder.profileView);
            receivedFileViewHolder.contactName.setText(contactName);
            receivedFileViewHolder.fileTime.setText(messageTime);

            receivedFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            receivedFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                receivedFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                receivedFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                receivedFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            receivedFileViewHolder.fileView.setImageResource(R.drawable.word_file_icon);

            receivedFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);
        } else if (messageType == EXCEL_SENT_TYPE) {
            Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            SentFileViewHolder sentFileViewHolder = (SentFileViewHolder) holder;
            sentFileViewHolder.fileTime.setText(messageTime);
            sentFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            sentFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            sentFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                sentFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                sentFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                sentFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            sentFileViewHolder.fileView.setImageResource(R.drawable.xlsx_file_icon);
        } else if (messageType == EXCEL_RECEIVED_TYPE) {
            Uri contactProfilePicUri = mMessageItems.get(position).getMessageImgResource();
            String contactName = mMessageItems.get(position).getMessageName();
            final Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            final ReceivedFileViewHolder receivedFileViewHolder = (ReceivedFileViewHolder) holder;
            Glide.with(receivedFileViewHolder.profileView.getContext())
                    .load(contactProfilePicUri).into(receivedFileViewHolder.profileView);
            receivedFileViewHolder.contactName.setText(contactName);
            receivedFileViewHolder.fileTime.setText(messageTime);

            receivedFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            receivedFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                receivedFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                receivedFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                receivedFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            receivedFileViewHolder.fileView.setImageResource(R.drawable.xlsx_file_icon);

            receivedFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);
        } else if (messageType == POWERPOINT_SENT_TYPE) {
            Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            SentFileViewHolder sentFileViewHolder = (SentFileViewHolder) holder;
            sentFileViewHolder.fileTime.setText(messageTime);
            sentFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            sentFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            sentFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                sentFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                sentFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                sentFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            sentFileViewHolder.fileView.setImageResource(R.drawable.powerpoint_file_icon);
        } else if (messageType == POWERPOINT_RECEIVED_TYPE) {
            Uri contactProfilePicUri = mMessageItems.get(position).getMessageImgResource();
            String contactName = mMessageItems.get(position).getMessageName();
            final Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            final ReceivedFileViewHolder receivedFileViewHolder = (ReceivedFileViewHolder) holder;
            Glide.with(receivedFileViewHolder.profileView.getContext())
                    .load(contactProfilePicUri).into(receivedFileViewHolder.profileView);
            receivedFileViewHolder.contactName.setText(contactName);
            receivedFileViewHolder.fileTime.setText(messageTime);

            receivedFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            receivedFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                receivedFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                receivedFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                receivedFileViewHolder.sizeTextView.setText(size + " Bytes");
            }

            receivedFileViewHolder.fileView.setImageResource(R.drawable.powerpoint_file_icon);

            receivedFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);
        } else if (messageType == FILE_SENT_TYPE) {
            Uri contentUri = mMessageItems.get(position).getMessageContentUri();
            String messageTime = mMessageItems.get(position).getMessageTime();

            SentFileViewHolder sentFileViewHolder = (SentFileViewHolder) holder;
            sentFileViewHolder.fileTime.setText(messageTime);
            sentFileViewHolder.mediaPlayerIcon.setVisibility(View.INVISIBLE);

            String name = mMessageItems.get(position).getFileName();
            double size = mMessageItems.get(position).getFileSize();//gives you size in bytes

            String linkToDownload = String.format("<a href=\"%s\">%s</a>", contentUri, name);

            sentFileViewHolder.downloadTextView.setText(Html.fromHtml(linkToDownload));
            sentFileViewHolder.downloadTextView.setMovementMethod(LinkMovementMethod.getInstance());

            //setting the size of the file
            if (size > 1000 && size < 1000000) {
                size = size/1000;
                sentFileViewHolder.sizeTextView.setText(size + " kB");
            } else if (size >= 1000000) {
                size = size/1000000;
                sentFileViewHolder.sizeTextView.setText(size + " MB");
            } else {
                sentFileViewHolder.sizeTextView.setText(size + " Bytes");
            }
            //need add something to the textview below this comment
        } else {

        }

//        else if (messageType == VIDEO_SENT_TYPE) {
//            SentVideoViewHolder sentVideoViewHolder = (SentVideoViewHolder) holder;
//            sentVideoViewHolder.videoTime.setText(mMessageItems.get(position).getMessageTime());
//            VideoView videoView = sentVideoViewHolder.videoView;
//            sentVideoViewHolder.videoView.setVideoURI(mMessageItems.get(position).getMessageContentUri());
//            MediaController mediaController = new MediaController(videoView.getContext());
//            videoView.setMediaController(mediaController);
//        } else if (messageType == VIDEO_RECEIVED_TYPE) {
//            ReceivedVideoViewHolder receivedVideoViewHolder = (ReceivedVideoViewHolder) holder;
//            Glide.with(((ReceivedVideoViewHolder) holder).profileImage.getContext())
//                    .load(mMessageItems.get(position).getMessageImgResource().toString())
//                    .into(((ReceivedVideoViewHolder) holder).profileImage);
//            receivedVideoViewHolder.messageName.setText(mMessageItems.get(position).getMessageName());
//            receivedVideoViewHolder.videoTime.setText(mMessageItems.get(position).getMessageTime());
//
//            VideoView videoView = receivedVideoViewHolder.videoView;
//            receivedVideoViewHolder.videoView.setVideoURI(mMessageItems.get(position).getMessageContentUri());
//            MediaController mediaController = new MediaController(videoView.getContext());
//            videoView.setMediaController(mediaController);
//        }
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
        public ImageView fileView, mediaPlayerIcon;
        public TextView fileTime, fileInfoTextView, downloadTextView, sizeTextView;
        public ImageButton downloadButton;
        public ConstraintLayout parentLayout;

        public SentFileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileView = itemView.findViewById(R.id.sent_file_body);
            mediaPlayerIcon = itemView.findViewById(R.id.media_player_icon);
            fileTime = itemView.findViewById(R.id.sent_file_time);
            fileInfoTextView = itemView.findViewById(R.id.download_textview);
            downloadTextView = itemView.findViewById(R.id.download_textview);
            sizeTextView = itemView.findViewById(R.id.file_size_textview);
            downloadButton = itemView.findViewById(R.id.download_button);
            parentLayout = itemView.findViewById(R.id.file_sent_parent_layout);
        }
    }

    class ReceivedFileViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileView, fileView, mediaPlayerIcon;
        public TextView contactName, fileTime, downloadTextView, sizeTextView;
        public ImageButton downloadButton;
        public ConstraintLayout parentLayout;

        public ReceivedFileViewHolder(@NonNull View itemView) {
            super(itemView);
            profileView = itemView.findViewById(R.id.file_received_profile_avatar);
            fileView = itemView.findViewById(R.id.received_file_body);
            mediaPlayerIcon = itemView.findViewById(R.id.media_player_icon_for_received_file);
            contactName = itemView.findViewById(R.id.file_received_name);
            fileTime = itemView.findViewById(R.id.file_received_time);
            downloadTextView = itemView.findViewById(R.id.download_textview_for_received_file);
            sizeTextView = itemView.findViewById(R.id.file_size_textview_for_received_file);
            downloadButton = itemView.findViewById(R.id.download_button_for_received_file);
            parentLayout = itemView.findViewById(R.id.file_received_parent_layout);
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
        } else if (mMessageItems.get(position).getMessageType().equals("imageSent")) {
            type = 3;
        } else if (mMessageItems.get(position).getMessageType().equals("imageReceived")) {
            type = 4;
        } else if (mMessageItems.get(position).getMessageType().equals("videoSent")) {
            type = 5;
        } else if (mMessageItems.get(position).getMessageType().equals("videoReceived")) {
            type = 6;
        } else if (mMessageItems.get(position).getMessageType().equals("pdfSent")) {
            type = 7;
        } else if (mMessageItems.get(position).getMessageType().equals("pdfReceived")) {
            type = 8;
        } else if (mMessageItems.get(position).getMessageType().equals("textSent")) {
            type = 9;
        } else if (mMessageItems.get(position).getMessageType().equals("textReceived")) {
            type = 10;
        } else if (mMessageItems.get(position).getMessageType().equals("wordSent")) {
            type = 11;
        } else if (mMessageItems.get(position).getMessageType().equals("wordReceived")) {
            type = 12;
        } else if (mMessageItems.get(position).getMessageType().equals("excelSent")) {
            type = 13;
        } else if (mMessageItems.get(position).getMessageType().equals("excelReceived")) {
            type = 14;
        } else if (mMessageItems.get(position).getMessageType().equals("powerpointSent")) {
            type = 15;
        } else if (mMessageItems.get(position).getMessageType().equals("powerpointReceived")) {
            type = 16;
        }
        else if (mMessageItems.get(position).getMessageType().equals("fileSent")) {
            type = 17;
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

