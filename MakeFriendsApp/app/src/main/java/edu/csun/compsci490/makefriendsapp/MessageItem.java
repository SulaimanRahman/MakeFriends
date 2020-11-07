package edu.csun.compsci490.makefriendsapp;

public class MessageItem {
    private int mImgResource;
    private String mName, mMessageBody, mMessageTime, mType;

    public MessageItem(int imageRes, String messengerName, String messageBody, String messageTime, String type){
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

    public int getMessageImgResource() { return mImgResource; }

    public String getMessageName() { return mName; }

    public String getMessageBody() {
        return mMessageBody;
    }

    public String getMessageTime() {
        return mMessageTime;
    }

    public String getMessageType(){ return mType; }
}
