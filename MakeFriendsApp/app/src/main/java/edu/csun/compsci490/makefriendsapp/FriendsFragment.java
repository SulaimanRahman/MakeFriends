package edu.csun.compsci490.makefriendsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "FriendFragment";

    private RecyclerView contactsRecyclerView;
    private ContactsAdapter mAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseManager databaseManager;
    private UserSingleton userSingleton;
    private String userEmail;
    private ArrayList allContactsEmails;
    private HashMap<String, HashMap<String, Object>> contactsData;
    private ArrayList<Contact> userData = new ArrayList<Contact>();
    private List<String> allEmails = new ArrayList<String>();
    private EditText searchQuery;
    private ContactsAdapter.RecyclerviewClickListener listener;
    TextView test;
    private String data;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //Log.d(TAG,"onCreate: started.");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends,container,false);

        databaseManager = new DatabaseManager();
        userSingleton = UserSingleton.getInstance();
        userEmail = userSingleton.getEmail();
        allContactsEmails = new ArrayList();
        contactsData = new HashMap<>();
        searchQuery = view.findViewById(R.id.contactSearchBar);
        contactsRecyclerView = view.findViewById(R.id.myRecyclerView);
        gettingEmails();
        setOnClickListener(userData);

        contactsRecyclerView.setHasFixedSize(true);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ContactsAdapter(getActivity(), userData, listener);
        contactsRecyclerView.setAdapter(mAdapter);

            searchQuery.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    filter(editable.toString());
                }

            });

        return view;
    }
    private void filter(String query){
        ArrayList<Contact> filteredList = new ArrayList<>();
        for(Contact item : userData) {
            if (item.getContactName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        setOnClickListener(filteredList);
        ContactsAdapter mAdapter1 = new ContactsAdapter(getContext(),filteredList,listener);
        contactsRecyclerView.setAdapter(mAdapter1);
        mAdapter1.filterList(filteredList);

    }

    public void gettingData(final List<String> emails){
        for(int i = 0; i < emails.size();i++) {
            db.collection(emails.get(i)).document("Profile")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null) {
                                Contact user = new Contact();
                                String name = documentSnapshot.getString("First Name") +" "+ documentSnapshot.getString("Last Name");
                                user.setContactName(name);
                                user.setUserImg(documentSnapshot.getString("Profile Picture Uri"));
                                user.setContactBio(documentSnapshot.getString("Biography"));
                                user.setContactMajor("Computer Science");

                                userData.add(user);

                            } else {
                                data = "else case";
                                Log.d("Tag           ", "else case");
                                // Toast.makeText(this, "Document Does Not exists", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Log.d("Tag",e.toString());
                    data = "Failed";
                }
            });

        }
    }
    public void gettingEmails() {
        db.collection(userEmail)
                .document("Contacts").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();

                            String test = document.get("All Users").toString();
                            //Object test = document.get("All Users");
                            if(!test.equals("none")) {
                                //Toast.makeText(getContext(), "we cool", Toast.LENGTH_LONG).show();
                                allEmails = (List<String>) document.get("All Users");
                                    if(!allEmails.get(0).equals("")) {
                                        gettingData(allEmails);
                                    }
                                }
                            }


                });
    }

    public void getAllContacts() {
        String documentPath = userEmail + "/Contacts";
        databaseManager.getFieldValue(documentPath, "All Users", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                if (value == null) {
                    //failed to get data;
                    return;
                }

                allContactsEmails = (ArrayList) value;

                removeTheBlockedUsersAndContinueWithTheProcess(allContactsEmails);
            }
        });
    }

    public void removeTheBlockedUsersAndContinueWithTheProcess(final ArrayList allContactsEmails) {
        String documentPath = userEmail + "/Blocked";
        databaseManager.getAllDocumentDataInHashMap(documentPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                if (value == null) {
                    //failed to get the data
                    return;
                }

                HashMap<String, Object> data = (HashMap) value;

                for (int i = 0; i < data.size(); i++) {
                    String key = "Blocked" + (i + 1);
                    allContactsEmails.remove(data.get(key));
                }

                getAllTheContactsDataAndPicturesUrisAndContinueWithTheProcess(allContactsEmails);

            }
        });
    }

    public void getAllTheContactsDataAndPicturesUrisAndContinueWithTheProcess(final ArrayList allContactsEmails) {

        for (int i = 0; i < allContactsEmails.size(); i++) {
            String documentPath = allContactsEmails.get(i).toString() + "/Profile";

            databaseManager.getAllDocumentDataInHashMap(documentPath, new FirebaseCallback() {
                @Override
                public void onCallback(Object value) {
                    if (value == null) {
                        //failed to get the data
                        return;
                    }

                    HashMap<String, Object> data = new HashMap<>();
                    String email = data.get("Email").toString();

                    contactsData.put(email, data);

                    Uri uri = null;
                    if (data.get("Profile Picture Uri") != null) {
                        uri = (Uri) data.get("Profile Picture Uri");
                    }

                    /*
                    below this comment, write all that code that sets creates the contact bubble and
                    use the uri, if uri is null then show no picture icon, and if the uri is not
                    null, then use that uri to show the picture
                     */


                }
            });
        }
    }

    //when the user is clicked on the contact to bring up their data:
    public void getTheContactData(String contactEmail) {
        HashMap<String, Object> contactData = contactsData.get(contactEmail);
        String firstName = contactData.get("First Name").toString();
        String lastName = contactData.get("Last Name").toString();
        String biography = contactData.get("Biography").toString();
        String profilePictureUri = contactData.get("Profile Picture Uri").toString();

        String documentPath = contactEmail + "/More Info";

        databaseManager.getFieldValue(documentPath, "Interest Array", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                if (value == null) {
                    //failed to get the data
                    return;
                }

                ArrayList interest = (ArrayList) value;

                /*
                use the variables defined above and the interests array to show the contact profile.
                write the code of displaying the data on the profile below this comment
                 */

            }
        });
    }

    //When search is clicked
    public void getTheSearchResult() {//I think this is wrong
        ArrayList<String> contactsNames = new ArrayList();

        for (int i = 0; i < contactsData.size(); i++) {
            String contactEmail = allContactsEmails.get(i).toString();
            String firstName = contactsData.get(contactEmail).get("First Name").toString();
            String lastName = contactsData.get(contactEmail).get("Last Name").toString();
            contactsNames.add(firstName + " " + lastName);
        }

        String searchText = null;//instead of null, write searchTextField.getText();

        for (int i = 0; i < contactsNames.size(); i++) {
            if (contactsNames.get(i).contains(searchText)) {
                //add contactsName.get(i) in the list under the search bar.
            }
        }

    }

    private void setOnClickListener(final ArrayList<Contact> newUserData){
        listener = new ContactsAdapter.RecyclerviewClickListener() {
            @Override
            public void onClick(View view, int pos) {
                Log.d(TAG,"im in setonclick!");
                Intent intent = new Intent(getActivity(),ContactProfilePage.class);
                intent.putExtra("userName",newUserData.get(pos).getContactName());
                intent.putExtra("userBio",newUserData.get(pos).getContactBio());
                intent.putExtra("userImg",newUserData.get(pos).getUserImg());
                startActivity(intent);
            }
        };
    }

}
