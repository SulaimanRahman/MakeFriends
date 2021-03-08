package edu.csun.compsci490.makefriendsapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MainNavigation extends AppCompatActivity {
    private UserSingleton userSingleton = UserSingleton.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String TAG = this.getClass().getName();
    private DatabaseManager databaseManager = new DatabaseManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);

        Intent serviceIntent = new Intent(this, NotificationService.class);
        serviceIntent.putExtra("startingMessage", "running");
        startService(serviceIntent);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_bar);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new HomeFragment()).commit();


    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch(item.getItemId()){
                case R.id.homeFragment:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.chatsFragment:
                    selectedFragment = new ChatsFragment();
                    break;
                case R.id.friendsFragment:
                    selectedFragment = new FriendsFragment();
                    break;
                case R.id.settingsFragment:
                    selectedFragment = new SettingsFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, selectedFragment).commit();
            return true;
        }
    };

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
////        if (userSingleton.getEmail() != null) {
////            Log.d(TAG, "Email is not Null");
////            BroadcastReceiver br = new MyBroadcastReceiver();
////
////            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
////            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
////            getApplicationContext().registerReceiver(br, filter);
////
////            Intent broadcastIntent = new Intent("restartNotificationService");
////            sendBroadcast(broadcastIntent);
//////            firebaseAuth.signInWithEmailAndPassword(userSingleton.getEmail(), userSingleton.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//////                @Override
//////                public void onComplete(@NonNull Task<AuthResult> task) {
//////                    Log.d(TAG, "OnDestroy in mainNavigation");
//////
//////                }
//////            });
////
////        } else {
////            Log.d(TAG, "User has sign out");
////        }
//    }

}
