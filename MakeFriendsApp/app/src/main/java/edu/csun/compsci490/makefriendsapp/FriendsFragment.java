package edu.csun.compsci490.makefriendsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

//import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

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

    private DatabaseManager databaseManager;
    private UserSingleton userSingleton;
    private String userEmail;
    private ArrayList allContactsEmails;
    private HashMap<String, HashMap<String, Object>> contactsData;

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

        contactsRecyclerView = view.findViewById(R.id.myRecyclerView);

        databaseManager = new DatabaseManager();
        userSingleton = UserSingleton.getInstance();
        userEmail = userSingleton.getEmail();
        allContactsEmails = new ArrayList();
        contactsData = new HashMap<>();


        return view;
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

}