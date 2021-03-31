package edu.csun.compsci490.makefriendsapp;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.auth.User;

import java.lang.reflect.Array;
import java.net.URLConnection;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

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
    private ImageButton attachButton;

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
        attachButton = findViewById(R.id.btn_attach);

        progressBar.setVisibility(View.VISIBLE);

        databaseManager = new DatabaseManager();
        chatSingleton = ChatSingleton.getInstance();

        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            String contactEmail = intent.getStringExtra("contactEmail");
            String contactFullName = intent.getStringExtra("contactFullName");
            Uri contactProfileUri = Uri.parse(intent.getStringExtra("contactProfileUri"));
            Log.d(TAG, "ContactProfileUri: " + intent.getStringExtra("contactProfileUri"));
            chatSingleton.setContactEmail(contactEmail);
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
                int numberOfMessages = messageItems.size() - 2;
                databaseManager.createNewField(documentPath, "Me" + numberOfMessages, message);
                databaseManager.createNewField(timeDocumentPath, "Time" + numberOfMessages, time);
                databaseManager.updateTheField(moreInfoDocPath, "All Messages Been Read", "true");

                //saving the data at the contact database
                String contactDocumentPath = contactEmail + "/Contacts/" + userEmail + "/Chat";
                String contactTimeDocumentPath = contactEmail + "/Contacts/" + userEmail + "/Chat Time";
                String contactMoreInfoDocPath = contactEmail + "/Contacts/" + userEmail + "/More Info";
                databaseManager.createNewField(contactDocumentPath, "Recipient" + numberOfMessages, message);
                databaseManager.createNewField(contactTimeDocumentPath, "Time" + numberOfMessages, time);
                databaseManager.updateTheField(contactMoreInfoDocPath, "All Messages Been Read", "false");

                messageAdapter.notifyDataSetChanged();
                chatBoxEditText.setText("");
                if (noConversationExists.getVisibility() == View.VISIBLE) {
                    noConversationExists.setVisibility(View.INVISIBLE);
                }
                Toast.makeText(MessagingActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Message Saved in the Database");
            }
        });

        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getAttachFile = new Intent(Intent.ACTION_GET_CONTENT);
                getAttachFile.setType("*/*");
                startActivityForResult(getAttachFile, 10);
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:
                try {
                    String path = data.getData().getPath();
                    Uri uri = data.getData();
                    //send the uri
                    fileSent(uri);

                } catch (Exception e) {
                    Log.d(TAG, "File was not selected");
                }

