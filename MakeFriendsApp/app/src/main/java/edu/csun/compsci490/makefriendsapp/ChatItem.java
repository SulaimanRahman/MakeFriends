package edu.csun.compsci490.makefriendsapp;

public class ChatItem {
    private int mImgResource;
    private String mName, mChatPreview;

    public ChatItem(int imageRes, String userName, String chatPreview){
        mImgResource = imageRes;
        mName = userName;
        mChatPreview = chatPreview;
    }

    public void enterChat(){
        // transition into messaging activity
        mName = "Go into chat activity";
    }

    public int getImgResource() {
        return mImgResource;
    }

    public String getChatPreview() {
        return mChatPreview;
    }

    public String getName() {
        return mName;
    }
}
