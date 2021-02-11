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
    private static String email;
    private static String password;
    public UserSingleton() {}

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

    public void reset() {//this is to clear all of the Singleton data
        profile = null;
        email = null;
        password = null;
        bio = null;
    }

}