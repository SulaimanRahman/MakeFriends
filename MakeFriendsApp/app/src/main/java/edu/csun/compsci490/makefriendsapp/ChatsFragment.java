package edu.csun.compsci490.makefriendsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;

import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatsFragment extends Fragment{
    private ArrayList<ChatItem> chatItems;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;

    private Button btnAdd, btnDelete;

    private ImageView btnFindFriends;

    private ProgressBar progressBar;

    private DatabaseManager databaseManager;
    private UserSingleton userSingleton;
    private ChatSingleton chatSingleton;
    private HashMap<String, String> contactsNames;
    private HashMap<String, Uri> contactsProfilePicUri;
    private HashMap<String, String> contactsLastMessageKey;
    private HashMap<String, String> contactsLastMessage;

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

//        chatAdapter.setOnChatClickListener(new ChatAdapter.OnChatClickListener() {
//            @Override
//            public void onChatClick(int position) {
//                enterMessagingActivity();
//            }
//
//            @Override
//            public void onDeleteClick(int position) {
//                deleteChat(position);
//            }
//        });

        return rootView;
    }

    public void insertChat(int position){
        if(chatItems.size()==1 && chatItems.get(0).getName() == ""){
            chatItems.remove(0);
            chatAdapter.notifyItemRemoved(0);
        }
        // insert new chat with new person at position 0 (top)
        chatItems.add(position, new ChatItem(R.drawable.ic_baseline_account_circle_24,"UseR HAS BEEN FouNd!"));
        //chatAdapter.notifyItemInserted(position);
    }

    public void deleteChat(int position){
        chatItems.remove(position);
        chatAdapter.notifyItemRemoved(position);

        if(chatItems.isEmpty()){
            //chatItems.add(new ChatItem(R.drawable.ic_launcher_foreground, "", "NO CONVERSATIONS YET"));
            chatAdapter.notifyItemInserted(0);
        }
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
            String lastMessage = contactsLastMessage.get(email);
            Uri profilePicUri = contactsProfilePicUri.get(email);

            /*
            write the code here that will create the chat bar and the picture circle in the
            beginning and use the variables above to get all the data for it.
             */
            chatItems.add(new ChatItem(profilePicUri, contactName, lastMessage, email));
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
                chatSingleton.setContactEmail(chatItems.get(position).getContactEmail());
                chatSingleton.setContactName(chatItems.get(position).getName());
                chatSingleton.setContactProfilePicUri(chatItems.get(position).getImgResource());

                enterMessagingActivity();
            }

            @Override
            public void onDeleteClick(int position) {
                deleteChat(position);
            }
        });

    }

    //when the chat is clicked in the navigation bar
    public void getAllContactsEmails() {

        String documentPath = userSingleton.getEmail() + "/Contacts";
        databaseManager.getFieldValue(documentPath, "All Users", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                if (value == null) {
                    //failed to get the data
                    return;
                }

                ArrayList allContactsEmail = (ArrayList) value;

                getContactsNamesAndProfilePicUri(allContactsEmail);
            }
        });
    }

    public void getContactsNamesAndProfilePicUri(final ArrayList allContactsEmail) {

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

                    HashMap<String, Object> data = (HashMap) value;

                    ArrayList<String> fieldsKey = new ArrayList();
                    fieldsKey.addAll(data.keySet());

                    int lastKeyIndex = fieldsKey.size() - 1;
                    String lastKey = fieldsKey.get(lastKeyIndex);
                    contactsLastMessageKey.put(allContactsEmail.get(finalI), lastKey);

                    String lastMessage = data.get(lastKey).toString();
                    contactsLastMessage.put(allContactsEmail.get(finalI), lastMessage);

                    if (finalI == allContactsEmail.size() - 1) {
                        listAllTheChats(allContactsEmail);
                    }
                }
            });
        }

    }

    public void listAllTheChats(ArrayList<String> allContactsEmail) {
        createChatList(allContactsEmail);

    }
}