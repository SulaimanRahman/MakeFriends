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
import java.util.List;
import java.util.concurrent.Semaphore;

public class CourseThread extends Thread {

    private TextView courseThreadStatusTextView;
    private TextView courseUserProcessedTextView;

    private DatabaseManager databaseManager;
    private String TAG = "Course Thread";
    private String status;

    private String user1Email;
    private String user1UID;
    private String user1Value;

    private String user2Email;
    private String user2UID;
    private String user2Value;

    private String courseMatched;
    private String courseFoundStatus;

    private ColorDrawable redColor;
    private ColorDrawable greenColor;

    private Context context;

    private int comparingSize, comparingCurrentSize;

    private Activity activity;

    private Thread thread1;
    private Thread thread2;

    private String userStatus;

    private ArrayList<String> user1Courses;
    private boolean haveGotUser1Courses;
    private ArrayList<DocumentSnapshot> user1CoursesDocuments;
    private String nameOfTheCourseThatMatched;

    private boolean canKillThread1and2;

    private boolean isReadyToStop;
    private String order;

    public CourseThread (TextView courseThreadStatusTextView, TextView courseUserProcessedTextView, Context context, Activity activity) {
        this.courseThreadStatusTextView = courseThreadStatusTextView;
        this.courseUserProcessedTextView = courseUserProcessedTextView;
        this.context = context;
        this.activity = activity;

        databaseManager = new DatabaseManager();

        status = "Running";
        courseFoundStatus = "Not Found";
        userStatus = "Continue";

        redColor = new ColorDrawable(ContextCompat.getColor(context, R.color.red2));
        greenColor = new ColorDrawable(ContextCompat.getColor(context, R.color.green));

        user1Courses = new ArrayList<>();
        user1CoursesDocuments = new ArrayList<>();

        isReadyToStop = false;
        order = "Keep Going";

        addActionListenerToCourseQueue();
    }

