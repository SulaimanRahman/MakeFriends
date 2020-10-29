package edu.csun.compsci490.makefriendsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessagingActivity extends AppCompatActivity {
    RecyclerView messagesRecyclerView;
    MessageAdapter messageAdapter;
    ArrayList<MessageItem> messageItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        retrieveConversation();

        // build recycler view
        final View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        messagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.chatRecyclerView);
        messagesRecyclerView.setHasFixedSize(true);
        messageAdapter = new MessageAdapter(messageItems);
        messagesRecyclerView.setAdapter(messageAdapter);
        messagesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    public void addMessage(int position){
        // firebase methods go here
    }

    private void retrieveConversation() {
        //initialize message items here
        messageItems = new ArrayList<>();
        messageItems.add(new MessageItem(R.drawable.ic_launcher_foreground, "John", "hello buddy", "11:52"));
    }
}
