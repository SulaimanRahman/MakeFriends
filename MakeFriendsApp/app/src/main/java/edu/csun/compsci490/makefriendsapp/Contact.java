package edu.csun.compsci490.makefriendsapp;


public class Contact {
    private String contactName;
    private String contactMajor;

    public String getContactBio() {
        return contactBio;
    }

    public void setContactBio(String contactBio) {
        this.contactBio = contactBio;
    }

    public String getContactInterest() {
        return contactInterest;
    }

    public void setContactInterest(String contactInterest) {
        this.contactInterest = contactInterest;
    }

    private String contactBio;
    private String contactInterest;

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    private String userImg;

    public Contact() {

    }

    public Contact(String contactName, String contactMajor, String userImg,String contactBio,String contactInterest) {
        this.contactName = contactName;
        this.contactMajor = getContactMajor();
        this.userImg = userImg;
        this.contactBio = contactBio;
        this.contactInterest = contactInterest;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactMajor() {
        return contactMajor;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setContactMajor(String contactEmail) {
        this.contactMajor = contactEmail;
    }

}
