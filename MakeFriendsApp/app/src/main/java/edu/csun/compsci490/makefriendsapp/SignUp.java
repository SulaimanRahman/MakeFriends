package edu.csun.compsci490.makefriendsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUp extends AppCompatActivity {
    EditText mFullName, mEmail, mPassword, mPhone;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mFullName = findViewById(R.id.editName);
        mEmail = findViewById(R.id.editEmail);
        mPassword = findViewById(R.id.editPassword);
        mPhone = findViewById(R.id.editPhone);
        mRegisterBtn = findViewById(R.id.btn_register);
        mLoginBtn = findViewById(R.id.btn_logIn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

        if(firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),PlaceHolder.class));
            finish();
        }



        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                //Error checking for email and password
                if(TextUtils.isEmpty(email)){
                  mEmail.setError("Email is Required for Registration");
                  return;
                }
                if(email.indexOf("@my.csun.edu")==-1){
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
                            Toast.makeText(SignUp.this,"Your Account has been Sucessfully Created!",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),PlaceHolder.class));
                        }
                        else{
                            Toast.makeText(SignUp.this,"Error! "+ task.getException().getMessage(),Toast.LENGTH_SHORT).show();

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