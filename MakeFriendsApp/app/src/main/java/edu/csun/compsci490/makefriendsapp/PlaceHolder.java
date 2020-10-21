package edu.csun.compsci490.makefriendsapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class PlaceHolder extends AppCompatActivity {

    //TextView fullName,email,phone,displayMsg;
    TextView fullName, email, displayMsg;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    String userID;
    Button sendCode, logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_holder);
        // initialize variables
        //phone = findViewById(R.id.yourPhone);
        fullName = findViewById(R.id.yourName);
        email = findViewById(R.id.yourEmail);
        logOut = (Button) findViewById(R.id.btn_logOut);
        sendCode = findViewById(R.id.verifyBtn);
        displayMsg = findViewById(R.id.verifyMsg);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userID = firebaseAuth.getCurrentUser().getUid();
        final FirebaseUser user = firebaseAuth.getCurrentUser();

        if(!user.isEmailVerified()){
            sendCode.setVisibility(View.VISIBLE);
            displayMsg.setVisibility(View.VISIBLE);

            sendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(view.getContext(),"Verification Email has been sent!",Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag","onFailure: Email not sent!"+e.getMessage());
                        }
                    });
                }
            });
        }

        DocumentReference documentReference = firebaseFirestore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                //phone.setText(value.getString("Phone"));
                fullName.setText(value.getString("First Name").concat(" ").concat(value.getString("Last Name")));
                email.setText(value.getString("Email"));
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

    }


//    public void logOut(View view){
//        FirebaseAuth.getInstance().signOut();
//        //startActivity(new Intent(getApplicationContext(),MainActivity.class));
//        finish();
//    }




}