//                try {
//                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType(data.resolveType(this));
//                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
//                    String fileMimeType = URLConnection.guessContentTypeFromName(contentResolver.getType(uri));
//                    Log.d(TAG, "mime type is: " + fileMimeType);
//                    //intent.setDataAndType(uri, data.resolveType(this));
//                    startActivityForResult(intent, requestCode);
//                } catch (ActivityNotFoundException e) {
//                    // no Activity to handle this kind of files
//                }
//                String mimeType = URLConnection.guessContentTypeFromName(uri.toString());
//                if (mimeType != null) {
//                    Log.d(TAG, "mime type is: " + mimeType);
//                } else {
//                    Log.d(TAG, "mime type is: null");
//                }
        }
    }

    public void fileSent(final Uri uri) {
        Toast.makeText(MessagingActivity.this, "Sending file", Toast.LENGTH_SHORT).show();
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        String type = contentResolver.getType(uri);

//                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("H:mm a");
//                LocalDateTime now = LocalDateTime.now();
//
//                String time = dtf.format(now);

        SimpleDateFormat formatter= new SimpleDateFormat("HH:mm a");
        Date date = new Date(System.currentTimeMillis());
        String time = formatter.format(date);
        Log.d(TAG, "Time is: " + time);
        Log.d(TAG, "Formatted is: " + type);

        Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        //long size = returnCursor.getLong(sizeIndex);//gives you size in bytes
        returnCursor.close();

        if (type.contains("image")) {
            int numberOfMessages = messageItems.size() - 1;
            final String savingPath = userEmail + "/" + contactEmail + "/" + name;

            databaseManager.saveFileInFirebaseStorage(savingPath, uri, new FirebaseCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onCallback(Object value) {
                    if (value != null) {
                        saveTheFileInfoInTheDB(savingPath, "image");
                    }
                }
            });

        } else if (type.contains("video")) {
            final String savingPath = userEmail + "/" + contactEmail + "/" + name;

            databaseManager.saveFileInFirebaseStorage(savingPath, uri, new FirebaseCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onCallback(Object value) {
                    if (value != null) {
                        saveTheFileInfoInTheDB(savingPath, "video");
                    }
                }
            });
        } else if (type.contains("word")) {
            final String savingPath = userEmail + "/" + contactEmail + "/" + name;

            databaseManager.saveFileInFirebaseStorage(savingPath, uri, new FirebaseCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onCallback(Object value) {
                    if (value != null) {
                        saveTheFileInfoInTheDB(savingPath, "word");
                    }
                }
            });
        } else if (type.contains("excel")) {
            final String savingPath = userEmail + "/" + contactEmail + "/" + name;

            databaseManager.saveFileInFirebaseStorage(savingPath, uri, new FirebaseCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onCallback(Object value) {
                    if (value != null) {
                        saveTheFileInfoInTheDB(savingPath, "excel");
                    }
                }
            });
        } else if (type.contains("pdf")) {
            final String savingPath = userEmail + "/" + contactEmail + "/" + name;

            databaseManager.saveFileInFirebaseStorage(savingPath, uri, new FirebaseCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onCallback(Object value) {
                    if (value != null) {
                        saveTheFileInfoInTheDB(savingPath, "pdf");
                    }
                }
            });
        } else if (type.contains("presentation")) {
            final String savingPath = userEmail + "/" + contactEmail + "/" + name;

            databaseManager.saveFileInFirebaseStorage(savingPath, uri, new FirebaseCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onCallback(Object value) {
                    if (value != null) {
                        saveTheFileInfoInTheDB(savingPath, "powerpoint");
                    }
                }
            });
        } else if (type.contains("text")) {
            final String savingPath = userEmail + "/" + contactEmail + "/" + name;

            databaseManager.saveFileInFirebaseStorage(savingPath, uri, new FirebaseCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onCallback(Object value) {
                    if (value != null) {
                        saveTheFileInfoInTheDB(savingPath, "text");
                    }
                }
            });
        } else if (type.contains("application")){
            final String savingPath = userEmail + "/" + contactEmail + "/" + name;

            databaseManager.saveFileInFirebaseStorage(savingPath, uri, new FirebaseCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onCallback(Object value) {
                    if (value != null) {
                        saveTheFileInfoInTheDB(savingPath, "application");
                    }
                }
            });
        }
    }

