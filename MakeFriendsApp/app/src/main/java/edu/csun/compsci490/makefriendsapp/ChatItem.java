package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;

public class ChatItem {
    private Uri mImgResource;
    private String mName, mChatPreview;
    private String contactEmail;

    public ChatItem(Uri imageRes, String userName, String chatPreview, String contactEmail){
        mImgResource = imageRes;
        mName = userName;
        mChatPreview = chatPreview;
        this.contactEmail = contactEmail;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
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
