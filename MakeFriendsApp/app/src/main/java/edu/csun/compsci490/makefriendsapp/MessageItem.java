package edu.csun.compsci490.makefriendsapp;

public class MessageItem {
    private int mImgResource;
    private String mName, mMessageBody, mMessageTime;

    public MessageItem(int imageRes, String messengerName, String messageBody, String messageTime){
        mImgResource = imageRes;
        mName = messengerName;
        mMessageBody = messageBody;
        mMessageTime = messageTime;
    }


    public int getMessageImgResource() {
        return mImgResource;
    }

    public String getMessageName() {
        return mName;
    }

    public String getMessageBody() {
        return mMessageBody;
    }

    public String getMessageTime() {
        return mMessageTime;
    }
}
