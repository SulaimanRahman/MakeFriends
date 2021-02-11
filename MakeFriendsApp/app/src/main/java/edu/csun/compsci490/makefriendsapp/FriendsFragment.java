package edu.csun.compsci490.makefriendsapp;

import android.content.Intent;
import android.os.Bundle;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

interface userCallback {
    void isUserExist(boolean exist);
}
public class FriendsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static final String TAG = "FriendFragment";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseManager databaseManager;
    private UserSingleton userSingleton = UserSingleton.getInstance();
    private String userEmail;
    //private userCallback cab;
    private ArrayList<Contact> userData = new ArrayList<>();
    private List<String> allEmails = new ArrayList<>();
    private EditText searchQuery;
    private ContactsAdapter.RecyclerviewClickListener listener;
    private RecyclerView contactsRecyclerView;
    private String interests = "";
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
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends,container,false);

        //databaseManager = new DatabaseManager();
        userSingleton = UserSingleton.getInstance();
        userEmail = userSingleton.getEmail();

        contactsRecyclerView = view.findViewById(R.id.myRecyclerView);
        contactsRecyclerView.setHasFixedSize(true);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchQuery = view.findViewById(R.id.contactSearchBar);
        final ArrayList<Contact> curData = new ArrayList<>();
        final TextView test = view.findViewById(R.id.textView2);
        gettingEmails(new userCallback() {
            @Override
            public void isUserExist(boolean exist) {
                if(exist){
                    test.setText(userData.get(0).getContactName());
                    test.setAlpha(0.0f);
                }
                else{
                    Toast.makeText(getContext(),"no bueno!",Toast.LENGTH_LONG).show();
                }
            }
        });
        setOnClickListener(userData);
        ContactsAdapter mAdapter = new ContactsAdapter(getContext(), userData, listener);
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

    public void gettingEmails(final userCallback cb) {
        db.collection(userEmail)
                .document("Contacts")
                .get()
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
                                gettingData(allEmails,cb);
                            }

                            //test(allEmails);
                        }
                        //cb.isUserExist(true);
                    }

                });
    }
    public void filter(String query){
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

    public void gettingData(final List<String> emails,final userCallback cb){
        for(int i = 0; i < emails.size();i++) {
            final String curEmail = emails.get(i);
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
                                getInterests(curEmail,user,cb);
                            } else {
                                data = "else case";
                                Log.d("Tag           ", "else case");
                                // Toast.makeText(this, "Document Does Not exists", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
//                    .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    // Log.d("Tag",e.toString());
//                    data = "Failed";
//                }
//            });

        }
    }
    public void getInterests( String e, final Contact user, final userCallback cb){
        ArrayList<Contact> test123 = new ArrayList<>();
        db.collection(e)
                .document("More Info").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        String test = document.get("Interest Array").toString();
                        if(!test.equals("none")) {
                            List<String> allInterests = (List<String>) document.get("Interest Array");
                            for(String s : allInterests){
                                //Log.d("interest",s);
                                s += " ";
                                interests += s;
                            }
                        }

                        user.setContactInterest(interests);
                        userData.add(user);
                        //mUserData.addUser(user);
                        interests="";
                        cb.isUserExist(true);
                    }

                });

    }

    public void setOnClickListener(final ArrayList<Contact> newUserData){
        listener = new ContactsAdapter.RecyclerviewClickListener() {
            @Override
            public void onClick(View view, int pos) {
                Log.d(TAG,"im in setonclick!");
                Intent intent = new Intent(getActivity(),ContactProfilePage.class);
                intent.putExtra("userName",newUserData.get(pos).getContactName());
                intent.putExtra("userBio",newUserData.get(pos).getContactBio());
                intent.putExtra("userImg",newUserData.get(pos).getUserImg());
                intent.putExtra("interests",newUserData.get(pos).getContactInterest());
                startActivity(intent);
            }
        };
    }

}
