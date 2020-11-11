package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;

public class ChatSingleton {
    private static ChatSingleton chatSingleton = new ChatSingleton();
    private String contactEmail;
    private Uri contactProfilePicUri;
    private String contactName;

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

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}
