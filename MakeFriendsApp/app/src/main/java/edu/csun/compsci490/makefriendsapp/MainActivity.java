package edu.csun.compsci490.makefriendsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText mEmail, mPassword;
    Button mLoginBtn;
    TextView mCreateBtn, forgotPwd;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    private DatabaseManager databaseManager = new DatabaseManager();
    private UserSingleton userSingleton;
    private SharedPreferences userLocalDatabase;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userSingleton = UserSingleton.getInstance();
        databaseManager = new DatabaseManager();
        userLocalDatabase = getSharedPreferences("userDetails", 0);
        Map<String, String> userData = (Map) userLocalDatabase.getAll();


        Button button;
        button = (Button) findViewById(R.id.btn_signUp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
            }
        });

        Button button1;
        button1 = (Button) findViewById(R.id.btn_signIn);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mEmail = findViewById(R.id.ed_email_input);
        mPassword = findViewById(R.id.ed_password_input);
        progressBar = findViewById(R.id.progressBar);
        firebaseAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.btn_login);
        forgotPwd = findViewById(R.id.tv_click_here);

        if (userData.size() == 0) {

        } else {
            mEmail.setText(userData.get("email").toString());
            mPassword.setText(userData.get("password").toString());
        }

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                email += "@my.csun.edu";
                //Error checking for email and password
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required for Registration!");
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

                //Authentication

                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            saveLoginDetails();
                            userSingleton.setEmail(mEmail.getText().toString() + "@my.csun.edu");
                            userSingleton.setPassword(mPassword.getText().toString());
                            Toast.makeText(MainActivity.this,"Logged In Successfully!",Toast.LENGTH_SHORT).show();
                            Log.d("Main Activity", "Email: " + mEmail.getText().toString());

                            if (userSingleton.getEmail().equals("admin@my.csun.edu")) {
                                Intent serverPage = new Intent(getApplicationContext(), ServerPage.class);
                                startActivity(serverPage);
                            } else {
                                DocumentReference documentReference = db.collection(userSingleton.getEmail()).document("Profile");
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            Log.d("A1A", "Retrieving first login value...");
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            String firstLogin = documentSnapshot.get("isFirstLogin").toString();
                                            Log.d("A1A", "first login value: " + firstLogin);
                                            if(firstLogin.equals("true")){
                                                // go to tour
                                                Log.d("A1A", "Going to tour");
                                                Intent intent = new Intent(getApplicationContext(), Tour.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // was not users first time logging in so start home page
                                                Log.d("A1A", "Going to home");
                                                Log.d("A1A", "first login value 2: " + firstLogin);
                                                startActivity(new Intent(getApplicationContext(), MainNavigation.class));
                                                finish();
                                            }

                                        } else {
                                            Log.e("LOGINERROR", "isFirstLogin field does not exist in users db");
                                        }
                                    }
                                });

                            }

//                            Intent homePage = new Intent(getApplicationContext(), HomePage.class);
//                            startActivity(homePage);
                            finish();
                        }
                        else{
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("Main Activity", "Email: " + mEmail.getText().toString());
                            Toast.makeText(MainActivity.this,"Error! "+ task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });


        forgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resentEmail = new EditText(v.getContext());
                final AlertDialog.Builder passwordResetDialg = new AlertDialog.Builder(v.getContext());
                passwordResetDialg.setTitle("Reset Your Password!");
                passwordResetDialg.setMessage("Enter Your CSUN email!");
                passwordResetDialg.setView(resentEmail);

                passwordResetDialg.setPositiveButton("Send Link", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //sent reset link
                        String Email = resentEmail.getText().toString();
                        firebaseAuth.sendPasswordResetEmail(Email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "reset password link has been sent to your csun email!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordResetDialg.setNegativeButton("Back to Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //back to sign in page
                    }
                });
                passwordResetDialg.create().show();

            }
        });
    }

    private void saveLoginDetails() {
        SharedPreferences.Editor sharePreferenceEditor = userLocalDatabase.edit();
        sharePreferenceEditor.putString("email", mEmail.getText().toString());
        sharePreferenceEditor.putString("password", mPassword.getText().toString());
        sharePreferenceEditor.putString("AutoLogin", "true");
        sharePreferenceEditor.commit();
    }

}