package edu.csun.compsci490.makefriendsapp;

import android.util.Log;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class RealtimeDatabaseManager {

    private DatabaseReference db;
    private String TAG = "RealtimeDatabaseManager";
    public RealtimeDatabaseManager() {
        db = FirebaseDatabase.getInstance().getReference();
    }

    public void getTotalNumberOfChildren(String path, final FirebaseCallback firebaseCallback) {
        db.child(path).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalNumberOfChildren = snapshot.getChildrenCount();
                firebaseCallback.onCallback(totalNumberOfChildren);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getSnapshot(String path, final FirebaseCallback firebaseCallback) {
        db.child(path).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                firebaseCallback.onCallback(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getValueOfChild(String childPath, final FirebaseCallback firebaseCallback) {
        db.child(childPath).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                firebaseCallback.onCallback(snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setValue(String childPath, String childValue) {
        db.child(childPath).setValue(childValue).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Added value successfully to Realtime DB");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to add value to the Realtime DB");
            }
        });
    }

    public void getAllTheChildValuesInHashMap(String childPath, final FirebaseCallback firebaseCallback) {
        db.child(childPath).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> data = new HashMap<>();
                int childrenCount = (int) snapshot.getChildrenCount();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.getKey();
                    String value = ds.getValue().toString();

                    data.put(key, value);
                }

                firebaseCallback.onCallback(data);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getAllTheChildrenHashMapInArrayList(String childPath, final FirebaseCallback firebaseCallback) {
        db.child(childPath).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<HashMap<String, String>> myArrayList= new ArrayList<>();

                for (DataSnapshot ds: snapshot.getChildren()) {
                    HashMap<String, String> data = new HashMap<>();
                    data.put(ds.getKey(), ds.getValue().toString());
                    myArrayList.add(data);
                }

                firebaseCallback.onCallback(myArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void removeTheField(String path, String childName) {
        db.child(path).child(childName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Successfully removed the value from realtime DB");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to remove the value from realtime DB");
            }
        });
    }
}
