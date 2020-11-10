package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;

public class ChatItem {
    private Uri mImgResource;
    private String mName, mChatPreview;

    public ChatItem(Uri imageRes, String userName, String chatPreview){
        mImgResource = imageRes;
        mName = userName;
        mChatPreview = chatPreview;
    }

    public void enterChat(){
        // transition into messaging activity
        mName = "Go into chat activity";
    }

    public Uri getImgResource() {
        return mImgResource;
    }

    public String getChatPreview() {
        return mChatPreview;
    }

    public String getName() {
        return mName;
    }
}
