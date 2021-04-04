package edu.csun.compsci490.makefriendsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatsFragment extends Fragment{
    private ArrayList<ChatItem> chatItems;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;

    private EditText chatsSearchBar;

    private Button btnAdd, btnDelete;

    private String TAG = "ChatsFragment";

    private ImageView btnFindFriends;

    private ProgressBar progressBar;

    private DatabaseManager databaseManager;
    private UserSingleton userSingleton;
    private ChatSingleton chatSingleton;
    private HashMap<String, String> contactsNames;
    private HashMap<String, Uri> contactsProfilePicUri;
    private HashMap<String, String> contactsLastMessageKey;
    private HashMap<String, String> contactsLastMessage;
    private HashMap<String, Boolean> isConversationEnded;
    private HashMap<String, Boolean> isConversationEndedByMe;
    private HashMap<String, Boolean> isUserBlocked;
    private HashMap<String, Boolean> isOtherUserAccountDeactivated;
    private HashMap<String, Boolean> areAllMessagesBeenRead;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // build recycler view
        final View rootView = inflater.inflate(R.layout.fragment_chats, container, false);

        databaseManager = new DatabaseManager();
        userSingleton = UserSingleton.getInstance();
        chatSingleton = chatSingleton.getInstance();
        contactsNames = new HashMap<>();
        contactsProfilePicUri = new HashMap<>();
        contactsLastMessageKey = new HashMap<>();
        contactsLastMessage = new HashMap<>();

        isConversationEnded = new HashMap<>();
        isConversationEndedByMe = new HashMap<>();
        isUserBlocked = new HashMap<>();
        isOtherUserAccountDeactivated = new HashMap<>();
        areAllMessagesBeenRead = new HashMap<>();

        progressBar = rootView.findViewById(R.id.fragment_chats_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        getAllContactsEmails();

        //createChatList();



        recyclerView = (RecyclerView) rootView.findViewById(R.id.chatRecyclerView);
        recyclerView.setHasFixedSize(true);
//        chatAdapter = new ChatAdapter(chatItems);
//        recyclerView.setAdapter(chatAdapter);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // intitialize buttons
        //btnAdd = rootView.findViewById(R.id.btn_addChat);
       // btnDelete = rootView.findViewById(R.id.btn_deleteChat);

        btnFindFriends = rootView.findViewById(R.id.btn_findFriends);

        btnFindFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_layout, new SearchUsersFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        chatsSearchBar = rootView.findViewById(R.id.chatsSearchBar);
        chatsSearchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (chatsSearchBar.getText().equals("")) {
                        //reset everything
                        if (contactsNames.size() == 0) {
                            noContacts();
                            return true;
                        } else {
                            ArrayList<String> allContactsEmails = new ArrayList<>();
                            allContactsEmails.addAll(contactsNames.keySet());
                            listAllTheChats(allContactsEmails);
                            return true;
                        }

                    } else {
                        if (contactsNames.size() == 0) {
                            noContacts();
                            return true;
                        } else {
                            ArrayList<String> searchedContactEmails = new ArrayList<>();
                            ArrayList<String> allContactsEmails = new ArrayList<>();
                            allContactsEmails.addAll(contactsNames.keySet());

                            for (int j = 0; j < contactsNames.size(); j++) {
                                String contactEmail = allContactsEmails.get(j);
                                if (contactsNames.get(contactEmail).contains(chatsSearchBar.getText())) {
                                    searchedContactEmails.add(contactEmail);
                                }
                            }

                            if (searchedContactEmails.size() == 0) {
                                noContactsFoundInSearch();
                                return true;
                            } else {
                                listAllTheChats(searchedContactEmails);
                                return true;
                            }

                        }
                    }
                }
                return false;
            }
        });

        chatsSearchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focusGained) {
                if (!focusGained) {
                    if (chatsSearchBar.getText().equals("")) {
                        if (contactsNames.size() == 0) {
                            noContacts();
                        } else {
                            ArrayList<String> allContactsEmails = new ArrayList<>();
                            allContactsEmails.addAll(contactsNames.keySet());
                            listAllTheChats(allContactsEmails);
                        }
                    }
                }
            }
        });

