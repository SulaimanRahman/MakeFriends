package edu.csun.compsci490.makefriendsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import android.os.CountDownTimer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MessagingActivity extends AppCompatActivity {
    RecyclerView messagesRecyclerView;
    MessageAdapter messageAdapter;
    ArrayList<MessageItem> messageItems = new ArrayList<>();

    private TextView contactNameTextView;
    private ImageView contactPicImageView;
    private TextView noConversationExists;
    private ProgressBar progressBar;

    private EditText chatBoxEditText;
    private Button sendButton;

    private DatabaseManager databaseManager;
    private ChatSingleton chatSingleton;
    private UserSingleton userSingleton;
    private HashMap<String, Object> messages;
    private ArrayList<String> messagesKeySet;
    private HashMap<String, String> messagesTime;
    private String TAG = "MessagingActivity.java";

    private String userEmail;
    private String contactEmail;
    private boolean firstLoad;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    private Button callBtn;
    SinchClient sinchClient = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button hangupBtn;
    private String mUID;
    private String UID = "";
    private Call call;
    private TextView callState;
    private TextView caller;
    private Boolean callEstablished = false;
    ConstraintLayout MainLayout;
    View callLayout;
    MediaPlayer ringTone = null;
    private ImageView calleeImg;
    private Boolean noPick = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        contactNameTextView = findViewById(R.id.tv_userName);
        contactPicImageView = findViewById(R.id.img_userAvatar);
        noConversationExists = findViewById(R.id.no_conversation_exists);
        progressBar = findViewById(R.id.activity_messaging_progress_bar);
        chatBoxEditText = findViewById(R.id.edittext_chatbox);
        sendButton = findViewById(R.id.button_chatbox_send);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mUID = firebaseUser.getUid();
        callBtn = findViewById(R.id.btn_audio);
        //hangupBtn = findViewById(R.id.hangupButton);
        MainLayout = findViewById(R.id.MessagingLayout);
        LayoutInflater inflater = getLayoutInflater();
        callLayout = inflater.inflate(R.layout.activity_call_screen,MainLayout,false);
        hangupBtn = callLayout.findViewById(R.id.hangupButton);
        progressBar.setVisibility(View.VISIBLE);
        callState = callLayout.findViewById(R.id.callState);
        caller = callLayout.findViewById(R.id.caller);
        ringTone = MediaPlayer.create(getApplicationContext(),R.raw.phone_ring);
        calleeImg = callLayout.findViewById(R.id.calleePic);

        databaseManager = new DatabaseManager();
        chatSingleton = ChatSingleton.getInstance();

        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            String contactEmail = intent.getStringExtra("contactEmail");
            String contactFullName = intent.getStringExtra("contactFullName");
            Uri contactProfileUri = Uri.parse(intent.getStringExtra("contactProfileUri"));
            Log.d(TAG, "ContactProfileUri: " + intent.getStringExtra("contactProfileUri"));
            chatSingleton.setContactEmail(contactEmail);
            //Toast.makeText(getApplicationContext(),chatSingleton.getContactEmail(),Toast.LENGTH_LONG).show();
            chatSingleton.setContactName(contactFullName);
            chatSingleton.setContactProfilePicUri(contactProfileUri);
        }

        userSingleton = UserSingleton.getInstance();
        messages = new HashMap<>();
        messagesKeySet = new ArrayList<>();
        messagesTime = new HashMap<>();

        userEmail = userSingleton.getEmail();
        contactEmail = chatSingleton.getContactEmail();
        firstLoad = true;

        contactNameTextView.setText(chatSingleton.getContactName());
        Glide.with(contactPicImageView.getContext())
                .load(chatSingleton.getContactProfilePicUri().toString())
                .into(contactPicImageView);

        retrieveConversation();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String message = chatBoxEditText.getText().toString();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("H:mm a");
                LocalDateTime now = LocalDateTime.now();

                String time = dtf.format(now);

                messageItems.add(new MessageItem(message, time, "sent"));

                String documentPath = userEmail + "/Contacts/" + contactEmail + "/Chat";
                String timeDocumentPath = userEmail + "/Contacts/" + contactEmail + "/Chat Time";
                String moreInfoDocPath = userEmail + "/Contacts/" + contactEmail + "/More Info";
                int numberOfMessages = messageItems.size() - 1;
                databaseManager.createNewField(documentPath, "Me" + numberOfMessages, message);
                databaseManager.createNewField(timeDocumentPath, "Time" + numberOfMessages, time);
                databaseManager.updateTheField(moreInfoDocPath, "All Messages Been Read", "true");

                //saving the data at the contact database
                String contactDocumentPath = contactEmail + "/Contacts/" + userEmail + "/Chat";
                String contactTimeDocumentPath = contactEmail + "/Contacts/" + userEmail + "/Chat Time";
                final String contactMoreInfoDocPath = contactEmail + "/Contacts/" + userEmail + "/More Info";
                databaseManager.createNewField(contactDocumentPath, "Recipient" + numberOfMessages, message);
                databaseManager.createNewField(contactTimeDocumentPath, "Time" + numberOfMessages, time);
                databaseManager.updateTheField(contactMoreInfoDocPath, "All Messages Been Read", "false");

                databaseManager.getFieldValue(contactMoreInfoDocPath, "Number Of Unread Messages", new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        if (value == null) {
                            databaseManager.createNewField(contactMoreInfoDocPath, "Number Of Unread Messages", String.valueOf(1));
                        } else {
                            int numberOfUnreadMessages = Integer.valueOf(value.toString());
                            numberOfUnreadMessages++;
                            databaseManager.updateTheField(contactMoreInfoDocPath, "Number Of Unread Messages", String.valueOf(numberOfUnreadMessages));
                        }
                    }
                });

                messageAdapter.notifyDataSetChanged();
                chatBoxEditText.setText("");
                if (noConversationExists.getVisibility() == View.VISIBLE) {
                    noConversationExists.setVisibility(View.INVISIBLE);
                }
                Toast.makeText(MessagingActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Message Saved in the Database");
            }
        });

        sinchClient = Sinch.getSinchClientBuilder()
                .context(getApplicationContext())
                .applicationKey("218b541c-7b15-40e6-838c-27ea3bfda70d")
                .applicationSecret("GKKHHnhcIEWctgcgzrA75A==")
                .environmentHost("clientapi.sinch.com")
                .userId(mUID)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener(){

        });

        sinchClient.start();

        getUserID(new userCallback() {
            @Override
            public void isUserExist(boolean exist) {
                if(exist){
                    //Toast.makeText(getApplicationContext(),UID,Toast.LENGTH_LONG).show();
                    callUser(UID);
                }
            }
        });

        //Toast.makeText(getApplicationContext(),UID,Toast.LENGTH_LONG).show();