    private void addActionListenerToCourseQueue() {
        Log.d(TAG, "adding EventListenerToCourseQueue");
        String courseQueueDocPath = "Connecting/Course Queue";

        databaseManager.getDocumentReference(courseQueueDocPath).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    //status = "Running";
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
            courseThreadStatusTextView.setTextColor(greenColor.getColor());
            courseUserProcessedTextView.setTextColor(greenColor.getColor());
            courseUserProcessedTextView.append("running method processed...\n");
            haveGotUser1Courses = false;
            Log.d(TAG, "Starting Course Run()");

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
                courseThreadStatusTextView.setText("Waiting");
                courseThreadStatusTextView.setTextColor(redColor.getColor());
                courseUserProcessedTextView.append("Waiting for next request...\n");

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
            courseUserProcessedTextView.setTextColor(redColor.getColor());
            courseThreadStatusTextView.setTextColor(redColor.getColor());
            courseThreadStatusTextView.setText("Stopped");
            courseUserProcessedTextView.append("Stopped.\n");
            isReadyToStop = true;
        }
    }

    private void appendToCourseProcessedTextView(final String string) {

        courseUserProcessedTextView.setText(string + "...\n");
        courseUserProcessedTextView.setMovementMethod(new ScrollingMovementMethod());

    }

    private void checkIfTheresAnyOneInTheQueue() {
        Log.d(TAG, "checkIfTheresAnyOneInTheQueueInCourseThread");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                courseThreadStatusTextView.setText("Processing");
                courseThreadStatusTextView.setTextColor(greenColor.getColor());
            }
        });


        String courseDocPath = "Connecting/Course Queue";

        databaseManager.getDocumentSnapshot(courseDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DocumentSnapshot snapshot = (DocumentSnapshot) value;

                if (snapshot.getData().size() == 0) {
                    //no ones in the queue
                    status = "Waiting";
                    courseThreadStatusTextView.setText("Waiting");
                    courseThreadStatusTextView.setTextColor(redColor.getColor());
                    //courseUserProcessedTextView.setText("No one's in the queue from checkIfTheresAnyOneInTheQueue()");
                    courseUserProcessedTextView.append("No one is in the queue..\n");
                    courseUserProcessedTextView.append("Waiting for next request...\n");
                } else {
                    appendToCourseProcessedTextView("Getting first person in the queue");
                    getFirstPersonInTheQueueAndLockIt(snapshot);
                }
            }
        });
    }

    private void getFirstPersonInTheQueueAndLockIt(final DocumentSnapshot snapshot) {
        Log.d(TAG, "getFirstPersonInTheQueueAndLockIt from Course Thread");

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
                appendToCourseProcessedTextView("Checking if user1 is canceling");
                String cancelingDocPath = user1Email + "/More Info";
                String cancelingFieldName = "Canceling";
                databaseManager.getFieldValue(cancelingDocPath, cancelingFieldName, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        String status = value.toString();
                        if (status.equals("false")) {
                            appendToCourseProcessedTextView("User one is not canceling");
                            appendToCourseProcessedTextView(user1Email + " is being processed");
                            //courseUserProcessedTextView.setText(user1Email);

                            user1Value = snapshot.get(user1UID).toString();

                            String user1CanCancelSearchingDocPath = user1Email + "/Search Canceling";
                            String fieldName = "Can Cancel Searching";

                            //locking user1
                            appendToCourseProcessedTextView("locking " + user1Email);
                            databaseManager.updateTheField(user1CanCancelSearchingDocPath, fieldName, "false");

                            //removing user1 from the queue
                            appendToCourseProcessedTextView("Removing " + user1Email + " from the queue");
                            String courseQueueDocPath = "Connecting/Course Queue";

                            databaseManager.deleteField(courseQueueDocPath, user1UID);
                            Log.d(TAG, "deleted user1 from the course queue");
                            appendToCourseProcessedTextView(user1Email + " removed from the queue");
                            try {
                                sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            checkIfTheresAnyoneInTheListToCompareWith();
                        } else {
                            appendToCourseProcessedTextView("User1 is canceling");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //sem.release();
                            run();
                        }
                    }
                });
            }
        });
    }

    private void checkIfTheresAnyoneInTheListToCompareWith() {
        appendToCourseProcessedTextView("Checking if there's anyone to compare " + user1Email + " with");
        Log.d(TAG, "checkIfTheresAnyoneInTheListToCompareWith From Course Thread");

        final String courseUsersDocPath = "Connecting/Course";

        databaseManager.getDocumentSnapshot(courseUsersDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DocumentSnapshot snapshot = (DocumentSnapshot) value;
                Log.d(TAG, "Number of people in the Course list: " + snapshot.getData().size());
                if (snapshot.getData().size() == 0) {//if no ones in the list
                    appendToCourseProcessedTextView("There's non one to compare with");
                    checkIfUser1IsLookingForAnyUserOrOnlyCourseMatch(false, true);
                } else {
                    appendToCourseProcessedTextView("Getting another user to compare with");
                    getSecondUserFromTheListToCompareWithAndLockIt(snapshot);
                }
            }
        });
    }

    private void getSecondUserFromTheListToCompareWithAndLockIt(final DocumentSnapshot snapshot) {
        Log.d(TAG, "getSecondUserFromTheListToCompareWithAndLockIt from Course Thread");

        final ArrayList<String> keys = new ArrayList<>();

        keys.addAll(snapshot.getData().keySet());

        final String allUsersDocumentPath = "Default/All Users";

        comparingSize = keys.size();

        thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                appendToCourseProcessedTextView("Thread1 started");
                for (int i = 0; i < keys.size(); i++) {

                    try {
                        Log.d(TAG, "setting userStatus to wait from Course Thread");
                        userStatus = "Wait";
                        appendToCourseProcessedTextView("Got another user to compare with");
                        if (courseFoundStatus.equals("Not Found") == false) {
                            Log.d(TAG, "Course found status is true");

                            break;
                        }
                        continueGettingSecondUser(snapshot, allUsersDocumentPath, keys, comparingSize, i);
                        while(userStatus.equals("Wait")) {
                            sleep(2000);
                        }
                        Log.d(TAG, "userStatus = " + userStatus + " from Course Thread");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        sleep(1000);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }

                if (courseFoundStatus.equals("Not Found")) {
                    appendToCourseProcessedTextView("No interests found with any of the users");
                    checkIfUser1IsLookingForAnyUserOrOnlyCourseMatch(true, true);
                } else {
                    appendToCourseProcessedTextView("Starting all over");
                    Log.d(TAG, "Going to call the super.run() From Course Thread");
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
        courseFoundStatus = "Not Found";
        run();
    }

    private void continueGettingSecondUser(final DocumentSnapshot snapshot, final String allUsersDocumentPath, final ArrayList<String> keys, final int comparingSize, final int i) {
        thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                appendToCourseProcessedTextView("Thread2 started");
                comparingCurrentSize = i;
                user2UID = keys.get(i);

                //getting user2 email
                appendToCourseProcessedTextView("Getting second user email");
                databaseManager.getFieldValue(allUsersDocumentPath, user2UID, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        user2Email = value.toString();
                        appendToCourseProcessedTextView("second user is " + user2Email);
                        //courseUserProcessedTextView.setText("Comparing " + user1Email + " and " + user2Email + " from Course Thread");
                        appendToCourseProcessedTextView("Comparing " + user1Email + " and " + user2Email);
                        user2Value = snapshot.get(user2UID).toString();

                        final String documentPath = user2Email + "/Search Canceling";
                        final String fieldName = "Can Cancel Searching";

                        //checking if user wants to cancel the searching
                        appendToCourseProcessedTextView("Checking if " + user2Email + " is canceling");
                        String user2MoreInfoDocPath = user2Email + "/More Info";
                        String cancelingFieldName = "Canceling";

                        databaseManager.getFieldValue(user2MoreInfoDocPath, cancelingFieldName, new FirebaseCallback() {
                            @Override
                            public void onCallback(Object value) {
                                String status = value.toString();

                                if (status.equals("true")) {//user 2 wants to cancel the search
                                    appendToCourseProcessedTextView(user2Email + " is canceling");
                                    databaseManager.updateTheField(documentPath, fieldName, "true");
                                    try {
                                        sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    userStatus = "Continue";
                                } else {
                                    //locking user2
                                    appendToCourseProcessedTextView(user2Email + " is not canceling");
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
        Log.d(TAG, "checkIfUser2IsAlreadyAContactWithUser1 from Course Thread");
        //checking if this user already a contact with user1 or not
        appendToCourseProcessedTextView("Checking if " + user1Email + " and " + user1Email + " are already contacts");
        final String user1ContactsDocPath = user1Email + "/Contacts";
        String fieldName = "All Users";

        databaseManager.getFieldValue(user1ContactsDocPath, fieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                String dbValue = value.toString();

                if (dbValue.equals("none")) {//user don't have any contacts
                    appendToCourseProcessedTextView(user1Email + " and " + user2Email + " are not contacts");
                    checkIfUser2IsBlockedByUser1();
                } else {//user has contacts
                    ArrayList<String> user1ContactsList = (ArrayList) value;

                    for (int i = 0; i < user1ContactsList.size(); i++) {
                        if (user1ContactsList.get(i).equals(user2Email)) {//user2 is already a contact with user1
                            //unlocking user2
                            appendToCourseProcessedTextView(user2Email + " is already a contact with " + user1Email);
                            String user2MoreInfoDocPath = user2Email + "/Search Canceling";
                            String searchingFieldName = "Can Cancel Searching";
                            appendToCourseProcessedTextView("Unlocking " + user2Email);
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");
                            //courseUserProcessedTextView.setText("User 2 is already a contact with user 1 from Course Thread");
                            //try next person in the list to compare with
                            appendToCourseProcessedTextView("Getting next person in the line");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            userStatus = "Continue";
                            break;
                        }

                        if (i == user1ContactsList.size() - 1) {//user2 is not a contact with user1
                            appendToCourseProcessedTextView(user2Email + " is not a contact with " + user1Email);
                            checkIfUser2IsBlockedByUser1();
                        }
                    }
                }
            }
        });
    }

    private void checkIfUser2IsBlockedByUser1() {
        appendToCourseProcessedTextView("Checking if " + user2Email + " is blocked by " + user1Email);
        Log.d(TAG, "checkIfUser2IsBlockedByUser1 from Course Thread");
        String user1ContactsDocPath = user1Email + "/Contacts";
        final String blockedUsersFieldName = "Blocked Users";

        databaseManager.getFieldValue(user1ContactsDocPath, blockedUsersFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                String dbValue = value.toString();
                Log.d(TAG, "Working line 350");
                if (dbValue.equals("none")) {//user 1 has not blocked anyone
                    appendToCourseProcessedTextView(user1Email + " has not blocked " + user2Email);
                    Log.d(TAG, "User1 has not blocked anyone");
                    checkIfUser1IsBlockedByUser2();
                } else {//user1 has blocked some users
                    ArrayList<String> blockedUsers = (ArrayList) value;

                    for (int i = 0; i < blockedUsers.size(); i++) {
                        if (blockedUsers.get(i).equals(user2Email)) {//user2 has been blocked by user1
                            //unlocking user2
                            appendToCourseProcessedTextView(user1Email + " has blocked " + user2Email);
                            String user2MoreInfoDocPath = user2Email + "/Search Canceling";
                            String searchingFieldName = "Can Cancel Searching";
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");
                            //courseUserProcessedTextView.setText("User 2 is blocked by user 1");
                            appendToCourseProcessedTextView("Getting next person in the list");
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //try next person in the list to compare with
                            userStatus = "Continue";
                            break;
                        }

                        if (i == blockedUsers.size() - 1) {//user1 has not blocked user2
                            appendToCourseProcessedTextView(user1Email + " has not blocked " + user2Email);
                            checkIfUser1IsBlockedByUser2();
//                            if (user1Value.equals("All")) {
//                                getUser1Courses();
//                            } else {
//                                compareUser1AndUser2Course();
//                            }
                        }
                    }
                }
            }
        });
    }

    private void checkIfUser1IsBlockedByUser2() {
        appendToCourseProcessedTextView("Checking if " + user1Email + " is blocked by " + user2Email);
        Log.d(TAG, "checkIfUser1IsBlockedByUser2 from Course Thread");
        String user1ContactsDocPath = user2Email + "/Contacts";
        final String blockedUsersFieldName = "Blocked Users";

        databaseManager.getFieldValue(user1ContactsDocPath, blockedUsersFieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                String dbValue = value.toString();

                if (dbValue.equals("none")) {//user 2 has not blocked anyone
                    appendToCourseProcessedTextView(user2Email + " has not blocked " + user1Email);
                    Log.d(TAG, "User2 has not blocked anyone");
                    getUser1Courses();
//                    if (user1Value.equals("All")) {
//                        getUser1Courses();
//                    } else {
//                        compareUser1AndUser2Course();
//                    }
                } else {//user2 has blocked some users
                    ArrayList<String> blockedUsers = (ArrayList) value;

                    for (int i = 0; i < blockedUsers.size(); i++) {
                        if (blockedUsers.get(i).equals(user1Email)) {//user1 has been blocked by user2
                            //unlocking user2
                            appendToCourseProcessedTextView(user2Email + " has blocked " + user1Email);
                            String user2MoreInfoDocPath = user2Email + "/Search Canceling";
                            String searchingFieldName = "Can Cancel Searching";
                            databaseManager.updateTheField(user2MoreInfoDocPath, searchingFieldName, "true");
                            //courseUserProcessedTextView.setText("User 1 is blocked by user 2");
                            appendToCourseProcessedTextView("Getting next person in the list");
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
                            appendToCourseProcessedTextView(user2Email + " has not blocked " + user1Email);
                            getUser1Courses();
//                            if (user1Value.equals("All")) {
//                                getUser1Courses();
//                            } else {
//                                compareUser1AndUser2Course();
//                            }
                        }
                    }
                }
            }
        });
    }

    private void getUser1Courses() {
        appendToCourseProcessedTextView("Getting " + user1Email + " Courses");
        Log.d(TAG, "getUser1Courses");
        if (haveGotUser1Courses) {
            compareUser1AndUser2Course();
        } else {
            final String user1CoursesCollectionPath = user1Email + "/More Info/Courses";
            databaseManager.getAllDocumentsInArrayListFromCollection(user1CoursesCollectionPath, new FirebaseCallback() {
                @Override
                public void onCallback(Object value) {
                    user1CoursesDocuments = (ArrayList) value;
                    compareUser1AndUser2Course();
                }
            });
//            databaseManager.getAllDocumentsNameInArrayListFromCollection(user1CoursesCollectionPath, new FirebaseCallback() {
//                @Override
//                public void onCallback(Object value) {
//                    final ArrayList<String> documentsName = (ArrayList) value;
//                    for (int i = 0; i < documentsName.size(); i++) {
//                        String documentPath = user1CoursesCollectionPath + "/" + documentsName.get(i);
//                        final int finalI = i;
//                        databaseManager.getFieldValue(documentPath, "Section", new FirebaseCallback() {
//                            @Override
//                            public void onCallback(Object value) {
//                                String sectionNumber = value.toString();
//                                user1Courses.add(sectionNumber);
//                                if (finalI == documentsName.size() - 1) {
//                                    haveGotUser1Courses = true;
//                                    compareUser1AndUser2Course();
//                                }
//                            }
//                        });
//                    }
//                }
//            });
        }

    }

    private void compareUser1AndUser2Course() {
        appendToCourseProcessedTextView("Comparing " + user1Email + " and " + user2Email + " courses");
        Log.d(TAG, "compareUser1AndUser2Course");
        if (user1Value.equals("All")) {
            for (int i = 0; i < user1CoursesDocuments.size(); i++) {
                String user1Course = user1CoursesDocuments.get(i).get("Section").toString();
                if (user1Course.equals(user2Value)) {//course found
                    appendToCourseProcessedTextView("Course matched");
                    courseMatched = user2Value;
                    courseFoundStatus = "Found";
                    nameOfTheCourseThatMatched = user1CoursesDocuments.get(i).get("Course").toString() + " " + user1CoursesDocuments.get(i).get("Course Number");
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    connectUser1AndUser2();
                } else {
                    //unlock user 2
                    appendToCourseProcessedTextView("Courses did not match");
                    String documentPath = user2Email + "/Search Canceling";
                    String fieldName = "Can Cancel Searching";
                    databaseManager.updateTheField(documentPath, fieldName, "true");
                    courseFoundStatus = "Not Found";
                    Log.d(TAG, "No Course Matched");
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    userStatus = "Continue";
//                    if (comparingCurrentSize == comparingSize - 1) {//no course match with any of the users
//                        checkIfUser1IsLookingForAnyUserOrOnlyCourseMatch(true, false);
//                    } else {
//                        try {
//                            sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        userStatus.equals("Continue");
//                    }
                }
            }
        } else {
            if (user1Value.equals(user2Value)) {//course matched
                appendToCourseProcessedTextView("Course matched");
                courseMatched = user1Value;
                courseFoundStatus = "Found";

                //getting the name of the course that matched
                for (int i = 0; i < user1CoursesDocuments.size(); i++) {
                    if (user1CoursesDocuments.get(i).get("Section").equals(user1Value)) {
                        String course = user1CoursesDocuments.get(i).get("Course").toString();
                        String courseNumber = user1CoursesDocuments.get(i).get("Course Number").toString();
                        nameOfTheCourseThatMatched = course + " " + courseNumber;
                        break;
                    }
                }

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                connectUser1AndUser2();
            } else {
                //unlock user 2
                appendToCourseProcessedTextView("Courses did not match");
                String documentPath = user2Email + "/Search Canceling";
                String fieldName = "Can Cancel Searching";
                databaseManager.updateTheField(documentPath, fieldName, "true");
                courseFoundStatus = "Not Found";
                Log.d(TAG, "No Course Matched");
                if (comparingCurrentSize == comparingSize - 1) {//no course match with any of the users
                    checkIfUser1IsLookingForAnyUserOrOnlyCourseMatch(true, false);
                } else {
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

    private void checkIfUser1IsLookingForAnyUserOrOnlyCourseMatch(boolean releaseComparingSem, boolean callRun) {
        appendToCourseProcessedTextView("Checking if " + user1Email + " is looking for only course match");
        Log.d(TAG, "checkIfUser1IsLookingForAnyUserOrOnlyCourseMatch From Course Thread");

        if (user1Value.equals("All")) {
            appendToCourseProcessedTextView(user1Email + " is not only looking for course match");
            String courseDocPath = "Connecting/Location Queue";
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    courseUserProcessedTextView.setText("Moving " + user1Email + " to Location Queue");
//                }
//            });
            appendToCourseProcessedTextView("Moving " + user1Email + " to the Location Thread");
            databaseManager.createNewField(courseDocPath, user1UID, user1Value);
            databaseManager.updateTheField(user1Email + "/More Info", "User Is In Queue", "Location Queue");
            unlockUser1(releaseComparingSem, callRun);
        } else {
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    courseUserProcessedTextView.setText("Adding user to course list");
//                }
//            });
            appendToCourseProcessedTextView("User is only looking for course match");
            addUser1ToTheList(releaseComparingSem, callRun);
        }
    }

    private void addUser1ToTheList(boolean releaseComparingSem, boolean callRun) {
        Log.d(TAG, "addUser1ToTheList From Course Thread");
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                courseUserProcessedTextView.setText("Adding " + user1Email + " to the list");
//            }
//        });

        appendToCourseProcessedTextView("Adding " + user1Email + " to the Course list");
        String courseDocPath = "Connecting/Course";

        databaseManager.createNewField(courseDocPath, user1UID, user1Value);

        //updating user1 whereabout
        appendToCourseProcessedTextView("Updating "  + user1Email + " whereabout in the database");
        databaseManager.updateTheField(user1Email + "/More Info", "User Is In Queue", "Course");

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        unlockUser1(releaseComparingSem, callRun);
    }

    private void unlockUser1(boolean releaseComparingSem, boolean callRun) {
        appendToCourseProcessedTextView("Unlocking " + user1Email);
        Log.d(TAG, "unlockUser1() from Course Thread");

        String user1DocumentPath = user1Email + "/Search Canceling";

        String fieldName = "Can Cancel Searching";
        databaseManager.updateTheField(user1DocumentPath, fieldName, "true");
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                courseUserProcessedTextView.setText("Starting all over from unlockUser1()");
//            }
//        });

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (releaseComparingSem == true && callRun == true) {
            appendToCourseProcessedTextView("Starting all over again");
            userStatus = "Continue";
            canKillThread1and2 = true;
            run();
        } else if (releaseComparingSem == true) {
            appendToCourseProcessedTextView("Getting next person in the line");
            userStatus = "Continue";
        } else if (callRun) {
            appendToCourseProcessedTextView("Starting all over again");
            //sem.release();
            canKillThread1and2 = true;
            run();
        }
    }

    private void connectUser1AndUser2() {
        appendToCourseProcessedTextView("Connecting " + user1Email + " and " + user2Email);
        Log.d(TAG, "connectUser1AndUser2 from course Thread");
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                courseUserProcessedTextView.setText("Connecting users");
//            }
//        });

        setUpUser1();
    }

    private void setUpUser1() {
        appendToCourseProcessedTextView("Setting up " + user1Email);
        final String message = "You two have been linked because you both are in class " + nameOfTheCourseThatMatched + "!";

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
        appendToCourseProcessedTextView("Setting up " + user2Email);
        final String message = "You two have been linked because you both are in class " + nameOfTheCourseThatMatched + "!";
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
                removeUsers2FromCourseAndUnlockBothUsers();
            }
        });
    }

    private void removeUsers2FromCourseAndUnlockBothUsers() {
        appendToCourseProcessedTextView("Resetting everything for " + user1Email + " and " + user2Email);
        Log.d(TAG, "removeUsers2FromCourseAndUnlockBothUsers");
        //courseUserProcessedTextView.setText("Connecting complete, removing users from the Course list");
        //removing the user1 and 2 from Course list
        databaseManager.deleteField("Connecting/Course", user1UID);
        databaseManager.deleteField("Connecting/Course", user2UID);

        databaseManager.deleteField("Connecting/Course Queue", user1UID);
        databaseManager.deleteField("Connecting/Course Queue", user2UID);
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

        //resetting Can Cancel Searching
        String searchCancelingDocPath2 = user2Email + "/Search Canceling";
        String canCancelSearchingFieldName2 = "Can Cancel Searching";
        databaseManager.updateTheField(searchCancelingDocPath2, canCancelSearchingFieldName2, "true");

        //unlocking user2
//        databaseManager.updateTheField(user2CanCancelSearchingDocPath, "Can Cancel Searching", "true");
//        databaseManager.updateTheField(user2MoreInfoDocumentPath, "Done Searching", "true");

        //courseUserProcessedTextView.setText("Starting all over");
        appendToCourseProcessedTextView("Everything's Complete");
        appendToCourseProcessedTextView("Starting all over again");
        try {
            Log.d(TAG, "Everything's Complete from Course Thread");
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
