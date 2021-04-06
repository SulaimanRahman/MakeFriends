package edu.csun.compsci490.makefriendsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class NotificationService extends Service {

    private static final String CHANNEL_ID = "exampleServiceChannel";

    private DatabaseManager databaseManager = new DatabaseManager();
    private UserSingleton userSingleton = new UserSingleton();
    private String TAG = "NotificationService";
    private boolean firstTime = true;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("startingMessage");

        Intent notificationIntent = new Intent(this, MainNavigation.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Make Friends")
                .setContentText(input)
                .setSmallIcon(R.drawable.launcher_icon2)
                .setColor(getResources().getColor(R.color.red2))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        startTheService();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startTheService() {
        final String userEmail = userSingleton.getEmail();

        String documentPath = userEmail + "/Contacts";
        databaseManager.getFieldValue(documentPath, "All Users", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                Log.d(TAG, "user Email: " + userEmail);
                if (value.equals("none")) {
                    //write the code here so that when a new user is added, this will run again
                } else {
                    ArrayList allUsers = (ArrayList) value;
                    for (int i = 0; i < allUsers.size(); i++) {
                        String contactPath = userEmail + "/Contacts/" + allUsers.get(i) + "/Chat";
                        final String contactEmail = allUsers.get(i).toString();
                        final int uniqueID = i;
                        if (firstTime) {
                            firstTime = false;
                            break;
                        }
                        databaseManager.getDocumentSnapshot(contactPath, new FirebaseCallback() {
                            @Override
                            public void onCallback(Object value) {
                                DocumentSnapshot documentSnapshot = (DocumentSnapshot) value;
                                addActionListener(documentSnapshot, contactEmail, uniqueID);
                            }
                        });
                    }
                }
            }
        });
    }

    private void addActionListener(DocumentSnapshot finalDocumentSnapshot, final String contactEmail, final int uniqueID) {
        final DocumentSnapshot documentSnapshot = finalDocumentSnapshot;
        documentSnapshot.getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.d(TAG, "Something went wrong: listen failed. ", error);
                }
                if (firstTime) {
                    addNotificationRemoval(documentSnapshot, firstTime, uniqueID);
                    firstTime = false;
                    return;
                }

                Log.d(TAG, "NotificationService - getting data from the path: " + documentSnapshot.getReference().getPath());
                //refreshing the snapshot
                databaseManager.getDocumentSnapshot(documentSnapshot.getReference().getPath(), new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        if (value == null) {
                            return;
                        }
                        DocumentSnapshot documentSnapshotRefreshed = (DocumentSnapshot) value;

                        HashMap<String, Object> data = (HashMap) documentSnapshotRefreshed.getData();
                        ArrayList<String> keys = new ArrayList<>();
                        keys.addAll(data.keySet());

                        int lastKeyIndex = keys.size() - 2;

                        String lastKey = "";

                        for (int i = 0; i < keys.size(); i++) {
                            //Log.d(TAG, "Currentfield Key: " + keys.get(i) + ", LastKeyIndex: " + lastKeyIndex);
                            if (keys.get(i).contains(String.valueOf(lastKeyIndex))) {
                                lastKey = keys.get(i);
                                break;
                            } else if (keys.get(i).equals("Note0") && keys.get(i).equals("")) {
                                lastKey = keys.get(i);
                            }
                        }
<<<<<<< Updated upstream
                        Log.d(TAG, "From NotificationService.java Last Message Key is: " + lastKey);
                        if (lastKey.contains("Me") || lastKey.contains("Sent") || lastKey.contains("Note0")) {
=======

                        Log.d(TAG, "From Notification Service - lastKey is: " + lastKey);
                        Log.d(TAG, "From Notification Service - lastKeyIndex is: " + lastKeyIndex);

                        if (lastKey.contains("Me")) {
>>>>>>> Stashed changes
                            //don't do anything
                        } else if (lastKey.contains("Received")) {
                            String newMassage = "File Received";
                            getContactData(contactEmail, newMassage, uniqueID);
                        } else {
                            String newMassage = data.get(lastKey).toString();
                            getContactData(contactEmail, newMassage, uniqueID);
                        }
                    }
                });
            }
        });
    }

    private void addNotificationRemoval(DocumentSnapshot documentSnapshot, final boolean firstTimeHere, final int uniqueID) {
        int lastIndexOfSlash = documentSnapshot.getReference().getPath().lastIndexOf("/");
        final String documentPath = documentSnapshot.getReference().getPath().substring(0, lastIndexOfSlash) + "/More Info";
        databaseManager.getDocumentSnapshot(documentPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DocumentSnapshot moreInfoDocumentSnapshot = (DocumentSnapshot) value;
                addListenerToMessageRead(moreInfoDocumentSnapshot, firstTimeHere, documentPath, uniqueID);
            }
        });
    }

    private void addListenerToMessageRead(DocumentSnapshot documentSnapshot, boolean firstTimeHere, final String documentPath, final int uniqueID) {
        documentSnapshot.getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                //refreshing the snapshot
                databaseManager.getFieldValue(documentPath, "All Messages Been Read", new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        if (value == null) {
                            return;
                        }
                        String status = value.toString();
                        if (status.equals("true")) {
                            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(uniqueID);
                        }
                    }
                });
            }
        });
    }

    private void getContactData(String contactEmail, final String newMessage, final int uniqueID) {
        String contactDocumentPath = contactEmail + "/Profile";
        databaseManager.getAllDocumentDataInHashMap(contactDocumentPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                HashMap<String, Object> data = (HashMap) value;

                final String contactFullName = data.get("First Name").toString() + " " + data.get("Last Name").toString();
                final String contactEmail = data.get("Email").toString();
                final String profileUriPath = data.get("Profile Picture Uri").toString();
                databaseManager.getTheFileUriFromFirebaseStorage(profileUriPath, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        String uri = value.toString();
                        Log.d("Notification", "uri: " + uri);
                        createNotification(contactEmail, contactFullName, uri, newMessage, uniqueID);
                    }
                });

            }
        });
    }

    private void createNotification(String contactEmail, String contactFullName, String contactProfileUri, String newMessage, int uniqueID) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(contactEmail, contactFullName + ": ", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        //NotificationData notificationData = new NotificationData(contactEmail, contactFullName, contactProfileUri);

        Intent notificationIntent = new Intent(this, MessagingActivity.class);
        notificationIntent.putExtra("contactEmail", contactEmail);
        notificationIntent.putExtra("contactFullName", contactFullName);
        Log.d(TAG, "contatprUri: " + contactProfileUri);
        notificationIntent.putExtra("contactProfileUri", contactProfileUri);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, contactEmail);
        builder.setContentTitle(contactFullName + ": ");
        builder.setContentText(newMessage);
        builder.setContentIntent(pendingIntent);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setSmallIcon(R.drawable.launcher_icon2);
            builder.setColor(getResources().getColor(R.color.red2));
        } else {
            builder.setSmallIcon(R.drawable.launcher_icon2);
        }

        builder.setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(uniqueID, builder.build());
    }
}
