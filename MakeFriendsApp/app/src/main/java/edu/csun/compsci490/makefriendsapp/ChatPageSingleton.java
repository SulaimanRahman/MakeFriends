package edu.csun.compsci490.makefriendsapp;

import java.util.ArrayList;

public class ChatPageSingleton {
    private static ChatPageSingleton chatPageSingleton = new ChatPageSingleton();

    //private ArrayList<>

    private ChatPageSingleton() {

    }

    public ChatPageSingleton getInstance() {
        return chatPageSingleton;
    }

    public void loadEverything() {

    }

}
