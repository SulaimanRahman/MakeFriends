package edu.csun.compsci490.makefriendsapp;

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

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    //private String fullName;
    private String major;
    private static String email;
    private static String password;

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