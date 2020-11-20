package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;

public class ChatItem {
    private Uri mImgResource;
    private String mName, mChatPreview;
    private String contactEmail;
    public boolean mItemType;
    private String mAppMessage;
    private int mAppMessageImage;

    public ChatItem(int imageRes, String appMessage){
        mAppMessageImage = imageRes;
        mAppMessage = appMessage;
        mItemType = true;
    }

    public ChatItem(Uri imageRes, String userName, String chatPreview, String contactEmail){
        mImgResource = imageRes;
        mName = userName;
        mChatPreview = chatPreview;
        this.contactEmail = contactEmail;
        mItemType = false;
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

    public boolean isAppMessage(){
        //if chat item is an app message type
        if(mItemType){
            return true;
        }
        return false;
    }
}
