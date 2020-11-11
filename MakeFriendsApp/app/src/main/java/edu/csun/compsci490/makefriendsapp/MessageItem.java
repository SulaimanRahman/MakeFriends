package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;

public class MessageItem {
    private Uri mImgResource;
    private String mName, mMessageBody, mMessageTime, mType;

    public MessageItem(Uri imageRes, String messengerName, String messageBody, String messageTime, String type){
        mImgResource = imageRes;
        mName = messengerName;
        mMessageBody = messageBody;
        mMessageTime = messageTime;
        mType = type;
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

    public Uri getMessageImgResource() { return mImgResource; }

    public String getMessageName() { return mName; }

    public String getMessageBody() {
        return mMessageBody;
    }

    public String getMessageTime() {
        return mMessageTime;
    }

    public String getMessageType(){ return mType; }
}
