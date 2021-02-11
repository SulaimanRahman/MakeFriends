package edu.csun.compsci490.makefriendsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.auth.User;

import java.util.Map;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIMER = 1000;
    private SharedPreferences userLocalDatabase;
    private FirebaseAuth firebaseAuth;
    private UserSingleton userSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        userLocalDatabase = getSharedPreferences("userDetails", 0);
        userSingleton = UserSingleton.getInstance();
        createNotificationChannel();

        final Map<String, String> userData = (Map) userLocalDatabase.getAll();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (userData.size() == 0 || userData.size() == 2) {
                    //in the last version the userData size was 2 and we want that to be three now and that will change by loging in from the mainActivity page
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (userData.get("AutoLogin").equals("true")) {
                    automaticallySignInUser(userData.get("email") + "@my.csun.edu", userData.get("password"));
                } else if (userData.get("AutoLogin").equals("false")) {
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        },SPLASH_TIMER);


    }

    private void automaticallySignInUser(final String email, final String password) {
        Log.d("SplashScreen", "Email: " + email + " password: " + password);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    userSingleton.setEmail(email);
                    userSingleton.setPassword(password);
                    Toast.makeText(SplashScreen.this,"Logged In Successfully!",Toast.LENGTH_SHORT).show();

                    if (userSingleton.getEmail().equals("admin@my.csun.edu")) {
                        Intent serverPage = new Intent(getApplicationContext(), ServerPage.class);
                        startActivity(serverPage);
                    } else {
                        startActivity(new Intent(SplashScreen.this,MainNavigation.class));
                    }
                    finish();
                }
                else{
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "exampleServiceChannel",
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            manager.createNotificationChannel(serviceChannel);

        }
    }
}