package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;

public class ChatItem {
    private Uri mImgResource;
    private String mName, mChatPreview;
    private String contactEmail;
    public boolean mItemType;
    private String mAppMessage;
    private int mAppMessageImage;

    private boolean isConversationEnded;
    private boolean isConversationEndedByMe;

    private boolean isUserBlocked;
    private boolean isOtherUserAccountDeactivated;

    private boolean allMessagesBeenRead;

    public ChatItem(int imageRes, String appMessage){
        mAppMessageImage = imageRes;
        mAppMessage = appMessage;
        mItemType = true;
    }

    public ChatItem(Uri imageRes, String userName, String chatPreview, String contactEmail, boolean isConversationEnded, boolean isConversationEndedByMe, boolean isUserBlocked, boolean isOtherUserAccountDeactivated, boolean allMessagesBeenRead){
        mImgResource = imageRes;
        mName = userName;
        mChatPreview = chatPreview;
        this.contactEmail = contactEmail;
        mItemType = false;
        this.isConversationEnded = isConversationEnded;
        this.isConversationEndedByMe = isConversationEndedByMe;
        this.isUserBlocked = isUserBlocked;
        this.isOtherUserAccountDeactivated = isOtherUserAccountDeactivated;
        this.allMessagesBeenRead = allMessagesBeenRead;
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

    public boolean isConversationEnded() {
        return isConversationEnded;
    }

    public boolean isConversationEndedByMe() {
        return isConversationEndedByMe;
    }

    public void setConversationEnded(boolean conversationEnded) {
        isConversationEnded = conversationEnded;
    }

    public void setConversationEndedByMe(boolean conversationEndedByMe) {
        isConversationEndedByMe = conversationEndedByMe;
    }

    public boolean isUserBlocked() {
        return isUserBlocked;
    }

    public boolean isOtherUserAccountDeactivated() {
        return isOtherUserAccountDeactivated;
    }

    public void setUserBlocked(boolean userBlocked) {
        isUserBlocked = userBlocked;
    }

    public void setOtherUserAccountDeactivated(boolean otherUserAccountDeactivated) {
        isOtherUserAccountDeactivated = otherUserAccountDeactivated;
    }

    public boolean isAllMessagesBeenRead() {
        return allMessagesBeenRead;
    }

    public void setAllMessagesBeenRead(boolean allMessagesBeenRead) {
        this.allMessagesBeenRead = allMessagesBeenRead;
    }
}
