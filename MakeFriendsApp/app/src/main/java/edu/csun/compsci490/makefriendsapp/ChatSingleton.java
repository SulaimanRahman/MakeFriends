package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;

public class ChatSingleton {
    private static ChatSingleton chatSingleton = new ChatSingleton();
    private String contactEmail;
    private Uri contactProfilePicUri;
    private String contactName;
    private String UID;




    private boolean isConversationEnded;
    private boolean isConversationEndedByMe;

    private boolean isUserBlocked;
    private boolean isOtherUserAccountDeactivated;

    private ChatSingleton() {}

    public static ChatSingleton getInstance() {
        return chatSingleton;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public Uri getContactProfilePicUri() {
        return contactProfilePicUri;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setContactProfilePicUri(Uri contactProfilePicUri) {
        this.contactProfilePicUri = contactProfilePicUri;
    }
    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
    public void setContactName(String contactName) {
        this.contactName = contactName;
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
}
