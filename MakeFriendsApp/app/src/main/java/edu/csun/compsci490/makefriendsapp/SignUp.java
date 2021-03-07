package edu.csun.compsci490.makefriendsapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    private static final String TAG = "SignUp";
    EditText mFirstName, mLastName, mEmail, mPassword;
    //EditText mFullName, mEmail, mPassword, mPhone;
    Button mVerifyBtn, mLoginBtn;
    //Button mRegisterBtn;
    //TextView mLoginBtn;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    String userID;
    FirebaseFirestore firebaseFirestore;
    DatabaseManager databaseManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        mFirstName = findViewById(R.id.et_first_name);
        mLastName = findViewById(R.id.et_last_name);
        //mFullName = findViewById(R.id.editName);
        mEmail = findViewById(R.id.et_csun_email);
        mPassword = findViewById(R.id.et_password);
        //mPhone = findViewById(R.id.editPhone);
        mVerifyBtn = findViewById(R.id.btn_verify);
        mLoginBtn = findViewById(R.id.btn_signIn);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        firebaseFirestore = FirebaseFirestore.getInstance();

        databaseManager = new DatabaseManager();

        // THIS CAUSES APP CRASH
//        if(firebaseAuth.getCurrentUser() != null){
//            startActivity(new Intent(getApplicationContext(),PlaceHolder.class));
//            finish();
//        }



        mVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String firstName = mFirstName.getText().toString();
                final String lastName = mLastName.getText().toString();
                final String email = mEmail.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();

                //final String fullName = mFullName.getText().toString();
                //final String phoneNumber = mPhone.getText().toString();

                //Error checking for email and password
                if(TextUtils.isEmpty(email)){
                  mEmail.setError("Email is Required for Registration");
                  return;
                }
                if(!email.contains("@my.csun.edu")){
                    mEmail.setError("Must be CSUN provided Email!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Please Enter Your Password!");
                    return;
                }
                if(password.length() < 6){
                    mPassword.setError("Password must be longer than 6 characters!");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //register to firebase
                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //send verificaiton email
                            FirebaseUser cuser = firebaseAuth.getCurrentUser();
                            cuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignUp.this,"Verification Email has been sent!",Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                     Log.d(TAG,"onFailure: Email not sent!"+e.getMessage());
                                }
                            });

                            userID = firebaseAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = firebaseFirestore.collection(email).document("Profile");
                            Map<String,Object> user = new HashMap<>();

                            // may need to edit these values on here and in the firebase console
                            user.put("First Name",firstName);
                            user.put("Last Name", lastName);
                            user.put("Email",email);
                            user.put("Password", password);
                            user.put("UID",userID);
                            user.put("Biography", "null");
                            user.put("Profile Picture Uri", "Default/BlankProfilePic");
                            //user.put("Phone",123456);

                            String documentPath = "Default/All Users";
                            databaseManager.createNewField(documentPath, userID, email);

                            String userMoreInfoDocPath = email + "/More Info";
                            databaseManager.createDocument(userMoreInfoDocPath, "Canceling", "false");
                            databaseManager.createNewField(userMoreInfoDocPath, "Done Searching", "false");
                            databaseManager.createNewField(userMoreInfoDocPath, "Interest Array", "none");
                            databaseManager.createNewField(userMoreInfoDocPath, "Location", "none");
                            databaseManager.createNewField(userMoreInfoDocPath, "Searching For", "none");
                            databaseManager.createNewField(userMoreInfoDocPath, "Searching For What Course", "none");
                            databaseManager.createNewField(userMoreInfoDocPath, "User Is In Queue", "none");
                            databaseManager.createNewField(userMoreInfoDocPath, "Latitude", "none");
                            databaseManager.createNewField(userMoreInfoDocPath, "Longitude", "none");

                            String profileSettingsDocPath = email + "/Profile Page Settings";
                            databaseManager.createDocument(profileSettingsDocPath, "Can Edit Interests", "true");
                            databaseManager.createDocument(profileSettingsDocPath, "Can Edit Courses", "true");

                            String contactsDocPath = email + "/Contacts";
                            databaseManager.createDocument(contactsDocPath, "All Users", "none");
                            databaseManager.createNewField(contactsDocPath, "Blocked Users", "none");

                            String searchCancelingDocPath = email + "/Search Canceling";
                            databaseManager.createDocument(searchCancelingDocPath, "Can Cancel Searching", "true");

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"onSuccess: user profile is created for "+ userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Log.d(TAG,"onFailure: " + e.getMessage());
                                    Log.d(TAG,"Failed to save teh data");
                                }
                            });

                            Toast.makeText(SignUp.this,"Your Account has been Successfully Created!",Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else{
                            Toast.makeText(SignUp.this,"Error! "+ task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });


            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

    }
}