/*    private void getTheUriOfTheFileFromDB(String savingPath, final String type) {
        databaseManager.getTheFileUriFromFirebaseStorage(savingPath, new FirebaseCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onCallback(Object value) {
                if (value != null) {
                    Uri uriFromDB = (Uri) value;
                    saveTheFileInfoInTheDB(uriFromDB, type);
                }
            }
        });
    }*/

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveTheFileInfoInTheDB(String savingPath, String type) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("H:mm a");
        LocalDateTime now = LocalDateTime.now();

        String time = dtf.format(now);

        String documentPath = userEmail + "/Contacts/" + contactEmail + "/Chat";
        String timeDocumentPath = userEmail + "/Contacts/" + contactEmail + "/Chat Time";
        String moreInfoDocPath = userEmail + "/Contacts/" + contactEmail + "/More Info";
        int numberOfMessages = messageItems.size() - 1;
        databaseManager.createNewField(documentPath, type + "Sent" + numberOfMessages, savingPath);
        databaseManager.createNewField(timeDocumentPath, "Time" + numberOfMessages, time);
        databaseManager.updateTheField(moreInfoDocPath, "All Messages Been Read", "true");

        //saving the data at the contact database
        String contactDocumentPath = contactEmail + "/Contacts/" + userEmail + "/Chat";
        String contactTimeDocumentPath = contactEmail + "/Contacts/" + userEmail + "/Chat Time";
        String contactMoreInfoDocPath = contactEmail + "/Contacts/" + userEmail + "/More Info";
        databaseManager.createNewField(contactDocumentPath, type + "Received" + numberOfMessages, savingPath);
        databaseManager.createNewField(contactTimeDocumentPath, "Time" + numberOfMessages, time);
        databaseManager.updateTheField(contactMoreInfoDocPath, "All Messages Been Read", "false");


        getTheFileData(null, null, time, savingPath, type + "Sent", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                messageAdapter.notifyDataSetChanged();
                Toast.makeText(MessagingActivity.this, "File Sent", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getTheFileData(final Uri contactProfilePicUri, final String contactName, final String time, String savingPath, final String type, final FirebaseCallback firebaseCallback) {
        databaseManager.getTheUriLinkNameAndSizeFromFirebaseStorageInHashMap(savingPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                HashMap<String, Object> fileData = (HashMap) value;
                Uri fileUri = Uri.parse(fileData.get("Uri").toString());
                String fileName = fileData.get("name").toString();
                long fileSize = (long) fileData.get("size");
                showTheFile(contactProfilePicUri, contactName, time, type, fileUri, fileName, fileSize, firebaseCallback);
            }
        });
    }

    private void showTheFile(Uri contactProfilePicUri, String contactName, String time, String type, Uri fileUri, String fileName, long fileSize, FirebaseCallback firebaseCallback) {
        if (type.contains("imageSent")) {
            messageItems.add(new MessageItem(fileUri, time, "imageSent", fileName, fileSize));
        } else if (type.contains("imageReceived")) {
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, fileUri, time, "imageReceived", fileName, fileSize));
        } else if (type.contains("videoSent")) {
            messageItems.add(new MessageItem(fileUri, time, "videoSent", fileName, fileSize));
        } else if (type.contains("videoReceived")) {
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, fileUri, time, "videoReceived", fileName, fileSize));
        } else if (type.contains("pdfSent")) {
            messageItems.add(new MessageItem(fileUri, time, "pdfSent", fileName, fileSize));
        } else if (type.contains("pdfReceived")) {
            Log.d(TAG, "Pdf received: " + fileUri.toString());
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, fileUri, time, "pdfReceived", fileName, fileSize));
        } else if (type.contains("textSent")) {
            messageItems.add(new MessageItem(fileUri, time, "textSent", fileName, fileSize));
        } else if (type.contains("textReceived")) {
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, fileUri, time, "textReceived", fileName, fileSize));
        } else if (type.contains("wordSent")) {
            messageItems.add(new MessageItem(fileUri, time, "wordSent", fileName, fileSize));
        } else if (type.contains("wordReceived")) {
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, fileUri, time, "wordReceived", fileName, fileSize));
        } else if (type.contains("excelSent")) {
            messageItems.add(new MessageItem(fileUri, time, "excelSent", fileName, fileSize));
        } else if (type.contains("excelReceived")) {
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, fileUri, time, "excelReceived", fileName, fileSize));
        } else if (type.contains("powerpointSent")) {
            messageItems.add(new MessageItem(fileUri, time, "powerpointSent", fileName, fileSize));
        } else if (type.contains("powerpointReceived")) {
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, fileUri, time, "powerpointReceived", fileName, fileSize));
        }

//        Log.d(TAG, "item count for messageAdapter is: " + messageAdapter.getItemCount());

        //messageAdapter.notifyDataSetChanged();
        chatBoxEditText.setText("");
        if (noConversationExists.getVisibility() == View.VISIBLE) {
            noConversationExists.setVisibility(View.INVISIBLE);
        }

        Log.d(TAG, "File Saved in the Database");
        firebaseCallback.onCallback(null);
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
        boolean isItAFile = false;

        addMessageToMessageItemRecursion(0);
