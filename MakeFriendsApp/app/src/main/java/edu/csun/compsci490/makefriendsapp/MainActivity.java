package edu.csun.compsci490.makefriendsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    EditText mEmail, mPassword;
    Button mLoginBtn;
    TextView mCreateBtn, forgotPwd;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button;
        button = (Button) findViewById(R.id.btn_signUp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        Button button1;
        button1 = (Button) findViewById(R.id.btn_signIn);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        mEmail = findViewById(R.id.ed_email_input);
        mPassword = findViewById(R.id.ed_password_input);
        progressBar = findViewById(R.id.progressBar);
        firebaseAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.btn_login);
        forgotPwd = findViewById(R.id.tv_click_here);


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
                            Toast.makeText(MainActivity.this,"Logged In Successfully!",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),PlaceHolder.class));
                        }
                        else{
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
}