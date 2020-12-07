package edu.csun.compsci490.makefriendsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.concurrent.Semaphore;

import javax.inject.Scope;

import static java.lang.Thread.sleep;

public class ServerPage extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    private TextView interestThreadStatusTextView;
    private TextView interestUserProcessedTextView;

    private TextView courseThreadStatusTextView;
    private TextView courseUserProcessedTextView;

    private TextView locationThreadStatusTextView;
    private TextView locationUserProcessedTextView;

    private Button startButton;
    private Button signOutButton;

    private InterestThread interestThread;
    private CourseThread courseThread;
    private LocationThread locationThread;

    private RealtimeDatabaseManager realtimeDatabaseManager;

    private String TAG = ServerPage.class.getName();

    private Semaphore interestSemaphore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_layout);
        interestSemaphore = new Semaphore(1);

        firebaseAuth = FirebaseAuth.getInstance();

        interestThreadStatusTextView = findViewById(R.id.interest_thread_status_textview);
        interestUserProcessedTextView = findViewById(R.id.processing_interest_user_textview);

        courseThreadStatusTextView = findViewById(R.id.course_thread_status_textview);
        courseUserProcessedTextView = findViewById(R.id.processing_course_user_textview);

        locationThreadStatusTextView = findViewById(R.id.location_thread_status_textview);
        locationUserProcessedTextView = findViewById(R.id.processing_location_user_textview);

        Typeface customFont = Typeface.createFromAsset(getAssets(), "CCBold.ttf");

        interestUserProcessedTextView.setTypeface(customFont);
        interestThreadStatusTextView.setMovementMethod(new ScrollingMovementMethod());

        courseUserProcessedTextView.setTypeface(customFont);
        courseUserProcessedTextView.setMovementMethod(new ScrollingMovementMethod());

        locationUserProcessedTextView.setTypeface(customFont);
        locationUserProcessedTextView.setMovementMethod(new ScrollingMovementMethod());

        signOutButton = findViewById(R.id.sign_out_button);
        startButton = findViewById(R.id.start_button);

        realtimeDatabaseManager = new RealtimeDatabaseManager();

        interestThread = new InterestThread(interestThreadStatusTextView, interestUserProcessedTextView, getApplicationContext(), interestSemaphore, this);
        courseThread = new CourseThread(courseThreadStatusTextView, courseUserProcessedTextView, getApplicationContext(), this);
        locationThread = new LocationThread(locationThreadStatusTextView, locationUserProcessedTextView, getApplicationContext(), this);

        final ColorDrawable redColor = new ColorDrawable(getResources().getColor(R.color.red2));
        final ColorDrawable greenColor = new ColorDrawable(getResources().getColor(R.color.green));
        final ColorDrawable greyColor = new ColorDrawable(getResources().getColor(R.color.grey));


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startButton.getText().equals("Start")) {
                    signOutButton.setEnabled(false);
                    signOutButton.setBackground(greyColor);
                    startButton.setText("Stop");
                    startButton.setBackground(redColor);

                    interestThread.setOrder("Keep Going");
                    interestThread.setReadyToStop(false);

                    courseThread.setOrder("Keep Going");
                    courseThread.setReadyToStop(false);

                    locationThread.setOrder("Keep Going");
                    locationThread.setReadyToStop(false);

                    interestThread.run();
                    courseThread.run();
                    locationThread.run();

                } else {
                    //successfully stops the threads
                    interestThread.setOrder("Stop");
                    courseThread.setOrder("Stop");
                    locationThread.setOrder("Stop");

                    interestThread.run();

                    courseThread.run();
                    locationThread.run();

                    while (interestThread.isReadyToStop() == false || courseThread.isReadyToStop() == false || locationThread.isReadyToStop() == false) {

                    }

                    interestThread.interrupt();
                    courseThread.interrupt();
                    locationThread.interrupt();

                    interestThreadStatusTextView.setText("Stopped");
                    courseThreadStatusTextView.setText("Stopped");
                    locationThreadStatusTextView.setText("Stopped");

                    signOutButton.setEnabled(true);
                    signOutButton.setBackground(redColor);
                    startButton.setBackground(greenColor);
                    startButton.setText("Start");
                }
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                realtimeDatabaseManager.getTotalNumberOfChildren("All Queue", new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {

                    }
                });
                Intent signInPage = new Intent(ServerPage.this, MainActivity.class);
                startActivity(signInPage);
                finish();
            }
        });
    }
}