/*        for (int i = 0; i < messages.size(); i++) {
            String messageKey = "";
            String message;
            String time = messagesTime.get("Time" + i);
            for (int j = 0; j < messagesKeySet.size(); j++) {
                Log.d(TAG, "i = " + i + "       messageKeySet is " + messagesKeySet.get(i));
                if (messagesKeySet.get(j).equals("Me" + i) || messagesKeySet.get(j).equals("Recipient" + i)
                        || messagesKeySet.get(j).equals("imageSent" + i) || messagesKeySet.get(j).equals("imageReceived" + i) || messagesKeySet.get(j).equals("videoSent" + i) || messagesKeySet.get(j).equals("videoReceived" + i)
                        || messagesKeySet.get(j).equals("pdfSent" + i) || messagesKeySet.get(j).equals("pdfReceived" + i) || messagesKeySet.get(j).equals("textSent" + i) || messagesKeySet.get(j).equals("textReceived" + i)
                        || messagesKeySet.get(j).equals("wordSent" + i) || messagesKeySet.get(j).equals("wordReceived" + i) || messagesKeySet.get(j).equals("excelSent" + i) || messagesKeySet.get(j).equals("excelReceived" + i)
                        || messagesKeySet.get(j).equals("powerpointSent" + i) || messagesKeySet.get(j).equals("powerpointReceived" + i)
                ) {
                    messageKey = messagesKeySet.get(j);
                    break;
                }

            }

            message = (String) messages.get(messageKey);
            Log.d(TAG, "message is " + message);
            Log.d(TAG, "messageKey is: " + messageKey);

            if (!messageKey.contains("Me") && !messageKey.contains("Recipient") && !messageKey.contains("Note") && !messageKey.equals("")) {
                getTheFileData(contactProfilePicUri, contactName, time, message, messageKey);
            } else if (messageKey.contains("Recipient")) {
                messageItems.add(new MessageItem(contactProfilePicUri, contactName, message, time, "received"));
            } else if (messageKey.contains("Me")) {
                messageItems.add(new MessageItem(message, time, "sent"));
            }
        }
        checkIfConversationHasEndedOrAccountDeactivated();*/
    }

    public void addMessageToMessageItemRecursion(int i) {
        String contactName = chatSingleton.getContactName();
        Uri contactProfilePicUri = chatSingleton.getContactProfilePicUri();
        Log.d(TAG, "i/messagesSize is " + i + "/" + messages.size());
        if (i == messages.size() - 1) {
            checkIfConversationHasEndedOrAccountDeactivated();
        } else {

            String messageKey = "";
            String message;
            String time = messagesTime.get("Time" + i);
            for (int j = 0; j < messagesKeySet.size(); j++) {
                Log.d(TAG, "i = " + i + "       messageKeySet is " + messagesKeySet.get(i));
                if (messagesKeySet.get(j).equals("Me" + i) || messagesKeySet.get(j).equals("Recipient" + i)
                        || messagesKeySet.get(j).equals("imageSent" + i) || messagesKeySet.get(j).equals("imageReceived" + i) || messagesKeySet.get(j).equals("videoSent" + i) || messagesKeySet.get(j).equals("videoReceived" + i)
                        || messagesKeySet.get(j).equals("pdfSent" + i) || messagesKeySet.get(j).equals("pdfReceived" + i) || messagesKeySet.get(j).equals("textSent" + i) || messagesKeySet.get(j).equals("textReceived" + i)
                        || messagesKeySet.get(j).equals("wordSent" + i) || messagesKeySet.get(j).equals("wordReceived" + i) || messagesKeySet.get(j).equals("excelSent" + i) || messagesKeySet.get(j).equals("excelReceived" + i)
                        || messagesKeySet.get(j).equals("powerpointSent" + i) || messagesKeySet.get(j).equals("powerpointReceived" + i)
                ) {
                    messageKey = messagesKeySet.get(j);
                    break;
                }

            }

            message = (String) messages.get(messageKey);
            Log.d(TAG, "message is " + message);
            Log.d(TAG, "messageKey is: " + messageKey);
            final int iIncrement = i + 1;
            if (!messageKey.contains("Me") && !messageKey.contains("Recipient") && !messageKey.contains("Note") && !messageKey.equals("")) {
                getTheFileData(contactProfilePicUri, contactName, time, message, messageKey, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        addMessageToMessageItemRecursion(iIncrement);
                    }
                });
            } else if (messageKey.contains("Recipient")) {
                messageItems.add(new MessageItem(contactProfilePicUri, contactName, message, time, "received"));
                //messageAdapter.notifyDataSetChanged();
                addMessageToMessageItemRecursion(iIncrement);

            } else if (messageKey.contains("Me")) {
                messageItems.add(new MessageItem(message, time, "sent"));
                //messageAdapter.notifyDataSetChanged();
                addMessageToMessageItemRecursion(iIncrement);
            }
        }
    }


