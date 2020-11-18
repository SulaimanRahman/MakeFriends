package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;
import android.widget.ImageView;

public class UserSingleton {

    private static UserSingleton userSingleton = new UserSingleton();


    private String bio;

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }



    private String profile;

    public Uri getUserProfileImg() {
        return userProfileImg;
    }

    public void setUserProfileImg(Uri userProfileImg) {
        this.userProfileImg = userProfileImg;
    }

    private Uri userProfileImg;

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    //private String fullName;
    private String firstName;
    private String lastName;
    private String major;
    private static String email;
    private static String password;

//    public String getUserImg() {
//        return userImg;
//    }

//    public void setUserImg(String userImg) {
//        this.userImg = userImg;
//    }

    //private String userImg;
    private UserSingleton() {}

    public static UserSingleton getInstance() {
        return userSingleton;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}