//        chatBoxEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (chatBoxEditText.getText().equals("")) {
//                    sendButton.setEnabled(false);
//                    Log.d(TAG, "Send Button disabled");
//                } else {
//                    sendButton.setEnabled(true);
//                    Log.d(TAG, "Send Button enabled");
//                }
//            }
//        });

//        chatBoxEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                if (keyEvent.getKeyCode() == KeyEvent.ACTION_DOWN){
//                    if (chatBoxEditText.getText().length() == 0) {
//                        sendButton.setEnabled(false);
//                        Log.d(TAG, "Send Button disabled");
//                    } else {
//                        sendButton.setEnabled(true);
//                        Log.d(TAG, "Send Button enabled");
//                    }
//                    return true;
//                }
//
//                return false;
//            }
//        });


    }
    private class SinchCallListener implements CallListener{


        @Override
        public void onCallProgressing(Call call) {

            //Toast.makeText(getApplicationContext(),"Calling!",Toast.LENGTH_LONG).show();
            callState.setText("connected");

        }

        @Override
        public void onCallEstablished(Call call) {
            //Toast.makeText(getApplicationContext(),"connected!",Toast.LENGTH_LONG).show();
            caller.setText(chatSingleton.getContactName() + " is talking");
        }

        @Override
        public void onCallEnded(Call callEnded) {
            Toast.makeText(getApplicationContext(),"Call ended",Toast.LENGTH_SHORT).show();
            call = null;
            callEnded.hangup();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }


    private class SinchCallClientListener implements CallClientListener {

        @Override
        public void onIncomingCall(CallClient callClient, final Call incomingCall) {
            Log.d(TAG, "Incoming call");
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(MessagingActivity.this).create();
            alertDialog.setTitle(chatSingleton.getContactName() +  "Calling");
            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "Reject", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    call.hangup();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(),"call connected!",Toast.LENGTH_LONG).show();
                    call = incomingCall;
                    call.answer();
                    callEstablished = true;
                    call.addCallListener(new SinchCallListener());
                    dialog.dismiss();
                    //setContentView(R.layout.activity_call_screen);
                    MainLayout.addView(callLayout);
                    hangupBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            call.hangup();
                        }
                    });
                    //Toast.makeText(getApplicationContext(),"call connected!",Toast.LENGTH_LONG).show();
                }
            });
            alertDialog.show();
        }
    }
    public void getUserID(userCallback callback)
    {
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setContentView(R.layout.activity_call_screen);
                db.collection(contactEmail).document("Profile")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot result = task.getResult();
                                UID = result.get("UID").toString();
                                callback.isUserExist(true);
                                //callUser(UID);
                                //Toast.makeText(getApplicationContext(),UID,Toast.LENGTH_LONG).show();
                                //Toast.makeText(getApplicationContext(),mUID,Toast.LENGTH_LONG).show();
                            }
                        });

            }
        });

    }

    public void callUser(String UID){
        if(this.call == null) {
            this.call = this.sinchClient.getCallClient().callUser(UID);
            this.call.addCallListener(new SinchCallListener());
            //setContentView(R.layout.activity_call_screen);
            //MainLayout.addView(callLayout);
            if (call != null) {
                long t = System.currentTimeMillis();
                long end = t + 15000;
                //Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_LONG).show();
                MainLayout.addView(callLayout);
                ringTone.start();
                Glide.with(getApplicationContext())
                        .load(chatSingleton.getContactProfilePicUri().toString())
                        .into(calleeImg);
                hangupBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        call.hangup();
                        callState.setText("disconnected");
                        ringTone.stop();
                        startActivity(new Intent(getApplicationContext(), MainNavigation.class));

                        //Toast.makeText(getApplicationContext(),"Call has ended!",Toast.LENGTH_LONG).show();
                        //MainLayout.addView(MainLayout);
                    }
                });
                if (callEstablished) {
                    callState.setText("connected");
                    caller.setText(chatSingleton.getContactName() + " is talking");
                    ringTone.stop();
                }
                new CountDownTimer(12000, 1000) {

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        noPick=true;
                        endCall(noPick);
                    }
                }.start();