/*    private  void getTheFileData1(Uri contactProfilePicUri, String contactName, String time, String messageKey, String message) {
        //databaseManager.getThe
         if (messageKey.contains("imageSent")) {
            Log.d(TAG, "ImageSent type");
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contentUri, time, "imageSent"));
        } else if (messageKey.contains("imageReceived")) {
            Log.d(TAG, "imageReceived type");
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, contentUri, time, "imageReceived"));
        } else if (messageKey.contains("videoSent")) {
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contentUri, time, "videoSent"));
        } else if (messageKey.contains("videoReceived")) {
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, contentUri, time, "videoReceived"));
        } else if (messageKey.contains("pdfSent")) {
            Log.d(TAG, "PDFSent type");
            Uri contentUri = Uri.parse(message);
            Log.d(TAG, "Pdf Sent: " + contentUri.toString());
            messageItems.add(new MessageItem(contentUri, time, "pdfSent"));
        } else if (messageKey.contains("pdfReceived")) {
            Log.d(TAG, "PDFReceived type");
            Uri contentUri = Uri.parse(message);
            Log.d(TAG, "Pdf received: " + contentUri.toString());
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, contentUri, time, "pdfReceived"));
        } else if (messageKey.contains("textSent")) {
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contentUri, time, "textSent"));
        } else if (messageKey.contains("textReceived")) {
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, contentUri, time, "textReceived"));
        } else if (messageKey.contains("wordSent")) {
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contentUri, time, "wordSent"));
        } else if (messageKey.contains("wordReceived")) {
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, contentUri, time, "wordReceived"));
        } else if (messageKey.contains("excelSent")) {
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contentUri, time, "excelSent"));
        } else if (messageKey.contains("excelReceived")) {
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, contentUri, time, "excelReceived"));
        } else if (messageKey.contains("powerpointSent")) {
            Log.d(TAG, "powerpointSent type");
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contentUri, time, "powerpointSent"));
        } else if (messageKey.contains("powerpointReceived")) {
            Log.d(TAG, "powerpointReceived type");
            Uri contentUri = Uri.parse(message);
            messageItems.add(new MessageItem(contactProfilePicUri, contactName, contentUri, time, "powerpointReceived"));
        }
    }*/

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
                        if (dataKeys.get(i).contains("Me" + (totalKeys - 2)) || dataKeys.get(i).contains("Recipient" + (totalKeys - 2)) || dataKeys.get(i).contains("Sent") || dataKeys.get(i).contains("Received")) {
                            lastKey = dataKeys.get(i);
                            break;
                        }
                    }
                    Log.d(TAG, "Last Key: " + lastKey);
                    if (lastKey.contains("Me")) {
                        //do nothing
                        firstLoad = false;
                        Log.d(TAG, "Last key: " + lastKey);
                    } else if (lastKey.contains("Recipient")) {
                        if (firstLoad) {
                            firstLoad = false;
                        } else {
                            final String message = data.get(lastKey).toString();
                            final String contactName = chatSingleton.getContactName();
                            final Uri contactProfilePicUri = chatSingleton.getContactProfilePicUri();
                            String documentPath = userEmail + "/Contacts/" + contactEmail + "/Chat Time";
                            databaseManager.getFieldValue(documentPath, "Time" + (dataKeys.size() - 2), new FirebaseCallback() {
                                @Override
                                public void onCallback(Object value) {
                                    String time = value.toString();
                                    messageItems.add(new MessageItem(contactProfilePicUri, contactName, message, time, "received"));
                                    messageAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        noConversationExists.setVisibility(View.GONE);
                    } else if (lastKey.contains("Sent")) {
                        firstLoad = false;
/*                        messagesKeySet.get(j).equals("imageSent" + i) || messagesKeySet.get(j).equals("imageReceived" + i) || messagesKeySet.get(j).equals("videoSent" + i) || messagesKeySet.get(j).equals("videoReceived" + i)
                                || messagesKeySet.get(j).equals("pdfSent" + i) || messagesKeySet.get(j).equals("pdfReceived" + i) || messagesKeySet.get(j).equals("textSent" + i) || messagesKeySet.get(j).equals("textReceived" + i)
                                || messagesKeySet.get(j).equals("wordSent" + i) || messagesKeySet.get(j).equals("wordReceived" + i) || messagesKeySet.get(j).equals("excelSent" + i) || messagesKeySet.get(j).equals("excelReceived" + i)
                                || messagesKeySet.get(j).equals("powerpointSent" + i) || messagesKeySet.get(j).equals("powerpointReceived" + i)*/
                    } else if (lastKey.contains("Note") == false) {
                        if (firstLoad) {
                            firstLoad = false;
                        } else {
                            final String message = data.get(lastKey).toString();
                            final String contactName = chatSingleton.getContactName();
                            final Uri contactProfilePicUri = chatSingleton.getContactProfilePicUri();
                            String documentPath = userEmail + "/Contacts/" + contactEmail + "/Chat Time";
                            final String finalLastKey = lastKey;
                            databaseManager.getFieldValue(documentPath, "Time" + (dataKeys.size() - 2), new FirebaseCallback() {
                                @Override
                                public void onCallback(Object value) {
                                    String time = value.toString();

                                    getTheFileData(contactProfilePicUri, contactName, time, message, finalLastKey, new FirebaseCallback() {
                                        @Override
                                        public void onCallback(Object value) {
                                            messageAdapter.notifyDataSetChanged();
                                        }
                                    });




                                    /*messageItems.add(new MessageItem(contactProfilePicUri, contactName, message, time, "received"));
                                    messageAdapter.notifyDataSetChanged();*/

                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "Current Data: Null");
                    }
                }
                    /*else if (lastKey.contains("imageReceived")) {
                        if (firstLoad) {
                            firstLoad = false;
                        } else {
                            final String message = data.get(lastKey).toString();
                            final String contactName = chatSingleton.getContactName();
                            final Uri contactProfilePicUri = chatSingleton.getContactProfilePicUri();
                            String documentPath = userEmail + "/Contacts/" + contactEmail + "/Chat Time";
                            databaseManager.getFieldValue(documentPath, "Time" + (dataKeys.size() - 2), new FirebaseCallback() {
                                @Override
                                public void onCallback(Object value) {
                                    String time = value.toString();
                                    *//*messageItems.add(new MessageItem(contactProfilePicUri, contactName, message, time, "received"));
                                    messageAdapter.notifyDataSetChanged();*//*

                                }
                            });
                        }
                    } else if (lastKey.contains("videoSent")) {
                        firstLoad = false;
                    } else if (lastKey.contains("videoReceived")) {
                        if (firstLoad) {
                            firstLoad = false;
                        } else {

                        }
                    } else if (lastKey.contains("pdfSent")) {
                        firstLoad = false;
                    } else if (lastKey.contains("pdfReceived")) {
                        if (firstLoad) {
                            firstLoad = false;
                        } else {

                        }
                    } else if (lastKey.contains("textSent")) {
                        firstLoad = false;
                    } else if (lastKey.contains("textReceived")) {
                        if (firstLoad) {
                            firstLoad = false;
                        } else {

                        }
                    } else if (lastKey.contains("wordSent")) {
                        firstLoad = false;
                    } else if (lastKey.contains("wordReceived")) {
                        if (firstLoad) {
                            firstLoad = false;
                        } else {

                        }
                    } else if (lastKey.contains("excelSent")) {
                        firstLoad = false;
                    } else if (lastKey.contains("excelReceived")) {
                        if (firstLoad) {
                            firstLoad = false;
                        } else {

                        }
                    } else if (lastKey.contains("powerpointSent")) {
                        firstLoad = false;
                    } else if (lastKey.contains("powerpointReceived")) {
                        if (firstLoad) {
                            firstLoad = false;
                        } else {

                        }
                    } else {
                        firstLoad = false;
                    }
                } else {
                    Log.d(TAG, "Current Data: Null");
                }
*/

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
                    }
                }
            }
        });
    }

}
