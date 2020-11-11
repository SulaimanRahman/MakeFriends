package edu.csun.compsci490.makefriendsapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;

public class MessagingActivity extends AppCompatActivity {
    RecyclerView messagesRecyclerView;
    MessageAdapter messageAdapter;
    ArrayList<MessageItem> messageItems = new ArrayList<>();
    private DatabaseManager databaseManager;
    private ChatSingleton chatSingleton;
    private UserSingleton userSingleton;
    HashMap<String, Object> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        databaseManager = new DatabaseManager();
        chatSingleton = ChatSingleton.getInstance();
        userSingleton = UserSingleton.getInstance();
        messages = new HashMap<>();

        retrieveConversation();
        messagesRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_messages);
        messagesRecyclerView.setHasFixedSize(true);
        messageAdapter = new MessageAdapter(this, messageItems);
        messagesRecyclerView.setAdapter(messageAdapter);
        messagesRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }


    // position will always be last since newest messages are added at bottom
    public void addNewMessage(int position){
        // firebase methods go here

        /*
        for received messages
        messageItems.add(new MessageItem(image, userName, messageBody, time, "received"));


        for sent messages
        messageItems.add(new MessageItem(messageBody, time, "received"));

        be sure to notify adapter or insertions or deletions or data changes
        messageAdapter.notify...
        */


    }

    private void retrieveConversation() {
        //initialize message items here
        //messageItems.add(new MessageItem("This message will explain why the user was linked with another user", "result"));
        //messageItems.add(new MessageItem(R.drawable.ic_launcher_foreground, "John", "hello buddy", "11:52", "received"));
        //messageItems.add(new MessageItem("whats up John", "11:54", "sent"));
        //messageItems.add(new MessageItem(R.drawable.ic_launcher_foreground, "John", "nothing much just out here trying to makefriends bro", "11:58", "received"));

        String userEmail = userSingleton.getEmail();
        String contactEmail = chatSingleton.getContactEmail();
        String documentPath = userEmail + "/Contacts/" + contactEmail + "/Chat";

        databaseManager.getAllDocumentDataInHashMap(documentPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                messages = (HashMap) value;
                addMessagesToMessageItems();
            }
        });

    }

    private void addMessagesToMessageItems() {

    }
}
