package edu.csun.compsci490.makefriendsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class SearchActivity extends AppCompatActivity {


    private static final String TAG = "searchAct";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("user");
    private contactsAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Query query = userRef.orderBy("major",Query.Direction.DESCENDING).limit(5);

        FirestoreRecyclerOptions<UserSingleton> options= new FirestoreRecyclerOptions.Builder<UserSingleton>()
                .setQuery(query,UserSingleton.class)
                .build();


        mAdapter = new contactsAdapter(options);
        final RecyclerView recyclerView = findViewById(R.id.myRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        EditText search = findViewById(R.id.searchBox);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Query query;
                        if(editable.toString().isEmpty()) {
                            query = userRef
                                    .orderBy("major", Query.Direction.DESCENDING);
                        }
                        else{
                            query = userRef
                                    .orderBy("major", Query.Direction.DESCENDING)
                                    .whereEqualTo("major", editable.toString());
                        }
                FirestoreRecyclerOptions<UserSingleton> options = new FirestoreRecyclerOptions.Builder<UserSingleton>()
                        .setQuery(query,UserSingleton.class)
                        .build();

                mAdapter.updateOptions(options);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}