//        chatsSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                if (chatsSearchBar.getText().equals("")) {
//                    //reset everything
//                    ArrayList<String> allContactsEmails = new ArrayList<>();
//                    allContactsEmails.addAll(contactsNames.keySet());
//                    listAllTheChats(allContactsEmails);
//                    return true;
//                }
//                return false;
//            }
//        });
        return rootView;
    }

    private void noContactsFoundInSearch() {
        chatItems = new ArrayList<>();
        //chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "NO CONVERSATIONS YET"));
        chatItems.add(new ChatItem(null, "No Results", "Try something different", null, false, false, false, false, true));
        //insertChat(0);
        chatAdapter = new ChatAdapter(chatItems);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progressBar.setVisibility(View.GONE);
        chatAdapter.notifyItemInserted(0);
    }

    public void insertChat(int position){
        if(chatItems.size()==1 && chatItems.get(0).getName() == ""){
            chatItems.remove(0);
            chatAdapter.notifyItemRemoved(0);
        }
        // insert new chat with new person at position 0 (top)
        //chatItems.add(position, new ChatItem(R.drawable.ic_baseline_account_circle_24,"UseR HAS BEEN FouNd!"));
        //chatAdapter.notifyItemInserted(position);
    }

    public void deleteChat(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to end conversation and delete the messages? There's no going back.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressBar.setVisibility(View.VISIBLE);
                        String contactEmail = chatItems.get(position).getContactEmail();

                        chatItems.remove(position);
                        chatAdapter.notifyItemRemoved(position);

                        setConversationEndVariableToTrueForContact(contactEmail);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();


//        if(chatItems.isEmpty()){
//            //chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "NO CONVERSATIONS YET"));
//            chatAdapter.notifyItemInserted(0);
//        }
    }

    private void setConversationEndVariableToTrueForContact(String contactEmail) {
        String moreInfoDocumentPath = contactEmail + "/Contacts/" + userSingleton.getEmail() + "/More Info";
        databaseManager.updateTheField(moreInfoDocumentPath, "Conversation Ended", "true");
        removeTheContactFromTheContactsList(contactEmail);
    }

    private void removeTheContactFromTheContactsList(final String contactEmail) {
        final String documentsPath = userSingleton.getEmail() + "/Contacts";
        databaseManager.getFieldValue(documentsPath, "All Users", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                ArrayList<String> contacts = (ArrayList) value;
                if (contacts.size() == 1) {
                    databaseManager.updateTheField(documentsPath, "All Users", "none");
                    noContacts();
                    removeTheContactDataFromTheUserDB(contactEmail);
                } else {
                    contacts.remove(contactEmail);
                    databaseManager.updateTheField(documentsPath, "All Users", contacts);
                    removeTheContactDataFromTheUserDB(contactEmail);
                }
            }
        });

    }

    private void removeTheContactDataFromTheUserDB(String contactEmail) {
        String collectionPath = userSingleton.getEmail() + "/Contacts/" + contactEmail;
        databaseManager.deleteCollection(collectionPath);
        progressBar.setVisibility(View.GONE);
    }

    public void enterMessagingActivity(){
        // enter chat method must be implemented
        Intent intent = new Intent();
        intent.setClass(getActivity(), MessagingActivity.class);
        getActivity().startActivity(intent);
    }

    private void createChatList(final ArrayList<String> allContactsEmail) {
        chatItems = new ArrayList<>();

        for (int i = 0; i < allContactsEmail.size(); i++) {
            String email = allContactsEmail.get(i);

            String contactName = contactsNames.get(email);
            String lastMessageKey = contactsLastMessageKey.get(email);
            Log.d(TAG, "Last message Key is 296: " + lastMessageKey);
            String lastMessage;
            if (lastMessageKey.equals("Note0")) {
                lastMessage = "Friendship Found!";
            } else {
               lastMessage = contactsLastMessage.get(email);
            }
            Log.d(TAG, "Last message is 303: " + lastMessage);

            Uri profilePicUri = contactsProfilePicUri.get(email);

            boolean isConversationEnded = this.isConversationEnded.get(email);
            boolean isConversationEndedByMe = this.isConversationEndedByMe.get(email);
            boolean isUserBlocked = this.isUserBlocked.get(email);
            boolean isOtherUserAccountDeactivated = this.isOtherUserAccountDeactivated.get(email);
            boolean areAllMessagesBeenRead = this.areAllMessagesBeenRead.get(email);
            /*
            write the code here that will create the chat bar and the picture circle in the
            beginning and use the variables above to get all the data for it.
             */
            chatItems.add(new ChatItem(profilePicUri, contactName, lastMessage, email, isConversationEnded, isConversationEndedByMe, isUserBlocked, isOtherUserAccountDeactivated, areAllMessagesBeenRead));
            insertChat(0);
        }

//        chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "Home name", "home preview"));
//        chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "Chat name", "chat preview"));
//        chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "Folder name", "folder preview"));

        chatAdapter = new ChatAdapter(chatItems);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progressBar.setVisibility(View.GONE);
        chatAdapter.setOnChatClickListener(new ChatAdapter.OnChatClickListener() {
            @Override
            public void onChatClick(int position) {
                /* still need to handle event when app message item like "user found", "chat ended" etc is clicked
                * also how item is replaced or removed */
                chatSingleton.setContactEmail(chatItems.get(position).getContactEmail());
                chatSingleton.setContactName(chatItems.get(position).getName());
                chatSingleton.setContactProfilePicUri(chatItems.get(position).getImgResource());

                enterMessagingActivity();
            }

            @Override
            public void onDeleteClick(int position) {
                deleteChat(position);

            }

            public void onBlockingClick(int position, ImageView icBlock) {
                blockTheContact(position, icBlock);
            }
        });
    }

    private void blockTheContact(final int position, final ImageView icBlock) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to end conversation and block the user? There's no going back.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressBar.setVisibility(View.VISIBLE);
                        icBlock.setVisibility(View.INVISIBLE);
                        String contactEmail = chatItems.get(position).getContactEmail();
                        setConversationEndVariableToTrueForTheContact(contactEmail);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    private void setConversationEndVariableToTrueForTheContact(String contactEmail) {
        String moreInfoDocumentPath = contactEmail + "/Contacts/" + userSingleton.getEmail() + "/More Info";
        databaseManager.updateTheField(moreInfoDocumentPath, "Conversation Ended", "true");
        setBlockedUserToTrue(contactEmail);
    }

    private void setBlockedUserToTrue(String contactEmail) {
        String documentPath = userSingleton.getEmail() + "/Contacts/" + contactEmail + "/More Info";
        databaseManager.updateTheField(documentPath, "Blocked User", "true");
        databaseManager.updateTheField(documentPath, "Conversation Ended", "true");
        databaseManager.updateTheField(documentPath, "Conversation Ended From My Side", "true");
        setContactInTheBlockedList(contactEmail);
    }

    private void setContactInTheBlockedList(final String contactEmail) {

        final String documentPath = userSingleton.getEmail() + "/Contacts";
        databaseManager.getFieldValue(documentPath, "Blocked Users", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                String dbValue = value.toString();
                if (dbValue.equals("none")) {
                    ArrayList<String> blockedUsers = new ArrayList<>();
                    blockedUsers.add(contactEmail);
                    databaseManager.updateTheField(documentPath, "Blocked Users", blockedUsers);
                    progressBar.setVisibility(View.GONE);
                } else {
                    ArrayList<String> blockedUsers = (ArrayList) value;
                    blockedUsers.add(contactEmail);
                    databaseManager.updateTheField(documentPath, "Blocked Users", blockedUsers);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    //when the chat is clicked in the navigation bar
    public void getAllContactsEmails() {
        Log.d("ChatsFragment", "getAllContactsEmails");
        String documentPath = userSingleton.getEmail() + "/Contacts";
        databaseManager.getFieldValue(documentPath, "All Users", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                if (value == null) {
                    //failed to get the data
                    return;
                }
                String contacts = value.toString();
                if (contacts.equals("none")) {//user don't have any contact
                    Log.d("ChatsFragment", "user don't have any contacts");
                    noContacts();
                } else {//user do have contacts
                    ArrayList<String> allContactsEmail = (ArrayList) value;
                    getContactsNamesAndProfilePicUri(allContactsEmail);
                }
            }
        });
    }

    private void noContacts() {
        chatItems = new ArrayList<>();
        //chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "NO CONVERSATIONS YET"));
        chatItems.add(new ChatItem(null, "NO CONVERSATIONS YET!", "Find connection to start chatting!", null, false, false, false, false, true));
        //insertChat(0);
        chatAdapter = new ChatAdapter(chatItems);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progressBar.setVisibility(View.GONE);
        chatAdapter.notifyItemInserted(0);
    }

    public void getContactsNamesAndProfilePicUri(final ArrayList<String> allContactsEmail) {
        Log.d("ChatsFragment", "getContactsNamesAndProfilePicUri");
        for (int i = 0; i < allContactsEmail.size(); i++) {
            String documentPath = allContactsEmail.get(i) + "/Profile";

            final int finalI = i;
            databaseManager.getAllDocumentDataInHashMap(documentPath, new FirebaseCallback() {
                @Override
                public void onCallback(Object value) {
                    if (value == null) {
                        //failed to get the data
                        return;
                    }

                    HashMap<String, Object> data = (HashMap) value;

                    String firstName = data.get("First Name").toString();
                    String lastName = data.get("Last Name").toString();
                    String profilePicPath = data.get("Profile Picture Uri").toString();

                    String contactEmail = allContactsEmail.get(finalI).toString();

                    contactsNames.put(contactEmail, firstName + " " + lastName);

                    databaseManager.getTheFileUriFromFirebaseStorage(profilePicPath, new FirebaseCallback() {
                        @Override
                        public void onCallback(Object value) {
                            Uri uri = Uri.parse(value.toString());
                            contactsProfilePicUri.put(allContactsEmail.get(finalI).toString(), uri);
                            if (finalI == allContactsEmail.size() - 1) {
                                getContactsLastMessage(allContactsEmail);
                            }
                        }
                    });

                }
            });
        }
    }

    public void getContactsLastMessage(final ArrayList<String> allContactsEmail) {
        Log.d("ChatsFragment", "getContactsLastMessage");
        for (int i = 0; i < allContactsEmail.size(); i++) {
            String documentPath = userSingleton.getEmail() + "/Contacts/" +
                    allContactsEmail.get(i) + "/Chat";

            final int finalI = i;
            databaseManager.getAllDocumentDataInHashMap(documentPath, new FirebaseCallback() {
                @Override
                public void onCallback(Object value) {
                    if (value == null) {
                        //failed to get the data
                        return;
                    }

                    final HashMap<String, Object> data = (HashMap) value;

                    ArrayList<String> fieldsKey = new ArrayList();
                    fieldsKey.addAll(data.keySet());

                    int lastKeyIndex = fieldsKey.size() - 2;

                    String lastKey = "";
                    for (int i = 0; i < fieldsKey.size(); i++) {
                        Log.d("ChatsFragment", "Currentfield Key: " + fieldsKey.get(i) + ", LastKeyIndex: " + lastKeyIndex);
                        if (fieldsKey.get(i).contains(String.valueOf(lastKeyIndex))) {
                            lastKey = fieldsKey.get(i);
                            break;
                        } else if (fieldsKey.get(i).equals("Note0") && lastKey.equals("")) {
                            lastKey = fieldsKey.get(i);
                        }
                    }
                    contactsLastMessageKey.put(allContactsEmail.get(finalI), lastKey);

                    Log.d("ChatsFragment", "Last Key: " + lastKey + ", total fields key: " + fieldsKey.size());

                    //find out if the conversation has ended or not and by whom
                    String contactMoreInfoDocPath = userSingleton.getEmail() + "/Contacts/" + allContactsEmail.get(finalI) + "/More Info";
                    String fieldName = "Conversation Ended";

                    final String finalLastKey = lastKey;
                    databaseManager.getDocumentSnapshot(contactMoreInfoDocPath, new FirebaseCallback() {
                        @Override
                        public void onCallback(Object value) {
                            DocumentSnapshot snapshot = (DocumentSnapshot) value;
                            String conversationEndedStatus = snapshot.get("Conversation Ended").toString();
                            String conversationEndedByMeStatus = snapshot.get("Conversation Ended From My Side").toString();
                            String userBlockedStatus = snapshot.get("Blocked User").toString();
                            String otherUSerDeactivatedAccount = snapshot.get("OtherUserDeactivatedAccount").toString();
                            String allMessagesBeenRead = snapshot.get("All Messages Been Read").toString();

                            isConversationEnded.put(allContactsEmail.get(finalI), Boolean.valueOf(conversationEndedStatus));
                            isConversationEndedByMe.put(allContactsEmail.get(finalI), Boolean.valueOf(conversationEndedByMeStatus));
                            isUserBlocked.put(allContactsEmail.get(finalI), Boolean.valueOf(userBlockedStatus));
                            isOtherUserAccountDeactivated.put(allContactsEmail.get(finalI), Boolean.valueOf(otherUSerDeactivatedAccount));
                            areAllMessagesBeenRead.put(allContactsEmail.get(finalI), Boolean.valueOf(allMessagesBeenRead));

                            String lastMessage;

                            if (conversationEndedStatus.equals("true")) {
                                lastMessage = "This conversation has ended";
                            } else {
                                lastMessage = data.get(finalLastKey).toString();
                            }

                            contactsLastMessage.put(allContactsEmail.get(finalI), lastMessage);
                            Log.d(TAG, "Last message: " + lastMessage);
                            if (finalI == allContactsEmail.size() - 1) {
                                //addActionListenerToDatabase(allContactsEmail);
                                listAllTheChats(allContactsEmail);
                            }
                        }
                    });
                }
            });
        }
    }

    private void addActionListenerToDatabase(ArrayList<String> allContactsEmails) {
        for (int i = 0; i < allContactsEmails.size(); i++) {
            String contactChatDocPath = userSingleton.getEmail() + "/Contacts/" + allContactsEmails.get(i) + "/Chat";

            databaseManager.getDocumentReference(contactChatDocPath).addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.d(TAG, "Something went wrong: listen failed. ", error);
                    }

                    if (snapshot != null && snapshot.exists()) {

                    }
                }
            });
        }
    }

    public void listAllTheChats(ArrayList<String> allContactsEmail) {
        createChatList(allContactsEmail);
    }

}