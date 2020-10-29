package edu.csun.compsci490.makefriendsapp;

public class UserSingleton {

    private static UserSingleton userSingleton = new UserSingleton();
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