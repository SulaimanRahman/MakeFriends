package edu.csun.compsci490.makefriendsapp;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Freezable;
import android.provider.ContactsContract;
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
    private String order;
    private DatabaseManager databaseManager;
    private int user0Number;
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

    private Semaphore comparingSem = new Semaphore(1);
    private int comparingSize, comparingCurrentSize;

    public InterestThread(TextView interestThreadStatusTextView, TextView interestUserProcessedTextView, Context context, Semaphore sem) {
        this.interestThreadStatusTextView = interestThreadStatusTextView;
        this.interestUserProcessedTextView = interestUserProcessedTextView;
        realtimeDatabaseManager = new RealtimeDatabaseManager();
        order = "running";
        databaseManager = new DatabaseManager();
        this.sem = sem;
        firebaseAuth = FirebaseAuth.getInstance();

        this.context = context;
        redColor = new ColorDrawable(ContextCompat.getColor(context, R.color.red2));
        greenColor = new ColorDrawable(ContextCompat.getColor(context, R.color.green));
        status = "Running";
        interestFoundStatus = "Not Found";
        addActionListenerToInterestQueue();
    }

    private void addActionListenerToInterestQueue() {
        Log.d(TAG, "Adding EventListenerToInterestQueue");
        String interestQueueDocPath = "Connecting/Interest Queue";

        databaseManager.getDocumentReference(interestQueueDocPath).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (status.equals("Waiting")) {
                    status = "Running";
                    sem.release();
                    run();
                }
            }
        });
    }


    @Override
    public void run() {
        super.run();
        Log.d(TAG, "Starting Run()");
        //while (status.equals("Keep running") || status.equals("Waiting")) {
        if (status.equalsIgnoreCase("Waiting")) {
            //do nothing
            interestThreadStatusTextView.setText("Waiting from run()");
            //Log.d(TAG, "Waiting");
        } else {
            Log.d(TAG, "In the while loop, status: " + status);
            try {
                sem.acquire();
                Log.d(TAG, "In the while loop, sem acquired");
                checkIfTheresAnyOneInTheQueue();
                //lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//            synchronized (lock) {
//
//            }

        //}

    }

    private void checkIfTheresAnyOneInTheQueue() {
        Log.d(TAG, "checkIfTheresAnyOneInTheQueue");
        interestThreadStatusTextView.setText("Processing");
        interestThreadStatusTextView.setBackground(greenColor);

        String interestDocPath = "Connecting/Interest Queue";

        databaseManager.getDocumentSnapshot(interestDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DocumentSnapshot snapshot = (DocumentSnapshot) value;

                if (snapshot.getData().size() == 0) {
                    //no ones in the queue
                    status = "Waiting";
                    interestThreadStatusTextView.setText("Waiting");
                    interestThreadStatusTextView.setBackground(redColor);
                    interestUserProcessedTextView.setText("No one's in the queue From Line 98");
                } else {
                    getFirstPersonInTheQueueAndLockIt(snapshot);
                }
            }
        });
    }

    private void getFirstPersonInTheQueueAndLockIt(final DocumentSnapshot snapshot) {
        Log.d(TAG, "getFirstPersonInTheQueueAndLockIt");

        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(snapshot.getData().keySet());

        user1UID = keys.get(0);

        String allUsersDocumentPath = "Default/All Users";
        databaseManager.getFieldValue(allUsersDocumentPath, user1UID, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                user1Email = value.toString();
                interestUserProcessedTextView.setText(user1Email);

                user1Value = snapshot.get(user1UID).toString();

                String user1MoreInfoDoc = user1Email + "/More Info";
                String fieldName = "Can Cancel Searching";

                //locking user1
                databaseManager.updateTheField(user1MoreInfoDoc, fieldName, "false");

                //removing user1 from the queue
                String interestQueueDocPath = "Connecting/Interest Queue";
                databaseManager.deleteField(interestQueueDocPath, user1UID);
                checkIfTheresAnyoneInTheListToCompareWith();

            }
        });
    }

    private void checkIfTheresAnyoneInTheListToCompareWith() {
        Log.d(TAG, "checkIfTheresAnyoneInTheListToCompareWith");

        final String interestUsersDocPath = "Connecting/Interest";

        databaseManager.getDocumentSnapshot(interestUsersDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DocumentSnapshot snapshot = (DocumentSnapshot) value;

                if (snapshot.getData().size() == 0) {//if no ones in the list
                    checkIfUser1IsLookingForAnyUserOrOnlyInterestMatch();//-----------------------------------------------------------
                } else {
                    getSecondUserFromTheListToCompareWithAndLockIt(snapshot);
                }
            }
        });
    }

    private void getSecondUserFromTheListToCompareWithAndLockIt(final DocumentSnapshot snapshot) {
        Log.d(TAG, "getSecondUserFromTheListToCompareWithAndLockIt");

        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(snapshot.getData().keySet());

        String allUsersDocumentPath = "Default/All Users";

        ArrayList<String> secondUserContacts = new ArrayList<>();

        comparingSize = keys.size();
        for (int i = 0; i < keys.size(); i++) {
            try {
                comparingSem.acquire();
                if (interestFoundStatus.equals("Not Found") == false) {
                    break;
                }

                comparingCurrentSize = i;
                user2UID = keys.get(i);
                databaseManager.getFieldValue(allUsersDocumentPath, user2UID, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        user2Email = value.toString();

                        user2Value = snapshot.get(user2UID).toString();

                        String documentPath = user2Email + "/More Info";
                        String fieldName = "Can Cancel Searching";

                        //locking user2
                        databaseManager.updateTheField(documentPath, fieldName, "false");

                        interestUserProcessedTextView.setText("Comparing " + user1Email + " and " + user2Email);

                        checkIfUser2IsAlreadyAContactWithUser1();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            comparingSem.acquire();
            if (interestFoundStatus.equals("Not Found")) {
                checkIfUser1IsLookingForAnyUserOrOnlyInterestMatch();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void checkIfUser2IsAlreadyAContactWithUser1() {
        //checking if this user already a contact with user1 or not
        final String user1ContactsDocPath = user1Email + "/Contacts";
        String fieldName = "All Users";

        databaseManager.getFieldValue(user1ContactsDocPath, fieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                if (value.getClass().isArray()) {//user has contacts
                    ArrayList<String> user1ContactsList = (ArrayList) value;

                    for (int i = 0; i < user1ContactsList.size(); i++) {
                        if (user1ContactsList.get(i).equals(user2Email)) {//user2 is already a contact with user1
                            //unlocking user2
                            String user2MoreInfoDocPath = user2Email + "/More Info";
                            String searchingFieldName = "Can Cancel Searching";
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");

                            //try next person in the list o compare with
                            comparingSem.release();
                            break;
                        }

                        if (i == user1ContactsList.size() - 1) {//user2 is not a contact with user1
                            checkIfUser2IsBlockedByUser1();
                        }
                    }


                } else {//user don't have any contacts
                    checkIfUser2IsBlockedByUser1();
                }
            }
        });
    }

    private void checkIfUser2IsBlockedByUser1() {
        String user1ContactsDocPath = user1Email + "/Contacts";
        final String blockedUsersFieldName = "Blocked Users";

        databaseManager.getFieldValue(user1ContactsDocPath, blockedUsersFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                if (value.getClass().isArray()) {//user1 has blocked some users
                    ArrayList<String> blockedUsers = (ArrayList) value;

                    for (int i = 0; i < blockedUsers.size(); i++) {
                        if (blockedUsers.get(i).equals(user2Email)) {//user2 has been blocked by user1
                            //unlocking user2
                            String user2MoreInfoDocPath = user2Email + "/More Info";
                            String searchingFieldName = "Can Cancel Searching";
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");

                            //try next person in the list o compare with
                            comparingSem.release();
                            break;
                        }

                        if (i == blockedUsers.size() - 1) {//user1 has not blocked user2
                            getFirstUserInterests();
                        }
                    }
                } else {//user 1 has not blocked anyone
                    getFirstUserInterests();
                }
            }
        });
    }

    private void getFirstUserInterests() {
        Log.d(TAG, "getSecondUserFromTheListToCompareWithAndLockIt");

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
        Log.d(TAG, "compareTheInterests");
        outerLoop:
        for (int i = 0; i < user1Interests.size(); i++) {
            for (int j = 0; j < user2Interests.size(); j++) {
                if (user1Interests.get(i).equals(user2Interests.get(j))) {
                    interestMatched = user1Interests.get(i);
                    interestFoundStatus = "Found";
                    comparingSem.release();
                    connectUser1AndUser2();
                    break outerLoop;
                }
            }
            if (i == user1Interests.size() - 1) {
                //unlock user 2
                String documentPath = user2Email + "/More Info";
                String fieldName = "Can Cancel Searching";
                databaseManager.updateTheField(documentPath, fieldName, "true");
                comparingSem.release();
            }
            if (comparingCurrentSize == comparingSize - 1) {//no interest match with any of the users
                checkIfUser1IsLookingForAnyUserOrOnlyInterestMatch();
            }
        }


    }

    private void checkIfUser1IsLookingForAnyUserOrOnlyInterestMatch() {
        Log.d(TAG, "checkIfUser1IsLookingForAnyUserOrOnlyInterestMatch");

        if (user1Value.equals("All")) {
            String courseDocPath = "Connecting/Course Queue";
            interestUserProcessedTextView.setText("Moving " + user1Email + " to Course Queue");
            databaseManager.createNewField(courseDocPath, user1UID, user1Value);
            unlockUser1();
        } else {
            interestUserProcessedTextView.setText("Adding user to interest list");
            addUser1ToTheList();
        }
    }

    private void addUser1ToTheList() {
        Log.d(TAG, "addUser1ToTheList");

        interestUserProcessedTextView.setText("Adding " + user1Email + " to the list");

        String interestDocPath = "Connecting/Interest";

        databaseManager.createNewField(interestDocPath, user1UID, user1Value);

        unlockUser1();
    }

    private void unlockUser1() {
        Log.d(TAG, "unlockUser1()");

        String user1DocumentPath = user1Email + "/More Info";
        // user2DocumentPath = user1Email + "/More Info";
        String fieldName = "Can Cancel Searching";
        databaseManager.updateTheField(user1DocumentPath, fieldName, "true");
        //databaseManager.updateTheField(user2DocumentPath, fieldName, "false");
        interestUserProcessedTextView.setText("Starting all over");
        sem.release();
        run();
    }

    private void connectUser1AndUser2() {
        Log.d(TAG, "connectUser1AndUser2");
        interestUserProcessedTextView.setText("Connecting users");
        final String message = "You two have been linked because you both like " + interestMatched + "!";

        final String user1ContactsDocumentPath = user1Email + "/Contacts";
        final String user1ChatDocumentPath = user1Email + "/Contacts/" + user2Email + "/Chat";
        final String user1ChatTimeDocumentPath = user1Email + "/Contacts/" + user2Email + "/Chat Time";
        final String user1MoreInfoDocumentPath = user1Email + "/Contacts/" + user2Email + "/More Info";

        final String user2ContactsDocumentPath = user2Email + "/Contacts";
        final String user2ChatDocumentPath = user2Email + "/Contacts/" + user1Email + "/Chat";
        final String user2ChatTimeDocumentPath = user2Email + "/Contacts/" + user1Email + "/Chat Time";
        final String user2MoreInfoDocumentPath = user2Email + "/Contacts/" + user1Email + "/More Info";

        final String contactsFieldName = "All Users";

        Date todaysDate = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm a");
        final String time = dateFormat.format(todaysDate);

        databaseManager.getFieldValue(user1ContactsDocumentPath, contactsFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                List<String> contacts = (ArrayList) value;
                contacts.add(user2Email);
                databaseManager.updateTheField(user1ContactsDocumentPath, contactsFieldName, contacts);

                databaseManager.createDocument(user1ChatDocumentPath, "Note0", message);
                databaseManager.createDocument(user1ChatTimeDocumentPath, "Time0", time);
                databaseManager.createDocument(user1MoreInfoDocumentPath, "Conversation Ended", "false");
                databaseManager.createNewField(user1MoreInfoDocumentPath, "OtherUserDeactivatedAccount", "false");
            }
        });

        databaseManager.getFieldValue(user2ContactsDocumentPath, contactsFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                List<String> contacts = new ArrayList<>();
                contacts.addAll((List)value);
                contacts.add(user1Email);
                databaseManager.updateTheField(user2ContactsDocumentPath, contactsFieldName, contacts);

                databaseManager.createDocument(user2ChatDocumentPath, "Note0", message);
                databaseManager.createDocument(user2ChatTimeDocumentPath, "Time0", time);
                databaseManager.createDocument(user2MoreInfoDocumentPath, "Conversation Ended", "false");
                databaseManager.createNewField(user2MoreInfoDocumentPath, "OtherUserDeactivatedAccount", "false");
            }
        });

        removeUsers2FromInterestAndUnlockBothUsers();
    }

    private void removeUsers2FromInterestAndUnlockBothUsers() {
        Log.d(TAG, "removeUsers2FromInterestAndUnlockBothUsers");
        interestUserProcessedTextView.setText("Connecting complete, removing users from the Interest list");
        //removing the user2 from interest list
        //databaseManager.deleteField("Connecting/Interest", user1UID);
        databaseManager.deleteField("Connecting/Interest", user2UID);

        //unlocking Both users variables
        String user1MoreInfoDocumentPath = user1Email + "/More Info";
        String user2MoreInfoDocumentPath = user2Email + "/More Info";

        //unlocking user1
        databaseManager.updateTheField(user1MoreInfoDocumentPath, "Can Cancel Searching", "true");
        databaseManager.updateTheField(user1MoreInfoDocumentPath, "Done Searching", "true");

        //unlocking user2
        databaseManager.updateTheField(user2MoreInfoDocumentPath, "Can Cancel Searching", "true");
        databaseManager.updateTheField(user2MoreInfoDocumentPath, "Done Searching", "true");

        interestUserProcessedTextView.setText("Starting all over");

        resetSomeVariables();

        run();
    }

    private void resetSomeVariables() {
        interestFoundStatus = "Not Found";
        sem = new Semaphore(1);
        comparingSem = new Semaphore(1);
    }


    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public void setUser0Number(int number) {
        user0Number = number;
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
