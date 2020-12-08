package edu.csun.compsci490.makefriendsapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Freezable;
import android.provider.ContactsContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class InterestThread extends Thread {
    private TextView interestThreadStatusTextView;
    private TextView interestUserProcessedTextView;
    private RealtimeDatabaseManager realtimeDatabaseManager;
    private FirebaseAuth firebaseAuth;

    private DatabaseManager databaseManager;
    private String TAG = "Interest Thread";
    private String status;
    private Semaphore sem;

    private String user1Email;
    private String user1UID;
    private String user1Value;
    private ArrayList<String> user1Interests;

    private String user2Email;
    private String user2UID;
    private String user2Value;
    private ArrayList<String> user2Interests;

    private String interestMatched;
    private String interestFoundStatus;

    private ColorDrawable redColor;
    private ColorDrawable greenColor;

    private Context context;

    private Object lock = new Object();

    private ArrayList<String> UserUIDsAlreadyCompareWith = new ArrayList<>();

    private Semaphore comparingSem;
    private int comparingSize, comparingCurrentSize;

    private Activity activity;

    private String order;

    private Thread thread1;
    private Thread thread2;

    private String userStatus;

    private boolean canKillThread1and2;

    private boolean isReadyToStop;

    public InterestThread(TextView interestThreadStatusTextView, TextView interestUserProcessedTextView, Context context, Semaphore sem, Activity activity) {
        this.interestThreadStatusTextView = interestThreadStatusTextView;
        this.interestUserProcessedTextView = interestUserProcessedTextView;

        this.activity = activity;

        isReadyToStop = false;
        order = "Keep Going";

        databaseManager = new DatabaseManager();
        this.sem = sem;
        firebaseAuth = FirebaseAuth.getInstance();

        this.context = context;
        redColor = new ColorDrawable(ContextCompat.getColor(context, R.color.red2));
        greenColor = new ColorDrawable(ContextCompat.getColor(context, R.color.green));
        status = "Running";
        interestFoundStatus = "Not Found";
        addActionListenerToInterestQueue();
        comparingSem = new Semaphore(1);
        Log.d(TAG, "number of comparing Sem from constructor: " + comparingSem.availablePermits());
        userStatus = "Continue";
    }

    private void addActionListenerToInterestQueue() {
        Log.d(TAG, "Adding EventListenerToInterestQueue");
        String interestQueueDocPath = "Connecting/Interest Queue";

        databaseManager.getDocumentReference(interestQueueDocPath).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (status.equals("Waiting")) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    status = "Running";
                    if (order.equals("Keep Going")) {
                        sem.release();
                        run();
                    }
                }
            }
        });
    }


    @Override
    public void run() {
        super.run();
        if (order.equals("Keep Going")) {
            interestThreadStatusTextView.setTextColor(greenColor.getColor());
            interestUserProcessedTextView.setTextColor(greenColor.getColor());
            interestUserProcessedTextView.append("running method processed...\n");
            Log.d(TAG, "Starting Interest Run()");
            Log.d(TAG, "number of comparing Sem from run: " + comparingSem.availablePermits());


            try {
                Log.d(TAG, "Thread1  before join(): " + String.valueOf(thread1.getState()));
                Log.d(TAG, "Thread2 state before join(): " + thread2.getState());

                if(thread1.isAlive()) {
                    if (canKillThread1and2) {
                        thread1.stop();
                    } else {
                        try {
                            thread1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (thread2.isAlive()) {
                    if (canKillThread1and2) {
                        thread1.stop();
                    } else {
                        try {
                            thread2.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Log.d(TAG, "Thread1 state: " + String.valueOf(thread1.getState()));
                Log.d(TAG, "Thread2 state: " + thread2.getState());
            } catch (Exception e) {
                e.printStackTrace();
            }
            canKillThread1and2 = false;

            if (status.equalsIgnoreCase("Waiting")) {
                //do nothing
                interestThreadStatusTextView.setText("Waiting");
                interestThreadStatusTextView.setTextColor(redColor.getColor());
                interestUserProcessedTextView.append("Waiting for next request...\n");
            } else {
                Log.d(TAG, "Status: " + status);
//                try {
//                    sem.acquire();
//                    Log.d(TAG, "From Run() sem acquired");
//
//                    //lock.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                checkIfTheresAnyOneInTheQueue();
            }

        } else {
            isReadyToStop = true;
            interestUserProcessedTextView.append("Stopped.\n");
            interestUserProcessedTextView.setTextColor(redColor.getColor());
            interestThreadStatusTextView.setTextColor(redColor.getColor());
        }

    }

    private void appendToInterestProcessedTextView(final String string) {
        interestUserProcessedTextView.setText(string + "...\n");
        interestUserProcessedTextView.setMovementMethod(new ScrollingMovementMethod());
        Log.d(TAG, "TextView Lenth is: " + interestUserProcessedTextView.length());

    }

    private void checkIfTheresAnyOneInTheQueue() {
        Log.d(TAG, "checkIfTheresAnyOneInTheQueueInInterestThread");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                interestThreadStatusTextView.setText("Processing");
                interestThreadStatusTextView.setTextColor(greenColor.getColor());
                interestUserProcessedTextView.append("Checking if there's anyone in the queue...\n");
            }
        });


        String interestDocPath = "Connecting/Interest Queue";

        databaseManager.getDocumentSnapshot(interestDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DocumentSnapshot snapshot = (DocumentSnapshot) value;

                if (snapshot.getData().size() == 0) {
                    //no ones in the queue
                    status = "Waiting";
                    interestThreadStatusTextView.setText("Waiting");
                    interestThreadStatusTextView.setTextColor(redColor.getColor());
                    //interestUserProcessedTextView.setText("No one's in the queue from checkIfTheresAnyOneInTheQueue()");
                    interestUserProcessedTextView.append("No one is in the queue...\n");
                    interestUserProcessedTextView.append("Waiting for next request...\n");
                } else {
                    interestUserProcessedTextView.append("Getting first person in the queue...\n");
                    getFirstPersonInTheQueueAndLockIt(snapshot);
                }
            }
        });
    }

    private void getFirstPersonInTheQueueAndLockIt(final DocumentSnapshot snapshot) {
        Log.d(TAG, "getFirstPersonInTheQueueAndLockIt from Interest Thread");

        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(snapshot.getData().keySet());

        user1UID = keys.get(0);

        String allUsersDocumentPath = "Default/All Users";
        //getting user1 email
        databaseManager.getFieldValue(allUsersDocumentPath, user1UID, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                user1Email = value.toString();

                //check if user 1 is canceling
                interestUserProcessedTextView.append("Checking if user1 is canceling...\n");
                String cancelingDocPath = user1Email + "/More Info";
                String cancelingFieldName = "Canceling";
                databaseManager.getFieldValue(cancelingDocPath, cancelingFieldName, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        String status = value.toString();
                        if (status.equals("false")) {
                            interestUserProcessedTextView.append("User1 is not canceling...\n");
                            interestUserProcessedTextView.append(user1Email + " is being processed...\n");
                            //interestUserProcessedTextView.setText(user1Email);

                            user1Value = snapshot.get(user1UID).toString();

                            String user1CanCancelSearchingDocPath = user1Email + "/Search Canceling";
                            String fieldName = "Can Cancel Searching";

                            //locking user1
                            interestUserProcessedTextView.append("Locking " + user1Email + "...\n");
                            databaseManager.updateTheField(user1CanCancelSearchingDocPath, fieldName, "false");

                            //removing user1 from the queue
                            interestUserProcessedTextView.append("Removing " + user1Email + " from the queue...\n");
                            String interestQueueDocPath = "Connecting/Interest Queue";

                            databaseManager.deleteField(interestQueueDocPath, user1UID);
                            Log.d(TAG, "deleted user1 from the interest queue");
                            interestUserProcessedTextView.append(user1Email + " removed from the queue...\n");
                            try {
                                sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            checkIfTheresAnyoneInTheListToCompareWith();
                        } else {
                            interestUserProcessedTextView.append("User1 is canceling...\n");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            sem.release();
                            run();
                        }
                    }
                });


            }
        });
    }

    private void checkIfTheresAnyoneInTheListToCompareWith() {
        interestUserProcessedTextView.append("Checking if there's anyone to compare " + user1Email + " with...\n");
        Log.d(TAG, "checkIfTheresAnyoneInTheListToCompareWith From Interest Thread");

        final String interestUsersDocPath = "Connecting/Interest";

        databaseManager.getDocumentSnapshot(interestUsersDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DocumentSnapshot snapshot = (DocumentSnapshot) value;
                Log.d(TAG, "Number of people in the Interest list: " + snapshot.getData().size());
                if (snapshot.getData().size() == 0) {//if no ones in the list
                    interestUserProcessedTextView.append("There's no one to compare with...\n");
                    checkIfUser1IsLookingForAnyUserOrOnlyInterestMatch(false, true);
                } else {
                    //comparingSem.release();
                    interestUserProcessedTextView.append("Getting another user to compare with...\n");
                    getSecondUserFromTheListToCompareWithAndLockIt(snapshot);

                }
            }
        });
    }

    private void getSecondUserFromTheListToCompareWithAndLockIt(final DocumentSnapshot snapshot) {
        Log.d(TAG, "getSecondUserFromTheListToCompareWithAndLockIt from Interest Thread");

        final ArrayList<String> keys = new ArrayList<>();

        keys.addAll(snapshot.getData().keySet());

        final String allUsersDocumentPath = "Default/All Users";

        ArrayList<String> secondUserContacts = new ArrayList<>();

        comparingSize = keys.size();

        thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                interestUserProcessedTextView.append("Thread1 started");
                for (int i = 0; i < keys.size(); i++) {

                    try {

                        Log.d(TAG, "Number of comparing Sem " + comparingSem.availablePermits());


                        Log.d(TAG, "setting userStatus to wait from Interest Thread");
                        userStatus = "Wait";
                        interestUserProcessedTextView.append("Got another user to compare with...\n");
                        if (interestFoundStatus.equals("Not Found") == false) {
                            Log.d(TAG, "Interest found status is true");
                            break;
                        }
                        continueGettingSecondUser(snapshot, allUsersDocumentPath, keys, comparingSize, i);
                        while(userStatus.equals("Wait")) {
                            sleep(2000);
                        }
                        Log.d(TAG, "userStatus = " + userStatus + " from Interest Thread");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }



                    try {
                        sleep(1000);
                    } catch (Exception e){
                        e.printStackTrace();
                    }


                }

                if (interestFoundStatus.equals("Not Found")) {
                    appendToInterestProcessedTextView("No interests found with any of the users");
                    checkIfUser1IsLookingForAnyUserOrOnlyInterestMatch(true, true);
                } else {
                    appendToInterestProcessedTextView("Starting all over");
                    Log.d(TAG, "Going to call the super.run() from Interest Thread");
                    runTheSuperThread();
                }
            }
        });
        thread1.start();
    }

    private void runTheSuperThread() {
        canKillThread1and2 = true;
        interestFoundStatus = "Not Found";
        run();
    }

    private void continueGettingSecondUser(final DocumentSnapshot snapshot, final String allUsersDocumentPath, final ArrayList<String> keys, final int comparingSize, final int i) {
        thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                appendToInterestProcessedTextView("Thread2 started");
                comparingCurrentSize = i;
                user2UID = keys.get(i);

                //getting user2 email
                appendToInterestProcessedTextView("Getting second user email");
                databaseManager.getFieldValue(allUsersDocumentPath, user2UID, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        user2Email = value.toString();
                        appendToInterestProcessedTextView("second user is " + user2Email);
                        //interestUserProcessedTextView.setText("Comparing " + user1Email + " and " + user2Email + " from Interest Thread");
                        appendToInterestProcessedTextView("Comparing " + user1Email + " and " + user2Email);
                        user2Value = snapshot.get(user2UID).toString();

                        final String documentPath = user2Email + "/Search Canceling";
                        final String fieldName = "Can Cancel Searching";

                        //locking user2
                        //databaseManager.updateTheField(documentPath, fieldName, "false");

                        //checking if user wants to cancel the searching
                        appendToInterestProcessedTextView("Checking if " + user2Email + " is canceling");
                        String user2MoreInfoDocPath = user2Email + "/More Info";
                        String cancelingFieldName = "Canceling";

                        databaseManager.getFieldValue(user2MoreInfoDocPath, cancelingFieldName, new FirebaseCallback() {
                            @Override
                            public void onCallback(Object value) {
                                String status = value.toString();

                                if (status.equals("true")) {//user 2 wants to cancel the search
                                    //unlocking user2
                                    appendToInterestProcessedTextView(user2Email + " is canceling");
                                    databaseManager.updateTheField(documentPath, fieldName, "true");
                                    try {
                                        sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    userStatus = "Continue";
                                } else {
                                    //locking user2
                                    appendToInterestProcessedTextView(user2Email + " is not canceling");
                                    databaseManager.updateTheField(documentPath, fieldName, "false");
                                    checkIfUser2IsAlreadyAContactWithUser1();
                                }
                            }
                        });
                    }
                });
            }
        });
        thread2.start();

    }

    private void checkIfUser2IsAlreadyAContactWithUser1() {
        Log.d(TAG, "checkIfUser2IsAlreadyAContactWithUser1 From Interest Thread");
        //checking if this user already a contact with user1 or not
        appendToInterestProcessedTextView("Checking if " + user1Email + " and " + user1Email + " are already contacts");
        final String user1ContactsDocPath = user1Email + "/Contacts";
        String fieldName = "All Users";

        databaseManager.getFieldValue(user1ContactsDocPath, fieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                String dbValue = value.toString();

                if (dbValue.equals("none")) {//user don't have any contacts
                    appendToInterestProcessedTextView(user1Email + " and " + user2Email + " are not contacts");
                    checkIfUser2IsBlockedByUser1();
                } else {//user has contacts
                    ArrayList<String> user1ContactsList = (ArrayList) value;

                    for (int i = 0; i < user1ContactsList.size(); i++) {
                        if (user1ContactsList.get(i).equals(user2Email)) {//user2 is already a contact with user1
                            //unlocking user2
                            appendToInterestProcessedTextView(user2Email + " is already a contact with " + user1Email);
                            String user2MoreInfoDocPath = user2Email + "/Search Canceling";
                            String searchingFieldName = "Can Cancel Searching";
                            appendToInterestProcessedTextView("Unlocking " + user2Email);
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");
                            //interestUserProcessedTextView.setText("User 2 is already a contact with user 1 from Interest Thread");
                            //try next person in the list o compare with
                            appendToInterestProcessedTextView("Getting next person in the line");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //comparingSem.release();
                            userStatus = "Continue";

                            break;
                        }

                        if (i == user1ContactsList.size() - 1) {//user2 is not a contact with user1
                            appendToInterestProcessedTextView(user2Email + " is not a contact with " + user1Email);
                            checkIfUser2IsBlockedByUser1();
                        }
                    }
                }
            }
        });
    }

    private void checkIfUser2IsBlockedByUser1() {
        appendToInterestProcessedTextView("Checking if " + user2Email + " is blocked by " + user1Email);
        Log.d(TAG, "checkIfUser2IsBlockedByUser1 from Interest Thread");
        String user1ContactsDocPath = user1Email + "/Contacts";
        final String blockedUsersFieldName = "Blocked Users";

        databaseManager.getFieldValue(user1ContactsDocPath, blockedUsersFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                String dbValue = value.toString();

                if (dbValue.equals("none")) {//user 1 has not blocked anyone
                    appendToInterestProcessedTextView(user1Email + " has not blocked " + user2Email);
                    checkIfUser1IsBlockedByUser2();
                } else {//user1 has blocked some users
                    ArrayList<String> blockedUsers = (ArrayList) value;

                    for (int i = 0; i < blockedUsers.size(); i++) {
                        if (blockedUsers.get(i).equals(user2Email)) {//user2 has been blocked by user1
                            appendToInterestProcessedTextView(user1Email + " has blocked " + user2Email);
                            //unlocking user2
                            String user2MoreInfoDocPath = user2Email + "/Search Canceling";
                            String searchingFieldName = "Can Cancel Searching";
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");
                            //interestUserProcessedTextView.setText("User 2 is blocked by user 1");
                            appendToInterestProcessedTextView("Getting next person in the list");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //try next person in the list o compare with
                            //comparingSem.release();
                            userStatus = "Continue";
                            break;
                        }

                        if (i == blockedUsers.size() - 1) {//user1 has not blocked user2
                            appendToInterestProcessedTextView(user1Email + " has not blocked " + user2Email);
                            checkIfUser1IsBlockedByUser2();
                        }
                    }
                }
            }
        });
    }

    private void checkIfUser1IsBlockedByUser2() {
        appendToInterestProcessedTextView("Checking if " + user1Email + " is blocked by " + user2Email);
        Log.d(TAG, "checkIfUser1IsBlockedByUser2 from Interest Thread");
        String user1ContactsDocPath = user2Email + "/Contacts";
        final String blockedUsersFieldName = "Blocked Users";

        databaseManager.getFieldValue(user1ContactsDocPath, blockedUsersFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                String dbValue = value.toString();

                if (dbValue.equals("none")) {//user 2 has not blocked anyone
                    appendToInterestProcessedTextView(user2Email + " has not blocked " + user1Email);
                    getFirstUserInterests();
                } else {//user1 has blocked some users
                    ArrayList<String> blockedUsers = (ArrayList) value;

                    for (int i = 0; i < blockedUsers.size(); i++) {
                        if (blockedUsers.get(i).equals(user1Email)) {//user1 has been blocked by user2
                            appendToInterestProcessedTextView(user2Email + " has blocked " + user1Email);
                            //unlocking user2
                            String user2MoreInfoDocPath = user2Email + "/Search Canceling";
                            String searchingFieldName = "Can Cancel Searching";
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");
                            //interestUserProcessedTextView.setText("User 1 is blocked by user 2");
                            appendToInterestProcessedTextView("Getting next person in the list");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //try next person in the list o compare with
                            //comparingSem.release();
                            userStatus = "Continue";
                            break;
                        }

                        if (i == blockedUsers.size() - 1) {//user2 has not blocked user1
                            appendToInterestProcessedTextView(user2Email + " has not blocked " + user1Email);
                            getFirstUserInterests();
                        }
                    }
                }
            }
        });
    }

    private void getFirstUserInterests() {
        appendToInterestProcessedTextView("Getting " + user1Email + " interests");
        Log.d(TAG, "getFirstUserInterests");

        String documentPath = user1Email + "/More Info";
        String fieldName = "Interest Array";

        databaseManager.getFieldValue(documentPath, fieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                user1Interests = (ArrayList) value;
                getSecondUserInterests();
            }
        });
    }

    private void getSecondUserInterests() {
        appendToInterestProcessedTextView("Getting " + user2Email + " interests");
        Log.d(TAG, "getSecondUserInterests");
        String documentPath = user2Email + "/More Info";
        String fieldName = "Interest Array";

        databaseManager.getFieldValue(documentPath, fieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                user2Interests = (ArrayList) value;
                compareTheInterests();
            }
        });
    }

    private void compareTheInterests() {
        appendToInterestProcessedTextView("Comparing " + user1Email + " and " + user2Email + " interests");
        Log.d(TAG, "compareTheInterests");
        outerLoop:
        for (int i = 0; i < user1Interests.size(); i++) {
            for (int j = 0; j < user2Interests.size(); j++) {
                if (user1Interests.get(i).equals(user2Interests.get(j))) {
                    interestMatched = user1Interests.get(i);
                    interestFoundStatus = "Found";
                    appendToInterestProcessedTextView("Same interest found");
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //comparingSem.release();
                    connectUser1AndUser2();
                    break outerLoop;
                }
            }
            if (i == user1Interests.size() - 1) {
                //unlock user 2
                appendToInterestProcessedTextView("No similar interest found");
                String documentPath = user2Email + "/Search Canceling";
                String fieldName = "Can Cancel Searching";
                databaseManager.updateTheField(documentPath, fieldName, "true");
                interestFoundStatus = "Not Found";
                Log.d(TAG, "No Interest Matched");
                if (comparingCurrentSize == comparingSize - 1) {//no interest match with any of the users
                    appendToInterestProcessedTextView("Interests did not match with any of the users");
                    checkIfUser1IsLookingForAnyUserOrOnlyInterestMatch(true, false);
                } else {
                    appendToInterestProcessedTextView("Getting next person to compare with");
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //comparingSem.release();

                    userStatus = "Continue";
                }
            }

        }


    }

    private void checkIfUser1IsLookingForAnyUserOrOnlyInterestMatch(boolean releaseComparingSem, boolean callRun) {
        appendToInterestProcessedTextView("Checking if " + user1Email + " is looking for only Interest match");
        Log.d(TAG, "checkIfUser1IsLookingForAnyUserOrOnlyInterestMatch From Interest Thread");

        if (user1Value.equals("All")) {
            appendToInterestProcessedTextView(user1Email + " is not only looking for interest match");
            String courseDocPath = "Connecting/Course Queue";
            //interestUserProcessedTextView.setText("Moving " + user1Email + " to Course Queue");
            appendToInterestProcessedTextView("Moving " + user1Email + " to the Course Thread");
            databaseManager.createNewField(courseDocPath, user1UID, user1Value);
            databaseManager.updateTheField(user1Email + "/More Info", "User Is In Queue", "Course Queue");
            unlockUser1(releaseComparingSem, callRun);
        } else {
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    interestUserProcessedTextView.setText("Adding user to interest list");
//                }
//            });
            appendToInterestProcessedTextView("User is only looking for interest match");
            addUser1ToTheList(releaseComparingSem, callRun);
        }
    }

    private void addUser1ToTheList(boolean releaseComparingSem, boolean callRun) {
        Log.d(TAG, "addUser1ToTheList From Interest Thread");

//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                interestUserProcessedTextView.setText("Adding " + user1Email + " to the list");
//            }
//        });

        appendToInterestProcessedTextView("Adding " + user1Email + " to the Interests list");

        String interestDocPath = "Connecting/Interest";

        databaseManager.createNewField(interestDocPath, user1UID, user1Value);

        //updating user1 whereabout
        appendToInterestProcessedTextView("Updating "  + user1Email + " whereabout in the database");
        databaseManager.updateTheField(user1Email + "/More Info", "User Is In Queue", "Interest");

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        unlockUser1(releaseComparingSem, callRun);
    }

    private void unlockUser1(boolean releaseComparingSem, boolean callRun) {
        appendToInterestProcessedTextView("Unlocking " + user1Email);
        Log.d(TAG, "unlockUser1() From Interest Thread");

        String user1DocumentPath = user1Email + "/Search Canceling";
        // user2DocumentPath = user1Email + "/More Info";
        String fieldName = "Can Cancel Searching";
        databaseManager.updateTheField(user1DocumentPath, fieldName, "true");
        //databaseManager.updateTheField(user2DocumentPath, fieldName, "false");
//
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                interestUserProcessedTextView.setText("Starting all over");
//            }
//        });


        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (releaseComparingSem == true && callRun == true) {
            appendToInterestProcessedTextView("Starting all over again");
            userStatus = "Continue";
            canKillThread1and2 = true;
            run();
        } else if (releaseComparingSem == true) {
            //comparingSem.release();
            appendToInterestProcessedTextView("Getting next person in the line");
            userStatus = "Continue";
        } else if (callRun) {
            appendToInterestProcessedTextView("Starting all over again");
            sem.release();
            canKillThread1and2 = true;
            run();
        }
//        sem.release();
//        run();
    }

    private void connectUser1AndUser2() {
        appendToInterestProcessedTextView("Connecting " + user1Email + " and " + user2Email);
        Log.d(TAG, "connectUser1AndUser2 From Interest Thread");
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                interestUserProcessedTextView.setText("Connecting users");
//            }
//        });

        setUpUser1();
    }

    private void setUpUser1() {
        appendToInterestProcessedTextView("Setting up " + user1Email);
        final String message = "You two have been linked because you both like " + interestMatched + "!";

        final String user1ContactsDocumentPath = user1Email + "/Contacts";
        final String user1ChatDocumentPath = user1Email + "/Contacts/" + user2Email + "/Chat";
        final String user1ChatTimeDocumentPath = user1Email + "/Contacts/" + user2Email + "/Chat Time";
        final String user1MoreInfoDocumentPath = user1Email + "/Contacts/" + user2Email + "/More Info";

        final String contactsFieldName = "All Users";

        Date todaysDate = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm a");
        final String time = dateFormat.format(todaysDate);

        databaseManager.getFieldValue(user1ContactsDocumentPath, contactsFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                String dbValue = value.toString();
                if (dbValue.equals("none")) {
                    ArrayList<String> contacts = new ArrayList<>();
                    contacts.add(user2Email);
                    databaseManager.updateTheField(user1ContactsDocumentPath, contactsFieldName, contacts);

                    databaseManager.createDocument(user1ChatDocumentPath, "Note0", message);
                    databaseManager.createDocument(user1ChatTimeDocumentPath, "Time0", time);
                    databaseManager.createDocument(user1MoreInfoDocumentPath, "Conversation Ended", "false");
                    databaseManager.createNewField(user1MoreInfoDocumentPath, "OtherUserDeactivatedAccount", "false");
                    databaseManager.createNewField(user1MoreInfoDocumentPath, "Blocked User", "false");
                    databaseManager.createNewField(user1MoreInfoDocumentPath, "Conversation Ended From My Side", "false");
                    databaseManager.createNewField(user1MoreInfoDocumentPath, "All Messages Been Read", "false");

                } else {
                    List<String> contacts = (ArrayList) value;
                    contacts.add(user2Email);
                    databaseManager.updateTheField(user1ContactsDocumentPath, contactsFieldName, contacts);

                    databaseManager.createDocument(user1ChatDocumentPath, "Note0", message);
                    databaseManager.createDocument(user1ChatTimeDocumentPath, "Time0", time);
                    databaseManager.createDocument(user1MoreInfoDocumentPath, "Conversation Ended", "false");
                    databaseManager.createNewField(user1MoreInfoDocumentPath, "OtherUserDeactivatedAccount", "false");
                    databaseManager.createNewField(user1MoreInfoDocumentPath, "Blocked User", "false");
                    databaseManager.createNewField(user1MoreInfoDocumentPath, "Conversation Ended From My Side", "false");
                    databaseManager.createNewField(user1MoreInfoDocumentPath, "All Messages Been Read", "false");

                }

                setUpUser2();
            }
        });

    }

    private void setUpUser2() {
        appendToInterestProcessedTextView("Setting up " + user2Email);
        final String message = "You two have been linked because you both like " + interestMatched + "!";
        final String user2ContactsDocumentPath = user2Email + "/Contacts";
        final String user2ChatDocumentPath = user2Email + "/Contacts/" + user1Email + "/Chat";
        final String user2ChatTimeDocumentPath = user2Email + "/Contacts/" + user1Email + "/Chat Time";
        final String user2MoreInfoDocumentPath = user2Email + "/Contacts/" + user1Email + "/More Info";

        final String contactsFieldName = "All Users";

        Date todaysDate = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm a");
        final String time = dateFormat.format(todaysDate);

        databaseManager.getFieldValue(user2ContactsDocumentPath, contactsFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                String dbValue = value.toString();
                if (dbValue.equals("none")) {
                    ArrayList<String> contacts = new ArrayList<>();
                    contacts.add(user1Email);
                    databaseManager.updateTheField(user2ContactsDocumentPath, contactsFieldName, contacts);

                    databaseManager.createDocument(user2ChatDocumentPath, "Note0", message);
                    databaseManager.createDocument(user2ChatTimeDocumentPath, "Time0", time);
                    databaseManager.createDocument(user2MoreInfoDocumentPath, "Conversation Ended", "false");
                    databaseManager.createNewField(user2MoreInfoDocumentPath, "OtherUserDeactivatedAccount", "false");
                    databaseManager.createNewField(user2MoreInfoDocumentPath, "Blocked User", "false");
                    databaseManager.createNewField(user2MoreInfoDocumentPath, "Conversation Ended From My Side", "false");
                    databaseManager.createNewField(user2MoreInfoDocumentPath, "All Messages Been Read", "false");

                } else {
                    List<String> contacts = (ArrayList) value;
                    contacts.add(user1Email);
                    databaseManager.updateTheField(user2ContactsDocumentPath, contactsFieldName, contacts);

                    databaseManager.createDocument(user2ChatDocumentPath, "Note0", message);
                    databaseManager.createDocument(user2ChatTimeDocumentPath, "Time0", time);
                    databaseManager.createDocument(user2MoreInfoDocumentPath, "Conversation Ended", "false");
                    databaseManager.createNewField(user2MoreInfoDocumentPath, "OtherUserDeactivatedAccount", "false");
                    databaseManager.createNewField(user2MoreInfoDocumentPath, "Blocked User", "false");
                    databaseManager.createNewField(user2MoreInfoDocumentPath, "Conversation Ended From My Side", "false");
                    databaseManager.createNewField(user2MoreInfoDocumentPath, "All Messages Been Read", "false");

                }
                removeUsers2FromInterestAndUnlockBothUsers();
            }
        });
    }

    private void removeUsers2FromInterestAndUnlockBothUsers() {
        appendToInterestProcessedTextView("Resetting everything for " + user1Email + " and " + user2Email);
        Log.d(TAG, "removeUsers2FromInterestAndUnlockBothUsers");
        //interestUserProcessedTextView.setText("Connecting complete, removing users from the Interest list");
        //removing the user1 and 2 from interest list
        databaseManager.deleteField("Connecting/Interest", user1UID);
        databaseManager.deleteField("Connecting/Interest", user2UID);

        databaseManager.deleteField("Connecting/Interest Queue", user1UID);
        databaseManager.deleteField("Connecting/Interest Queue", user2UID);
        //unlocking Both users variables
        String user1MoreInfoDocumentPath = user1Email + "/More Info";
        String user1CanCancelSearchingDocPath = user1Email + "/Search Canceling";
        String user2MoreInfoDocumentPath = user2Email + "/More Info";
        String user2CanCancelSearchingDocPath = user2Email + "/Search Canceling";
        //unlocking user1
//        databaseManager.updateTheField(user1CanCancelSearchingDocPath, "Can Cancel Searching", "true");
//        databaseManager.updateTheField(user1MoreInfoDocumentPath, "Done Searching", "true");

        //resetting everything for user 1

        //enabling interest Editing
        String profileSettingsDocPath = user1Email + "/Profile Page Settings";
        String enablingInterestFieldName = "Can Edit Interests";
        databaseManager.updateTheField(profileSettingsDocPath, enablingInterestFieldName, "true");

        //resetting things in user More Info document
        String userMoreInfoDocPath = user1Email + "/More Info";

        //Resetting Canceling status
        String cancelingStatusFieldName = "Canceling";
        databaseManager.updateTheField(userMoreInfoDocPath, cancelingStatusFieldName, "false");



        //resetting user Queue
        String userQueueLocFieldName = "User Is In Queue";
        databaseManager.updateTheField(userMoreInfoDocPath, userQueueLocFieldName, "none");

        //resetting User Searching For
        String searchingForFieldName = "Searching For";
        databaseManager.updateTheField(userMoreInfoDocPath, searchingForFieldName, "none");

        //resetting Can Cancel Searching
        String searchCancelingDocPath = user1Email + "/Search Canceling";
        String canCancelSearchingFieldName = "Can Cancel Searching";
        databaseManager.updateTheField(searchCancelingDocPath, canCancelSearchingFieldName, "true");



        //resetting everything for user 2
        //enabling interest Editing
        String profileSettingsDocPath2 = user2Email + "/Profile Page Settings";
        String enablingInterestFieldName2 = "Can Edit Interests";
        databaseManager.updateTheField(profileSettingsDocPath2, enablingInterestFieldName, "true");

        //resetting things in user More Info document
        String userMoreInfoDocPath2 = user2Email + "/More Info";

        //Resetting Canceling status
        String cancelingStatusFieldName2 = "Canceling";
        databaseManager.updateTheField(userMoreInfoDocPath2, cancelingStatusFieldName2, "false");

        //resetting user Queue
        String userQueueLocFieldName2 = "User Is In Queue";
        databaseManager.updateTheField(userMoreInfoDocPath2, userQueueLocFieldName2, "none");

        //resetting User Searching For
        String searchingForFieldName2 = "Searching For";
        databaseManager.updateTheField(userMoreInfoDocPath2, searchingForFieldName2, "none");

        //resetting Can Cancel Searching
        String searchCancelingDocPath2 = user2Email + "/Search Canceling";
        String canCancelSearchingFieldName2 = "Can Cancel Searching";
        databaseManager.updateTheField(searchCancelingDocPath2, canCancelSearchingFieldName2, "true");

        //unlocking user2
//        databaseManager.updateTheField(user2CanCancelSearchingDocPath, "Can Cancel Searching", "true");
//        databaseManager.updateTheField(user2MoreInfoDocumentPath, "Done Searching", "true");

        //interestUserProcessedTextView.setText("Starting all over");
        appendToInterestProcessedTextView("Everything's Complete");
        appendToInterestProcessedTextView("Starting all over again");
        resetSomeVariables();

        try {
            Log.d(TAG, "Everything's Complete from Interest Thread");
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        userStatus = "Continue";
        //run();
    }

    private void resetSomeVariables() {
        //interestFoundStatus = "Not Found";
        sem = new Semaphore(1);
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        comparingSem = new Semaphore(1);
    }


    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public boolean isReadyToStop() {
        return isReadyToStop;
    }

    public void setReadyToStop(boolean readyToStop) {
        isReadyToStop = readyToStop;
    }

    public void releaseSem() {
        sem.release();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
