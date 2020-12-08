package edu.csun.compsci490.makefriendsapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LocationThread extends Thread {
    private String TAG = LocationThread.class.getName();

    private TextView locationThreadStatusTextView;
    private TextView locationUserProcessedTextView;
    private Context context;
    private Activity activity;

    private DatabaseManager databaseManager;
    private String status;
    private String locationMatchedStatus;
    private String userStatus;

    private ColorDrawable redColor;
    private ColorDrawable greenColor;

    private int comparingSize;
    private int comparingCurrentSize;

    private Thread thread1;
    private Thread thread2;

    private boolean canKillThread1and2;

    private String user1UID;
    private String user1Email;
    private String user1Value;
    private String user1Latitude;
    private String user1Longitude;

    private String user2UID;
    private String user2Email;
    private String user2Value;
    private String user2Latitude;
    private String user2Longitude;

    private boolean isReadyToStop;
    private String order;

    public LocationThread (TextView locationThreadStatusTextView, TextView locationUserProcessedTextView, Context context, Activity activity) {
        this.locationThreadStatusTextView = locationThreadStatusTextView;
        this.locationUserProcessedTextView = locationUserProcessedTextView;
        this.context = context;
        this.activity = activity;

        databaseManager = new DatabaseManager();

        status = "Running";
        locationMatchedStatus = "Not Found";
        userStatus = "Continue";

        redColor = new ColorDrawable(ContextCompat.getColor(context, R.color.red2));
        greenColor = new ColorDrawable(ContextCompat.getColor(context, R.color.green));

        isReadyToStop = false;
        order = "Keep Going";

        addActionListenerToLocationQueue();
    }

    private void addActionListenerToLocationQueue() {
        Log.d(TAG, "adding EventListenerToLocationQueue");
        String locationQueueDocPath = "Connecting/Location Queue";

        databaseManager.getDocumentReference(locationQueueDocPath).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (status.equals("Waiting")) {
                    status = "Running";
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (order.equals("Keep Going")) {
                        run();
                    }
                }
            }
        });
    }

    public void run() {
        super.run();

        if (order.equals("Keep Going")) {
            locationThreadStatusTextView.setTextColor(greenColor.getColor());
            locationUserProcessedTextView.setTextColor(greenColor.getColor());
            appendToLocationProcessedTextView("running method processed");
            Log.d(TAG, "Starting Location Run()");

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
                locationThreadStatusTextView.setText("Waiting");
                locationThreadStatusTextView.setTextColor(redColor.getColor());
                appendToLocationProcessedTextView("Waiting for next request");

            } else {
                Log.d(TAG, "Status: " + status);
                try {
                    Log.d(TAG, "From Run() sem acquired");
                    checkIfTheresAnyOneInTheQueue();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            locationUserProcessedTextView.append("Stopped.\n");
            locationUserProcessedTextView.setTextColor(redColor.getColor());
            locationThreadStatusTextView.setTextColor(redColor.getColor());
            isReadyToStop = true;
        }

    }

    private void appendToLocationProcessedTextView(final String string) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationUserProcessedTextView.append(string + "...\n");
                locationUserProcessedTextView.setMovementMethod(new ScrollingMovementMethod());
            }
        });
    }

    private void checkIfTheresAnyOneInTheQueue() {
        Log.d(TAG, "checkIfTheresAnyOneInTheQueue from Location Thread");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationThreadStatusTextView.setText("Processing");
                locationThreadStatusTextView.setTextColor(greenColor.getColor());
            }
        });


        String courseDocPath = "Connecting/Location Queue";

        databaseManager.getDocumentSnapshot(courseDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DocumentSnapshot snapshot = (DocumentSnapshot) value;

                if (snapshot.getData().size() == 0) {
                    //no ones in the queue
                    status = "Waiting";
                    locationThreadStatusTextView.setText("Waiting");
                    locationThreadStatusTextView.setTextColor(redColor.getColor());
                    //locationUserProcessedTextView.setText("No one's in the queue from checkIfTheresAnyOneInTheQueue()");
                    appendToLocationProcessedTextView("No one is in the queue");
                    appendToLocationProcessedTextView("Waiting for next request");
                } else {
                    appendToLocationProcessedTextView("Getting first person in the queue");
                    getFirstPersonInTheQueueAndLockIt(snapshot);
                }
            }
        });
    }

    private void getFirstPersonInTheQueueAndLockIt(final DocumentSnapshot snapshot) {
        Log.d(TAG, "getFirstPersonInTheQueueAndLockIt from Location Thread");

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
                appendToLocationProcessedTextView("Checking if user1 is canceling");
                String cancelingDocPath = user1Email + "/More Info";
                String cancelingFieldName = "Canceling";
                databaseManager.getFieldValue(cancelingDocPath, cancelingFieldName, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        String status = value.toString();
                        if (status.equals("false")) {
                            appendToLocationProcessedTextView("User one is not canceling");
                            appendToLocationProcessedTextView(user1Email + " is being processed");
                            //locationUserProcessedTextView.setText(user1Email);

                            user1Value = snapshot.get(user1UID).toString();

                            String user1CanCancelSearchingDocPath = user1Email + "/Search Canceling";
                            String fieldName = "Can Cancel Searching";

                            //locking user1
                            appendToLocationProcessedTextView("locking " + user1Email);
                            databaseManager.updateTheField(user1CanCancelSearchingDocPath, fieldName, "false");

                            //removing user1 from the queue
                            appendToLocationProcessedTextView("Removing " + user1Email + " from the queue");
                            String courseQueueDocPath = "Connecting/Location Queue";

                            databaseManager.deleteField(courseQueueDocPath, user1UID);
                            Log.d(TAG, "deleted user1 from the Location queue");
                            appendToLocationProcessedTextView(user1Email + " removed from the queue");
                            try {
                                sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            checkIfTheresAnyoneInTheListToCompareWith();
                        } else {
                            appendToLocationProcessedTextView("User1 is canceling");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            run();
                        }
                    }
                });
            }
        });
    }

    private void checkIfTheresAnyoneInTheListToCompareWith() {
        appendToLocationProcessedTextView("Checking if there's anyone to compare " + user1Email + " with");
        Log.d(TAG, "checkIfTheresAnyoneInTheListToCompareWith From Location Thread");

        final String courseUsersDocPath = "Connecting/Location";

        databaseManager.getDocumentSnapshot(courseUsersDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DocumentSnapshot snapshot = (DocumentSnapshot) value;
                Log.d(TAG, "Number of people in the Location list: " + snapshot.getData().size());
                if (snapshot.getData().size() == 0) {//if no ones in the list
                    appendToLocationProcessedTextView("There's non one to compare with");
                    checkIfUser1IsLookingForAnyUserOrOnlyLocationMatch(false, true);
                } else {
                    appendToLocationProcessedTextView("Getting another user to compare with");
                    getSecondUserFromTheListToCompareWithAndLockIt(snapshot);
                }
            }
        });
    }

    private void getSecondUserFromTheListToCompareWithAndLockIt(final DocumentSnapshot snapshot) {
        Log.d(TAG, "getSecondUserFromTheListToCompareWithAndLockIt from Location Thread");

        final ArrayList<String> keys = new ArrayList<>();

        keys.addAll(snapshot.getData().keySet());

        final String allUsersDocumentPath = "Default/All Users";

        comparingSize = keys.size();

        thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                appendToLocationProcessedTextView("Thread1 started");
                for (int i = 0; i < keys.size(); i++) {

                    try {
                        Log.d(TAG, "setting userStatus to wait from Location Thread");
                        userStatus = "Wait";
                        appendToLocationProcessedTextView("Got another user to compare with");
                        if (locationMatchedStatus.equals("Not Found") == false) {
                            Log.d(TAG, "Location matched status is true");

                            break;
                        }
                        continueGettingSecondUser(snapshot, allUsersDocumentPath, keys, comparingSize, i);
                        while(userStatus.equals("Wait")) {
                            sleep(2000);
                        }
                        Log.d(TAG, "userStatus = " + userStatus + " from Location Thread");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        sleep(1000);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }

                if (locationMatchedStatus.equals("Not Found")) {
                    appendToLocationProcessedTextView("No interests found with any of the users");
                    checkIfUser1IsLookingForAnyUserOrOnlyLocationMatch(true, true);
                } else {
                    appendToLocationProcessedTextView("Starting all over");
                    Log.d(TAG, "Going to call the super.run() From Location Thread");
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runTheSuperThread();
                }
            }
        });
        thread1.start();
    }

    private void runTheSuperThread() {
        canKillThread1and2 = true;
        locationMatchedStatus = "Not Found";
        run();
    }

    private void continueGettingSecondUser(final DocumentSnapshot snapshot, final String allUsersDocumentPath, final ArrayList<String> keys, final int comparingSize, final int i) {
        thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                appendToLocationProcessedTextView("Thread2 started");
                comparingCurrentSize = i;
                user2UID = keys.get(i);

                //getting user2 email
                appendToLocationProcessedTextView("Getting second user email");
                databaseManager.getFieldValue(allUsersDocumentPath, user2UID, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        user2Email = value.toString();
                        appendToLocationProcessedTextView("second user is " + user2Email);
                        //locationUserProcessedTextView.setText("Comparing " + user1Email + " and " + user2Email + " from Location Thread");
                        appendToLocationProcessedTextView("Comparing " + user1Email + " and " + user2Email);
                        user2Value = snapshot.get(user2UID).toString();

                        final String documentPath = user2Email + "/Search Canceling";
                        final String fieldName = "Can Cancel Searching";

                        //checking if user wants to cancel the searching
                        appendToLocationProcessedTextView("Checking if " + user2Email + " is canceling");
                        String user2MoreInfoDocPath = user2Email + "/More Info";
                        String cancelingFieldName = "Canceling";

                        databaseManager.getFieldValue(user2MoreInfoDocPath, cancelingFieldName, new FirebaseCallback() {
                            @Override
                            public void onCallback(Object value) {
                                String status = value.toString();

                                if (status.equals("true")) {//user 2 wants to cancel the search
                                    appendToLocationProcessedTextView(user2Email + " is canceling");
                                    databaseManager.updateTheField(documentPath, fieldName, "true");
                                    try {
                                        sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    userStatus = "Continue";
                                } else {
                                    //locking user2
                                    appendToLocationProcessedTextView(user2Email + " is not canceling");
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
        Log.d(TAG, "checkIfUser2IsAlreadyAContactWithUser1 from Interest Thread");
        //checking if this user already a contact with user1 or not
        appendToLocationProcessedTextView("Checking if " + user1Email + " and " + user1Email + " are already contacts");
        final String user1ContactsDocPath = user1Email + "/Contacts";
        String fieldName = "All Users";

        databaseManager.getFieldValue(user1ContactsDocPath, fieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                String dbValue = value.toString();

                if (dbValue.equals("none")) {//user don't have any contacts
                    appendToLocationProcessedTextView(user1Email + " and " + user2Email + " are not contacts");
                    checkIfUser2IsBlockedByUser1();
                } else {//user has contacts
                    ArrayList<String> user1ContactsList = (ArrayList) value;

                    for (int i = 0; i < user1ContactsList.size(); i++) {
                        if (user1ContactsList.get(i).equals(user2Email)) {//user2 is already a contact with user1
                            //unlocking user2
                            appendToLocationProcessedTextView(user2Email + " is already a contact with " + user1Email);
                            String user2MoreInfoDocPath = user2Email + "/Search Canceling";
                            String searchingFieldName = "Can Cancel Searching";
                            appendToLocationProcessedTextView("Unlocking " + user2Email);
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");
                            //locationUserProcessedTextView.setText("User 2 is already a contact with user 1 from Location Thread");
                            //try next person in the list to compare with
                            appendToLocationProcessedTextView("Getting next person in the line");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            userStatus = "Continue";
                            break;
                        }

                        if (i == user1ContactsList.size() - 1) {//user2 is not a contact with user1
                            appendToLocationProcessedTextView(user2Email + " is not a contact with " + user1Email);
                            checkIfUser2IsBlockedByUser1();
                        }
                    }
                }
            }
        });
    }

    private void checkIfUser2IsBlockedByUser1() {
        appendToLocationProcessedTextView("Checking if " + user2Email + " is blocked by " + user1Email);
        Log.d(TAG, "checkIfUser2IsBlockedByUser1 from Location Thread");
        String user1ContactsDocPath = user1Email + "/Contacts";
        final String blockedUsersFieldName = "Blocked Users";

        databaseManager.getFieldValue(user1ContactsDocPath, blockedUsersFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                String dbValue = value.toString();

                if (dbValue.equals("none")) {//user 1 has not blocked anyone
                    appendToLocationProcessedTextView(user1Email + " has not blocked " + user2Email);
                    checkIfUser1IsBlockedByUser2();

                } else {//user1 has blocked some users
                    ArrayList<String> blockedUsers = (ArrayList) value;

                    for (int i = 0; i < blockedUsers.size(); i++) {
                        if (blockedUsers.get(i).equals(user2Email)) {//user2 has been blocked by user1
                            appendToLocationProcessedTextView(user1Email + " has blocked " + user2Email);
                            //unlocking user2
                            String user2MoreInfoDocPath = user2Email + "/Search Canceling";
                            String searchingFieldName = "Can Cancel Searching";
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");
                            //locationUserProcessedTextView.setText("User 2 is blocked by user 1");
                            appendToLocationProcessedTextView("Getting next person in the list");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //try next person in the list o compare with
                            userStatus = "Continue";
                            break;
                        }

                        if (i == blockedUsers.size() - 1) {//user1 has not blocked user2
                            appendToLocationProcessedTextView(user1Email + " has not blocked " + user2Email);
                            checkIfUser1IsBlockedByUser2();

                        }
                    }
                }
            }
        });
    }

    private void checkIfUser1IsBlockedByUser2() {
        appendToLocationProcessedTextView("Checking if " + user1Email + " is blocked by " + user2Email);
        Log.d(TAG, "checkIfUser1IsBlockedByUser2 from Location Thread");
        String user1ContactsDocPath = user2Email + "/Contacts";
        final String blockedUsersFieldName = "Blocked Users";

        databaseManager.getFieldValue(user1ContactsDocPath, blockedUsersFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                String dbValue = value.toString();

                if (dbValue.equals("none")) {//user 2 has not blocked anyone
                    appendToLocationProcessedTextView(user2Email + " has not blocked " + user1Email);
                    getUser1LatitudeAndLongitude();

                } else {//user1 has blocked some users
                    ArrayList<String> blockedUsers = (ArrayList) value;

                    for (int i = 0; i < blockedUsers.size(); i++) {
                        if (blockedUsers.get(i).equals(user1Email)) {//user1 has been blocked by user2
                            appendToLocationProcessedTextView(user2Email + " has blocked " + user1Email);
                            //unlocking user2
                            String user2MoreInfoDocPath = user2Email + "/Search Canceling";
                            String searchingFieldName = "Can Cancel Searching";
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");
                            //locationUserProcessedTextView.setText("User 1 is blocked by user 2");
                            appendToLocationProcessedTextView("Getting next person in the list");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //try next person in the list o compare with
                            userStatus = "Continue";
                            break;
                        }

                        if (i == blockedUsers.size() - 1) {//user2 has not blocked user1
                            appendToLocationProcessedTextView(user2Email + " has not blocked " + user1Email);
                            getUser1LatitudeAndLongitude();

                        }
                    }
                }
            }
        });
    }
    private void getUser1LatitudeAndLongitude() {
        appendToLocationProcessedTextView("Getting " + user1Email + " Latitude and Longitude");
        Log.d(TAG, "getUser1LatitudeAndLongitude");
        String user1MoreInfoDocPath = user1Email + "/More Info";
        databaseManager.getAllDocumentDataInHashMap(user1MoreInfoDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                HashMap<String, Object> data = (HashMap) value;

                user1Latitude = data.get("Latitude").toString();
                user1Longitude = data.get("Longitude").toString();
                Log.d(TAG, "User1 Latitude and Longitude: " + user1Latitude + ", " + user1Longitude);
                getUser2LatitudeAndLongitude();
            }
        });
    }

    private void getUser2LatitudeAndLongitude() {
        appendToLocationProcessedTextView("Getting " + user2Email + " Latitude and Longitude");
        Log.d(TAG, "getUser2LatitudeAndLongitude");
        String user2MoreInfoDocPath = user1Email + "/More Info";
        databaseManager.getAllDocumentDataInHashMap(user2MoreInfoDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                HashMap<String, Object> data = (HashMap) value;

                user2Latitude = data.get("Latitude").toString();
                user2Longitude = data.get("Longitude").toString();
                Log.d(TAG, "User2 Latitude and Longitude: " + user2Latitude + ", " + user2Longitude);
                checkIfUser1And2AreWithin5Miles();
            }
        });
    }

    private void checkIfUser1And2AreWithin5Miles() {
        appendToLocationProcessedTextView("Checking if " + user1Email + " and " + user2Email + " are within 5 miles");
        Log.d(TAG, "checkIfUser1And2AreWithin5Miles");
        double user1Latitude = Double.valueOf(this.user1Latitude);
        double user1Longitude = Double.valueOf(this.user1Longitude);

        double user2Latitude = Double.valueOf(this.user2Latitude);
        double user2Longitude = Double.valueOf(this.user2Longitude);

        if ((user1Latitude == user2Latitude) && (user1Longitude == user2Longitude)) {
            //the distance between the users is 0. Connect them.
            appendToLocationProcessedTextView(user1Email + " and " + user2Email + " are 0 miles away from each other");
            appendToLocationProcessedTextView("Two Users within 5 miles founded");
            Log.d(TAG, "The distance between user1 and user2 is 0");
            locationMatchedStatus = "Found";
            connectUser1AndUser2();
        } else {
            double theta = user1Longitude - user2Longitude;
            double distance = Math.sin(Math.toRadians(user1Latitude)) * Math.sin(Math.toRadians(user2Latitude)) + Math.cos(Math.toRadians(user1Latitude) * Math.cos(Math.toRadians(user2Latitude)) * Math.cos(Math.toRadians(theta)));
            distance = Math.acos(distance);
            distance = Math.toDegrees(distance);
            distance = distance * 60 * 1.1515;
            Log.d(TAG, "The distance between User1 and User2: " + distance);
            appendToLocationProcessedTextView("Distance between " + user1Email + " and " + user2Email + " is " + distance + " miles");
            if (distance <= 5) {
                appendToLocationProcessedTextView("Two Users within 5 miles founded");
                locationMatchedStatus = "Found";
                connectUser1AndUser2();
            } else {
                //unlock user 2
                appendToLocationProcessedTextView(user1Email + " and " + user2Email + " are not within five miles");
                String documentPath = user2Email + "/Search Canceling";
                String fieldName = "Can Cancel Searching";
                databaseManager.updateTheField(documentPath, fieldName, "true");
                locationMatchedStatus = "Not Found";
                Log.d(TAG, "No Course Matched");
                if (comparingCurrentSize == comparingSize - 1) {//no location match with any of the users
                    appendToLocationProcessedTextView(user1Email + " is nearby none of the uses that are searching by location");
                    checkIfUser1IsLookingForAnyUserOrOnlyLocationMatch(true, false);
                } else {
                    appendToLocationProcessedTextView("Getting next person to compare with");
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    userStatus = "Continue";
                }
            }
        }
    }

    private void checkIfUser1IsLookingForAnyUserOrOnlyLocationMatch(boolean releaseComparingSem, boolean callRun) {
        appendToLocationProcessedTextView("Checking if " + user1Email + " is looking for only location match");
        Log.d(TAG, "checkIfUser1IsLookingForAnyUserOrOnlyLocationMatch From Location Thread");

        if (user1Value.equals("All")) {
            appendToLocationProcessedTextView(user1Email + " is not only looking for location match");
            String courseDocPath = "Connecting/Interest Queue";
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    locationUserProcessedTextView.setText("Moving " + user1Email + " to Interest Queue");
//                }
//            });
            appendToLocationProcessedTextView("Moving " + user1Email + " to the Interest Thread");
            databaseManager.createNewField(courseDocPath, user1UID, user1Value);
            databaseManager.updateTheField(user1Email + "/More Info", "User Is In Queue", "Interest Queue");
            unlockUser1(releaseComparingSem, callRun);
        } else {
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    locationUserProcessedTextView.setText("Adding user to location list");
//                }
//            });

            appendToLocationProcessedTextView("User is only looking for location match");

            addUser1ToTheList(releaseComparingSem, callRun);
        }
    }

    private void addUser1ToTheList(boolean releaseComparingSem, boolean callRun) {
        Log.d(TAG, "addUser1ToTheList From Location Thread");
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                locationUserProcessedTextView.setText("Adding " + user1Email + " to the list");
//            }
//        });

        appendToLocationProcessedTextView("Adding " + user1Email + " to the Location list");
        String courseDocPath = "Connecting/Location";

        databaseManager.createNewField(courseDocPath, user1UID, user1Value);

        //updating user1 whereabout
        appendToLocationProcessedTextView("Updating "  + user1Email + " whereabout in the database");
        databaseManager.updateTheField(user1Email + "/More Info", "User Is In Queue", "Location");

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        unlockUser1(releaseComparingSem, callRun);
    }

    private void unlockUser1(boolean releaseComparingSem, boolean callRun) {
        appendToLocationProcessedTextView("Unlocking " + user1Email);
        Log.d(TAG, "unlockUser1() from Location Thread");

        String user1DocumentPath = user1Email + "/Search Canceling";

        String fieldName = "Can Cancel Searching";
        databaseManager.updateTheField(user1DocumentPath, fieldName, "true");
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                locationUserProcessedTextView.setText("Starting all over");
//            }
//        });

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (releaseComparingSem == true && callRun == true) {
            appendToLocationProcessedTextView("Starting all over again");
            userStatus = "Continue";
            canKillThread1and2 = true;
            run();
        } else if (releaseComparingSem == true) {
            appendToLocationProcessedTextView("Getting next person in the line");
            userStatus = "Continue";
        } else if (callRun) {
            appendToLocationProcessedTextView("Starting all over again");
            //sem.release();
            canKillThread1and2 = true;
            run();
        }
    }

    private void connectUser1AndUser2() {
        appendToLocationProcessedTextView("Connecting " + user1Email + " and " + user2Email);
        Log.d(TAG, "connectUser1AndUser2 from Location Thread");
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                locationUserProcessedTextView.setText("Connecting users");
//            }
//        });

        setUpUser1();
    }

    private void setUpUser1() {
        appendToLocationProcessedTextView("Setting up " + user1Email);
        final String message = "You two have been linked because you both are within 5 miles radius!";

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
        appendToLocationProcessedTextView("Setting up " + user2Email);
        final String message = "You two have been linked because you both are within 5 miles radius!";
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
                removeUsers2FromLocationAndUnlockBothUsers();
            }
        });
    }

    private void removeUsers2FromLocationAndUnlockBothUsers() {
        appendToLocationProcessedTextView("Resetting everything for " + user1Email + " and " + user2Email);
        Log.d(TAG, "removeUsers2FromCourseAndUnlockBothUsers");
        //locationUserProcessedTextView.setText("Connecting complete, removing users from the Location list");
        //removing the user1 and 2 from Course list
        databaseManager.deleteField("Connecting/Location", user1UID);
        databaseManager.deleteField("Connecting/Location", user2UID);

        databaseManager.deleteField("Connecting/Location Queue", user1UID);
        databaseManager.deleteField("Connecting/Location Queue", user2UID);
        //unlocking Both users variables
        String user1MoreInfoDocumentPath = user1Email + "/More Info";
        String user1CanCancelSearchingDocPath = user1Email + "/Search Canceling";
        String user2MoreInfoDocumentPath = user2Email + "/More Info";
        String user2CanCancelSearchingDocPath = user2Email + "/Search Canceling";
        //unlocking user1
//        databaseManager.updateTheField(user1CanCancelSearchingDocPath, "Can Cancel Searching", "true");
//        databaseManager.updateTheField(user1MoreInfoDocumentPath, "Done Searching", "true");

        //resetting everything for user 1

        //enabling interest and Course Editing
        String profileSettingsDocPath = user1Email + "/Profile Page Settings";
        String enablingInterestFieldName = "Can Edit Interests";
        String enablingCourseFieldName = "Can Edit Courses";
        databaseManager.updateTheField(profileSettingsDocPath, enablingInterestFieldName, "true");
        databaseManager.updateTheField(profileSettingsDocPath, enablingCourseFieldName, "true");

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

        //resetting Latitude and Longitude
        databaseManager.updateTheField(user1MoreInfoDocumentPath, "Latitude", "none");
        databaseManager.updateTheField(user1MoreInfoDocumentPath, "Longitude", "none");

        //resetting Can Cancel Searching
        String searchCancelingDocPath = user1Email + "/Search Canceling";
        String canCancelSearchingFieldName = "Can Cancel Searching";
        databaseManager.updateTheField(searchCancelingDocPath, canCancelSearchingFieldName, "true");

        //resetting everything for user 2
        //enabling interest Editing
        String profileSettingsDocPath2 = user2Email + "/Profile Page Settings";
        String enablingInterestFieldName2 = "Can Edit Interests";
        String enablingCourseFieldName2 = "Can Edit Courses";
        databaseManager.updateTheField(profileSettingsDocPath2, enablingInterestFieldName2, "true");
        databaseManager.updateTheField(profileSettingsDocPath, enablingCourseFieldName2, "true");

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

        //resetting Latitude and Longitude
        databaseManager.updateTheField(user2MoreInfoDocumentPath, "Latitude", "none");
        databaseManager.updateTheField(user2MoreInfoDocumentPath, "Longitude", "none");

        //resetting Can Cancel Searching
        String searchCancelingDocPath2 = user2Email + "/Search Canceling";
        String canCancelSearchingFieldName2 = "Can Cancel Searching";
        databaseManager.updateTheField(searchCancelingDocPath2, canCancelSearchingFieldName2, "true");

        //unlocking user2
//        databaseManager.updateTheField(user2CanCancelSearchingDocPath, "Can Cancel Searching", "true");
//        databaseManager.updateTheField(user2MoreInfoDocumentPath, "Done Searching", "true");

        //locationUserProcessedTextView.setText("Starting all over");
        appendToLocationProcessedTextView("Everything's Complete");
        appendToLocationProcessedTextView("Starting all over again");

        try {
            Log.d(TAG, "Everything's Complete from Location Thread");
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        userStatus = "Continue";
    }

    public boolean isReadyToStop() {
        return isReadyToStop;
    }

    public void setReadyToStop(boolean readyToStop) {
        isReadyToStop = readyToStop;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getStatus() {
        return status;
    }
}