//                Runnable myRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        while (!callEstablished) {
//                            if(System.currentTimeMillis() > end && !callEstablished) {
//                                break;
//                            }
//                        }
//                    }
//                };
//                Thread mThread = new Thread(myRunnable);
//                mThread.start();
//                if(!mThread.isAlive()){
//                    call.hangup();
//                    ringTone.stop();
//                    callState.setText("call ended");
//                    Toast.makeText(getApplicationContext(), "No Answer", Toast.LENGTH_LONG).show();
//                    startActivity(new Intent(getApplicationContext(), MainNavigation.class));
//                }
            }
        }
    }

    public void endCall(Boolean res){
        if(res){
            call.hangup();
            ringTone.stop();
            callState.setText("call ended");
            Toast.makeText(getApplicationContext(), "No Answer", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), MainNavigation.class));
        }
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

        String documentPath = userEmail + "/Contacts/" + contactEmail + "/Chat";

        databaseManager.getAllDocumentDataInHashMap(documentPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                messages = (HashMap) value;
                messagesKeySet.addAll(messages.keySet());
                messageItems.add(new MessageItem(messages.get("Note0").toString(), "result"));
                if (messages.size() < 2) {
                    noConversationExists.setVisibility(View.VISIBLE);
                }
                getMessagesTime();

            }
        });
    }

    private void getMessagesTime() {
        String documentPath = userEmail + "/Contacts/" + contactEmail + "/Chat Time";

        databaseManager.getAllDocumentDataInHashMap(documentPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                messagesTime = (HashMap) value;
                addMessagesToMessageItems();
            }
        });
    }

    private void addMessagesToMessageItems() {
        String contactName = chatSingleton.getContactName();
        Uri contactProfilePicUri = chatSingleton.getContactProfilePicUri();
        for (int i = 1; i < messages.size(); i++) {
            String messageKey = "";
            String message;
            String time = messagesTime.get("Time" + i);
            for (int j = 0; j < messagesKeySet.size(); j++) {
                if (messagesKeySet.get(j).equals("Me" + i) || messagesKeySet.get(j).equals("Recipient" + i)) {
                    messageKey = messagesKeySet.get(j);
                    break;
                }

            }

            message = (String) messages.get(messageKey);

            if (messageKey.contains("Recipient")) {
                messageItems.add(new MessageItem(contactProfilePicUri, contactName, message, time, "received"));
            } else if (messageKey.contains("Me")) {
                messageItems.add(new MessageItem(message, time, "sent"));
            } else if (messageKey.contains("file")) {

            }

        }

        checkIfConversationHasEndedOrAccountDeactivated();

    }

    private void checkIfConversationHasEndedOrAccountDeactivated() {

        if (chatSingleton.isConversationEnded()) {
            messageItems.add(new MessageItem("This Conversation has been ended", "result"));
            chatBoxEditText.setEnabled(false);
            sendButton.setEnabled(false);
            loadEverythingToTheGUI();
        } else if (chatSingleton.isOtherUserAccountDeactivated()) {
            messageItems.add(new MessageItem("The recipient has deleted their account", "result"));
            chatBoxEditText.setEnabled(false);
            sendButton.setEnabled(false);
            loadEverythingToTheGUI();
        } else {
            loadEverythingToTheGUI();
        }


//        String documentPath = userEmail + "/Contacts/" + contactEmail + "/More Info";
//
//        databaseManager.getAllDocumentDataInHashMap(documentPath, new FirebaseCallback() {
//            @Override
//            public void onCallback(Object value) {
//                HashMap<String, String> data = (HashMap) value;
//
//                if (data.get("Conversation Ended").equals("true")) {
//                    messageItems.add(new MessageItem("This Conversation has been ended", "result"));
//                    chatBoxEditText.setEnabled(false);
//                    sendButton.setEnabled(false);
//                    loadEverythingToTheGUI();
//                } else if (data.get("OtherUserDeactivatedAccount").equals("true")) {
//                    messageItems.add(new MessageItem("The recipient has deleted their account", "result"));
//                    chatBoxEditText.setEnabled(false);
//                    sendButton.setEnabled(false);
//                    loadEverythingToTheGUI();
//                } else {
//                    loadEverythingToTheGUI();
//                }
//            }
//        });
    }
    private void loadEverythingToTheGUI() {
        messagesRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_messages);
        messagesRecyclerView.setHasFixedSize(true);
        messageAdapter = new MessageAdapter(this, messageItems);
        messagesRecyclerView.setAdapter(messageAdapter);
        messagesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //set all the messages been viewed:
        String documentPath = userSingleton.getEmail() + "/Contacts/" + chatSingleton.getContactEmail() + "/More Info";
        databaseManager.updateTheField(documentPath, "All Messages Been Read", "true");
        try {
            databaseManager.updateTheField(documentPath, "Number Of Unread Messages", "0");
        } catch (Exception e) {
            //just leave this empty for now, to avoid errors for some of the accounts that don't have
            //the field "Number Of Unread Messages" on his database
        }


        progressBar.setVisibility(View.GONE);

        messagesRecyclerView.scrollToPosition(messageItems.size() - 1);
        setActionListenerToDatabase();

    }

    public void setActionListenerToDatabase() {
        String chatDocumentPath = userEmail + "/Contacts/" + contactEmail + "/Chat";

        databaseManager.getDocumentReference(chatDocumentPath).addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d(TAG, "Something went wrong: listen failed. ", error);
                }

                if (snapshot != null && snapshot.exists()) {
                    HashMap<String, Object> data = (HashMap) snapshot.getData();
                    ArrayList<String> dataKeys = new ArrayList<>();
                    dataKeys.addAll(data.keySet());

                    int totalKeys = dataKeys.size();
                    String lastKey = "";
                    for (int i = 0; i < dataKeys.size(); i++) {
                        if (dataKeys.get(i).contains("Me" + (totalKeys - 1)) || dataKeys.get(i).contains("Recipient" + (totalKeys - 1))) {
                            lastKey = dataKeys.get(i);
                            break;
                        }
                    }

                    if (lastKey.contains("Me")) {
                        //do nothing
                        firstLoad = false;
                        Log.d(TAG, "Last key: " + lastKey);
                    } else if (lastKey.contains("Recipient")){
                        if (firstLoad) {
                            firstLoad = false;
                        } else {
                            final String message = data.get(lastKey).toString();
                            final String contactName = chatSingleton.getContactName();
                            final Uri contactProfilePicUri = chatSingleton.getContactProfilePicUri();
                            String documentPath = userEmail + "/Contacts/" + contactEmail + "/Chat Time";
                            databaseManager.getFieldValue(documentPath, "Time" + (dataKeys.size() - 1), new FirebaseCallback() {
                                @Override
                                public void onCallback(Object value) {
                                    String time = value.toString();
                                    messageItems.add(new MessageItem(contactProfilePicUri, contactName, message, time, "received"));
                                    messageAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        noConversationExists.setVisibility(View.GONE);
                    } else {
                        firstLoad = false;
                    }
                } else {
                    Log.d(TAG, "Current Data: Null");
                }


            }
        });

        final String contactMoreInfoDocPath = userEmail + "/Contacts/" + contactEmail + "/More Info";
        databaseManager.getDocumentReference(contactMoreInfoDocPath).addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d(TAG, "Something went wrong: listen failed. ", error);
                }

                if (snapshot != null && snapshot.exists()) {
                    HashMap<String, Object> data = (HashMap) snapshot.getData();

                    if (data.get("Conversation Ended").equals("true")) {
                        messageItems.add(new MessageItem("This Conversation has been ended", "result"));
                        chatBoxEditText.setEnabled(false);
                        sendButton.setEnabled(false);
                        messageAdapter.notifyDataSetChanged();

                    } else if (data.get("OtherUserDeactivatedAccount").equals("true")) {
                        messageItems.add(new MessageItem("The recipient has deleted their account", "result"));
                        chatBoxEditText.setEnabled(false);
                        sendButton.setEnabled(false);
                        messageAdapter.notifyDataSetChanged();

                    } else if (data.get("All Messages Been Read").equals("false")) {
                        databaseManager.updateTheField(contactMoreInfoDocPath, "All Messages Been Read", "true");
                        try {
                            databaseManager.updateTheField(contactMoreInfoDocPath, "Number Of Unread Messages", "0");
                        } catch (Exception e) {
                            //just leave this empty for now, to avoid errors for some of the accounts that don't have
                            //the field "Number Of Unread Messages" on his database
                        }
                    }


                }
            }
        });
    }

}
