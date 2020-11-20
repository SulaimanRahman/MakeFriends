package edu.csun.compsci490.makefriendsapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
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
    private Thread courseThread;
    private Thread locationThread;

    private RealtimeDatabaseManager realtimeDatabaseManager;

    private String TAG = "ServerPage";
    private int firstTime = 0;

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

        signOutButton = findViewById(R.id.sign_out_button);
        startButton = findViewById(R.id.start_button);

        realtimeDatabaseManager = new RealtimeDatabaseManager();

        interestThread = new InterestThread(interestThreadStatusTextView, interestUserProcessedTextView, getApplicationContext(), interestSemaphore);
        courseThread = new Thread();
        locationThread = new Thread();

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

                    interestThread.run();
                    //setUpInterestThread();

                    //setUpCourseThread();

                    //setUpLocationThread();
                } else {
                    //successfully stops the threads
                    signOutButton.setEnabled(true);
                    signOutButton.setBackground(redColor);
                    startButton.setBackground(greenColor);
                    startButton.setText("Start");
//                    try {
//                        interestThread.join();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
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

    private void getUser0Number() {
        String childPath = "Interest/User0";
        realtimeDatabaseManager.getValueOfChild(childPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                int user0Number = Integer.parseInt(value.toString());
                interestThread.setUser0Number(user0Number);

            }
        });
    }


    private void setUpInterestThread() {
        String path = "Interest Queue";
        Log.d(TAG, "Setting Up Interest LIne 132");
        realtimeDatabaseManager.getSnapshot(path, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DataSnapshot dataSnapshot = (DataSnapshot) value;
                Log.d(TAG, "Setting Up Interest and first time = " + firstTime);
                if (dataSnapshot.getChildrenCount() == 1 && firstTime == 0) {
                    //do nothing
                    interestThread.setStatus("Waiting");
                    interestUserProcessedTextView.setText("No one's in the Queue");
                    Log.d(TAG, "total number of children are 1");
                    addEventListenerToInterestQueue();
                } else if (firstTime == 0) {
                    Log.d(TAG, "Starting the thread");
                    interestThread.setStatus("Keep running");
                    interestUserProcessedTextView.setText("Starting");
                    interestThread.start();
                    addEventListenerToInterestQueue();
                }

            }
        });
    }

    private void addEventListenerToInterestQueue() {
        String path = "Interest Queue";

        Log.d(TAG, "Adding EventListenerToInterestQueue");
        realtimeDatabaseManager.getSnapshot(path, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DataSnapshot snapshot = (DataSnapshot) value;
                snapshot.getRef().addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                        if (interestThread.isAlive()) {
//                            //do nothing
//                        } else if (interestThread.getState() == Thread.State.WAITING) {
//                            interestThread.notify();
//                        }

//                        if (interestThread.getStatus().equals("Waiting") == true && firstTime != 0) {
//                            if (firstTime == 1) {
//                                interestThread.start();
//                                interestUserProcessedTextView.setText("Starting");
//                            } else {
//                                interestThread.releaseSem();
//                            }
//
//                        } else {
//                            //do nothing
//                        }

                        if (interestThread.getStatus().equals("Waiting") == true && firstTime != 0) {
                            if (firstTime == 1) {
                                Log.d(TAG, "Starting from childEventListener");
                                interestThread.start();
                                interestUserProcessedTextView.setText("Starting");
                            } else {
                                Log.d(TAG, "Releasing a sem");
                                interestSemaphore.release();
                                interestThread.setStatus("Run");
                                interestThread.run();
                            }

                        } else {
                            //do nothing
                            Log.d(TAG, "Doing nothing from ChildEventListener");
                        }
                        firstTime++;
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }

    private void startInterestThread() {

    }

    private void setUpCourseThread() {

    }

    private void startCourseThread() {

    }

    private void setUpLocationThread() {

    }

    private void startLocationThread() {

    }
}
