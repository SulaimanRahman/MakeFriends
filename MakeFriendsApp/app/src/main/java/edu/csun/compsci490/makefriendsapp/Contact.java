package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;

public class Contact {
    private String contactName;
    private String contactEmail;
    private Uri contactProfilePicUri;

    public Contact() {

    }

    public Contact(String contactName, String contactEmail, Uri contactProfilePicUri) {
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactProfilePicUri = contactProfilePicUri;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public Uri getContactProfilePicUri() {
        return contactProfilePicUri;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setContactProfilePicUri(Uri contactProfilePicUri) {
        this.contactProfilePicUri = contactProfilePicUri;
    }
}
