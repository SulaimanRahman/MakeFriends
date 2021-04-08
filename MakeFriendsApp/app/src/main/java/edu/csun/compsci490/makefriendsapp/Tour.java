package edu.csun.compsci490.makefriendsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class Tour extends AppCompatActivity {
    private Button finishButton;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences userLocalDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);

        finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set the isFirstLogin variable to false
                userLocalDatabase = getSharedPreferences("userDetails", 0);
                Map<String, String> userData = (Map) userLocalDatabase.getAll();

                DatabaseManager dm = new DatabaseManager();
                String pathToFirstLogin = userData.get("email") + "@my.csun.edu/Profile";
                dm.updateTheField(pathToFirstLogin, "isFirstLogin", "false");
                // intent from this to MainNavigation
                Intent intent = new Intent(getApplicationContext(), MainNavigation.class);
                //start activity
                startActivity(intent);
                finish();
            }
        });

    }
}