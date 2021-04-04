package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;

public class MessageItem {
    private Uri mImgResource;
    private String mName, mMessageBody, mMessageTime, mType;
    private Uri messageContentUri;
    private String fileName;
    private long fileSize;

    public MessageItem(Uri imageRes, String messengerName, String messageBody, String messageTime, String type){
        mImgResource = imageRes;
        mName = messengerName;
        mMessageBody = messageBody;
        mMessageTime = messageTime;
        mType = type;
    }

    public MessageItem(Uri imageRes, String messengerName, Uri messageContentUri, String messageTime, String type, String fileName, long fileSize){
        mImgResource = imageRes;
        mName = messengerName;
        this.messageContentUri = messageContentUri;
        mMessageTime = messageTime;
        mType = type;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
    public MessageItem(String messageBody, String messageTime, String type){
        mMessageBody = messageBody;
        mMessageTime = messageTime;
        mType = type;
    }

    public MessageItem(String searchResultMessage, String type){
        mMessageBody = searchResultMessage;
        mType = type;
    }

    public MessageItem(Uri messageContentUri, String messageTime, String type, String fileName, long fileSize) {
        this.messageContentUri = messageContentUri;
        mMessageTime = messageTime;
        mType = type;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public Uri getMessageContentUri() {return messageContentUri;}

    public Uri getMessageImgResource() { return mImgResource; }

    public String getMessageName() { return mName; }

    public String getMessageBody() {
        return mMessageBody;
    }

    public String getMessageTime() {
        return mMessageTime;
    }

    public String getMessageType(){ return mType; }

    public String getFileName() {return fileName;}

    public long getFileSize() {return fileSize;}
}
