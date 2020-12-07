package edu.csun.compsci490.makefriendsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchUsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchUsersFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    TextView interests, location, all;
    ImageView allSelectedIcon, courseSelectedIcon, locationSelectedIcon, interestsSelectedIcon;
    ConstraintLayout.LayoutParams params;
    Spinner spinner;

    private FirebaseAuth firebaseAuth;
    private DatabaseManager databaseManager;
    private UserSingleton userSingleton;
    private String userEmail;
    private String TAG = "SearchUsersFragment";

    private ColorDrawable blackColor;
    private ColorDrawable greyColor;

    private String canCancelSearching;
    private ProgressBar progressBar;

    private Button resetButton;

    private String latitude;
    private String longitude;

    private LocationManager locationManager;
    private Location mLocation;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private String providerInfo;

    public SearchUsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchUsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchUsersFragment newInstance(String param1, String param2) {
        SearchUsersFragment fragment = new SearchUsersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_search_users, container, false);


        firebaseAuth = FirebaseAuth.getInstance();
        databaseManager = new DatabaseManager();
        userSingleton = UserSingleton.getInstance();
        userEmail = userSingleton.getEmail();

        greyColor = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.grey));
        blackColor = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.black));

        progressBar = rootView.findViewById(R.id.progress_bar_search_users_fragment);
        progressBar.setVisibility(View.VISIBLE);
        // selected search option icon
        allSelectedIcon = rootView.findViewById(R.id.allSelectedIcon);
        courseSelectedIcon = rootView.findViewById(R.id.courseSelectedIcon);
        locationSelectedIcon = rootView.findViewById(R.id.locationSelectedIcon);
        interestsSelectedIcon = rootView.findViewById(R.id.interestSelectedIcon);

        resetButton = rootView.findViewById(R.id.reset_everything);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interestsSelectedIcon.setVisibility(View.INVISIBLE);
                databaseManager.updateTheField(userEmail + "/Contacts", "All Users", "none");
                databaseManager.updateTheField(userEmail + "/Contacts", "Blocked Users", "none");

                databaseManager.updateTheField(userEmail + "/More Info", "Canceling", "false");
                databaseManager.updateTheField(userEmail + "/More Info", "Searching For", "none");
                databaseManager.updateTheField(userEmail + "/More Info", "User Is In Queue", "none");
                databaseManager.updateTheField(userEmail + "/More Info", "Searching For What Course", "none");

                databaseManager.updateTheField(userEmail + "/Profile Page Settings", "Can Edit Interests", "true");
                databaseManager.updateTheField(userEmail + "/Profile Page Settings", "Can Edit Courses", "true");
                databaseManager.updateTheField(userEmail + "/Search Canceling", "Can Cancel Searching", "false");
                databaseManager.updateTheField(userEmail + "/Search Canceling", "Can Cancel Searching", "true");

            }
        });

        // buttons
        interests = rootView.findViewById(R.id.tv_interests);
        location = rootView.findViewById(R.id.tv_location);
        all = rootView.findViewById(R.id.tv_all);

        spinner = (Spinner) rootView.findViewById(R.id.spinner_Course);


        //setting up the courses in the list
        final List<String> courseOptions = new ArrayList<>();
        courseOptions.add(0, "Course");

        final String coursesCollectionPath = userEmail + "/More Info/Courses";
        databaseManager.getAllDocumentsNameInArrayListFromCollection(coursesCollectionPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                final ArrayList<String> documentNames = (ArrayList) value;

                for (int i = 0; i < documentNames.size(); i++) {
                    String documentPath = coursesCollectionPath + "/" + documentNames.get(i);
                    final int finalI = i;
                    databaseManager.getFieldValue(documentPath, "Section", new FirebaseCallback() {
                        @Override
                        public void onCallback(Object value) {
                            String sectionNumber = value.toString();
                            courseOptions.add(sectionNumber);

                            if (finalI == documentNames.size() - 1) {
                                ArrayAdapter<String> dataAdapter;
                                dataAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, courseOptions);

                                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                spinner.setAdapter(dataAdapter);
                            }
                        }
                    });
                }
                if (documentNames.size() == 0) {
                    ArrayAdapter<String> dataAdapter;
                    dataAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, courseOptions);

                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinner.setAdapter(dataAdapter);
                }
            }
        });
//        List<String> courseOptions = new ArrayList<>();
//        courseOptions.add(0, "Course");
//        courseOptions.add("COMP1");
//        courseOptions.add("COMP2");
//        courseOptions.add("COMP3");
//        courseOptions.add("COMP4");
//        courseOptions.add("COMP5");


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getItemAtPosition(i).equals("Course")) {
                    // selected item is the course hint, do nothing

                } else if (adapterView.getItemAtPosition(i).equals("Cancel")) {
                    lockAllSearchingPossibilities();
                    setTheUserStatusToCanceling();
                    resetCourseSpinner();
                } else {
                    // on selected spinner item
                    if (courseSelectedIcon.getVisibility() == View.VISIBLE) {
                        // the user selected the same course he is searching for, so do nothing
                    } else {
                        String item = adapterView.getItemAtPosition(i).toString();
                        // search based on which item/course was selected here
                        progressBar.setVisibility(View.VISIBLE);
                        spinner.setEnabled(false);
                        lockOtherSearchingPossibilitiesForCourse(true, item);

                        // show selected spinner item
                        // Toast.makeText(adapterView.getContext(), "Selected: " + item, Toast.LENGTH_SHORT).show();

                        // there is still a bug here when toggling the icon for the courses. needs fix
                        //toggleCourseIcon();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub
            }
        });

        interests.setOnClickListener(this);
        location.setOnClickListener(this);
        all.setOnClickListener(this);

        lockAllSearchingPossibilities();
        getUserMoreInfoDocumentSnapshot();

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_interests:
                interests.setEnabled(false);
                interests.setTextColor(greyColor.getColor());
                progressBar.setVisibility(View.VISIBLE);
                checkIfUserIsSearchingOrCancelingForInterest();
                //toggleInterestIcon();
                //checkIfUserHasAddedAnyInterests();
                //addUserToTheInterestQueue();
                break;
            case R.id.tv_location:
                progressBar.setVisibility(View.VISIBLE);
                checkIfUserIsSearchingOrCancelingForLocation();

                //toggleLocationIcon();
                break;
            case R.id.tv_all:
                //check interest, courses, and location. they all have to be available
                progressBar.setVisibility(View.VISIBLE);
                checkIfUserIsSearchingOrCancelingForAll();
                //toggleAllIcon();
                break;
        }
    }

    private void lockAllSearchingPossibilities() {
        interests.setEnabled(false);
        location.setEnabled(false);
        spinner.setEnabled(false);
        all.setEnabled(false);

        interests.setTextColor(greyColor.getColor());
        location.setTextColor(greyColor.getColor());
        all.setTextColor(greyColor.getColor());
    }

    public void getUserMoreInfoDocumentSnapshot() {

        String userSearchCancelingDocPath = userEmail + "/Search Canceling";

        databaseManager.getDocumentSnapshot(userSearchCancelingDocPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                DocumentSnapshot snapshot = (DocumentSnapshot) value;
                snapshot.getReference().addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        String fieldName = "Can Cancel Searching";
                        String status = value.get(fieldName).toString();
                        if (status.equals("true")) {
                            //checking if User has started searching
                            canCancelSearching = "true";
                            updateTheLayout();

                        } else {
                            canCancelSearching = "false";
                            lockAllSearchingPossibilities();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

//    public void setActionListenerToCanCancelSearching(DocumentSnapshot snapshot) {
//
//    }

    private void updateTheLayout() {
        String userMoreInfoDocPath = userEmail + "/More Info";
        String fieldName = "Searching For";

        databaseManager.getFieldValue(userMoreInfoDocPath, fieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                String searchingFor = value.toString();
                if (searchingFor.equals("none")) {
                    unlockAllSearchingPossibilities();
                } else if (searchingFor.equals("Interest")) {
                    lockOtherSearchingPossibilitiesForInterest(false);
                } else if (searchingFor.equals("Course")) {
                    lockOtherSearchingPossibilitiesForCourse(false, null);
                } else if (searchingFor.equals("Location")) {
                    lockOtherSearchingPossibilitiesForLocation(false);
                } else if (searchingFor.equals("All")) {
                    lockOtherSearchingPossibilitiesForAll(false);
                }

            }
        });
    }

    private void checkIfUserIsSearchingOrCancelingForInterest() {
        if (interestsSelectedIcon.getVisibility() == View.INVISIBLE) {//User is searching
            lockOtherSearchingPossibilitiesForInterest(true);//user can only do one search at a time
            //toggleInterestIcon();
        } else {//user is canceling
            setTheUserStatusToCanceling();
        }
    }

    private void lockOtherSearchingPossibilitiesForInterest(boolean checkForInterest) {
        location.setEnabled(false);
        spinner.setEnabled(false);
        all.setEnabled(false);

        location.setTextColor(greyColor.getColor());
        all.setTextColor(greyColor.getColor());

        if (checkForInterest) {
            checkIfUserHasAddedAnyInterests();
        } else {
            interestsSelectedIcon.setVisibility(View.VISIBLE);
            interests.setEnabled(true);
            interests.setTextColor(blackColor.getColor());
        }

    }

    private void checkIfUserHasAddedAnyInterests() {
        String interestsDocPath = userEmail + "/More Info";
        databaseManager.getFieldValue(interestsDocPath, "Interest Array", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                String dbValue = value.toString();
                if (dbValue.equals("none")) {//user don't have any interests
                    unlockAllSearchingPossibilities();
                    Log.d(TAG, "User don't have any interests");
                    Toast.makeText(getContext(), "Please add interests in the home page", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {//user has interests saved
                    ArrayList<String> interest = (ArrayList) value;
                    Log.d(TAG, "User does have interests");
                    toggleInterestIcon();
                    lockEditingInterests();//because we don't want the user to delete all the interests cause that will create errors
                }
            }
        });
    }

    private void unlockAllSearchingPossibilities() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location.setEnabled(true);
            all.setEnabled(true);

            location.setTextColor(blackColor.getColor());
            all.setTextColor(blackColor.getColor());

            locationSelectedIcon.setVisibility(View.INVISIBLE);
            allSelectedIcon.setVisibility(View.INVISIBLE);

            /*if the user preses home and goes to the settings and turns off the location
            take care of this situation, like add listener to that or if the user leaves the
            app and then comes back to this exact page, then recheck if the location is on or off
             */
        } else {
            location.setEnabled(false);
            all.setEnabled(false);

            location.setTextColor(greyColor.getColor());
            all.setTextColor(greyColor.getColor());

            locationSelectedIcon.setVisibility(View.INVISIBLE);
            allSelectedIcon.setVisibility(View.INVISIBLE);

            //show a popup that access to location is off and go to settings to give access
            //until then you will not be able to search by all and location

            //writing the following toast twice for making it to last longer than LENGTH_LONG
            Toast.makeText(getContext(), "Please allow location from the settings to be able to search by 'Location' and 'All'", Toast.LENGTH_LONG).show();
            Toast.makeText(getContext(), "Please allow location from the settings to be able to search by 'Location' and 'All'", Toast.LENGTH_LONG).show();
        }

        interests.setEnabled(true);

        spinner.setEnabled(true);
        resetCourseSpinner();

        interests.setTextColor(blackColor.getColor());

        interestsSelectedIcon.setVisibility(View.INVISIBLE);
        courseSelectedIcon.setVisibility(View.INVISIBLE);

    }

    public void toggleInterestIcon() {
        allSelectedIcon.setVisibility(View.INVISIBLE);
        if (interestsSelectedIcon.getVisibility() == View.VISIBLE) {
            interestsSelectedIcon.setVisibility(View.INVISIBLE);
        } else {
            interestsSelectedIcon.setVisibility(View.VISIBLE);
        }
    }

    private void lockEditingInterests() {
        String profileSettingDocPath = userEmail + "/Profile Page Settings";
        String fieldName = "Can Edit Interests";
        databaseManager.updateTheField(profileSettingDocPath, fieldName, "false");
        setTheUserSearchingForInterest();
    }

    private void setTheUserSearchingForInterest() {
        String searchingForDocPath = userEmail + "/More Info";
        String fieldName = "Searching For";

        databaseManager.updateTheField(searchingForDocPath, fieldName, "Interest");
        setTheUserWhereAboutToInterestQueue();
    }

    private void setTheUserWhereAboutToInterestQueue() {
        String whereAboutDocPath = userEmail + "/More Info";
        String fieldName = "User Is In Queue";
        databaseManager.updateTheField(whereAboutDocPath, fieldName, "Interest Queue");
        addUserToTheInterestQueue();
    }

    private void addUserToTheInterestQueue() {
        String interestQueueDocPath = "Connecting/Interest Queue";
        String userUID = firebaseAuth.getUid();

        databaseManager.createNewField(interestQueueDocPath, userUID, "Interest");
        interests.setEnabled(true);
        interests.setTextColor(blackColor.getColor());
        progressBar.setVisibility(View.GONE);
    }

    private void setTheUserStatusToCanceling() {
        String cancelingStatusDocPath = userEmail + "/More Info";
        String fieldName = "Canceling";
        databaseManager.updateTheField(cancelingStatusDocPath, fieldName, "true");
        getWhatQueueIsTheUserIn();
    }

    private void getWhatQueueIsTheUserIn() {

        try {
            sleep(1000);
            Log.d(TAG, "Sleeping for 1000 millis");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (canCancelSearching.equals("false")) {

        }

        String userQueueDocPath = userEmail + "/More Info";
        String fieldName = "User Is In Queue";

        databaseManager.getFieldValue(userQueueDocPath, fieldName, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                String queueName = value.toString();
                removeUserFromTheSearching(queueName);
            }
        });
    }

    private void removeUserFromTheSearching(String queueName) {
        String queueDocPath = "Connecting/" + queueName;
        String fieldName = firebaseAuth.getUid();
        databaseManager.deleteField(queueDocPath, fieldName);
        resetEverythingInTheDB();
    }

    private void resetEverythingInTheDB() {
        //enabling interest Editing
        String profileSettingsDocPath = userEmail + "/Profile Page Settings";
        String enablingInterestFieldName = "Can Edit Interests";
        String enablingCourseFieldName = "Can Edit Courses";
        databaseManager.updateTheField(profileSettingsDocPath, enablingInterestFieldName, "true");
        databaseManager.updateTheField(profileSettingsDocPath, enablingCourseFieldName, "true");

        //resetting things in user More Info document
        String userMoreInfoDocPath = userEmail + "/More Info";

        //Resetting Canceling status
        String cancelingStatusFieldName = "Canceling";
        databaseManager.updateTheField(userMoreInfoDocPath, cancelingStatusFieldName, "false");

        //resetting name of the courseSearching
        String courseSearching = "Searching For What Course";
        databaseManager.updateTheField(userMoreInfoDocPath, courseSearching, "none");

        //resetting user Queue
        String userQueueLocFieldName = "User Is In Queue";
        databaseManager.updateTheField(userMoreInfoDocPath, userQueueLocFieldName, "none");

        //resetting User Searching For
        String searchingForFieldName = "Searching For";
        databaseManager.updateTheField(userMoreInfoDocPath, searchingForFieldName, "none");

        //resetting User Latitude and Longitude
        databaseManager.updateTheField(userMoreInfoDocPath, "Latitude", "none");
        databaseManager.updateTheField(userMoreInfoDocPath, "Longitude", "none");

        //resetting Can Cancel Searching
        String searchCancelingDocPath = userEmail + "/Search Canceling";
        String canCancelSearchingFieldName = "Can Cancel Searching";
        databaseManager.updateTheField(searchCancelingDocPath, canCancelSearchingFieldName, "true");

        //resetting interest toggle
        updateTheLayout();
        //toggleInterestIcon();
        progressBar.setVisibility(View.GONE);
        //unlockAllSearchingPossibilities();
    }

    public void toggleCourseIcon() {
        allSelectedIcon.setVisibility(View.INVISIBLE);
        if (courseSelectedIcon.getVisibility() == View.VISIBLE) {
            courseSelectedIcon.setVisibility(View.INVISIBLE);
        } else {
            courseSelectedIcon.setVisibility(View.VISIBLE);
        }
    }

    private void lockOtherSearchingPossibilitiesForCourse(boolean searchingForACourse, String course) {
        interests.setEnabled(false);
        location.setEnabled(false);
        all.setEnabled(false);

        interests.setTextColor(greyColor.getColor());
        location.setTextColor(greyColor.getColor());
        all.setTextColor(greyColor.getColor());

        //set the Course drop down to the course that is being searched for
        if (searchingForACourse) {
            final String documentPath = userEmail + "/More Info";

            databaseManager.updateTheField(documentPath, "Searching For What Course", course);
            List<String> courseOptions = new ArrayList<>();
            courseOptions.add(0, course);
            courseOptions.add("Cancel");

            ArrayAdapter<String> dataAdapter;
            dataAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, courseOptions);

            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(dataAdapter);

            lockEditingCourses(course);

        } else {
            //checking what course is the user searching for is in progress
            String moreInfoDocPath = userEmail + "/More Info";
            String fieldName = "Searching For What Course";
            databaseManager.getFieldValue(moreInfoDocPath, fieldName, new FirebaseCallback() {
                @Override
                public void onCallback(Object value) {
                    String course = value.toString();
                    List<String> courseOptions = new ArrayList<>();
                    courseOptions.add(0, course);
                    courseOptions.add("Cancel");

                    ArrayAdapter<String> dataAdapter;
                    dataAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, courseOptions);

                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinner.setAdapter(dataAdapter);
                    spinner.setEnabled(true);
                }
            });
        }

    }

    private void resetCourseSpinner() {
        final List<String> courseOptions = new ArrayList<>();
        courseOptions.add(0, "Course");

        final String coursesCollectionPath = userEmail + "/More Info/Courses";
        databaseManager.getAllDocumentsNameInArrayListFromCollection(coursesCollectionPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                final ArrayList<String> documentNames = (ArrayList) value;

                for (int i = 0; i < documentNames.size(); i++) {
                    String documentPath = coursesCollectionPath + "/" + documentNames.get(i);
                    final int finalI = i;
                    databaseManager.getFieldValue(documentPath, "Section", new FirebaseCallback() {
                        @Override
                        public void onCallback(Object value) {
                            String sectionNumber = value.toString();
                            courseOptions.add(sectionNumber);

                            if (finalI == documentNames.size() - 1) {
                                ArrayAdapter<String> dataAdapter;
                                dataAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, courseOptions);

                                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                spinner.setAdapter(dataAdapter);
                            }
                        }
                    });
                }
            }
        });
    }

    private void lockEditingCourses(String course) {
        String profileSettingDocPath = userEmail + "/Profile Page Settings";
        databaseManager.updateTheField(profileSettingDocPath, "Can Edit Courses", "false");
        setTheUserSearchingForCourse(course);
    }

    private void setTheUserSearchingForCourse(String course) {
        String moreInfoDocPath = userEmail + "/More Info";
        databaseManager.updateTheField(moreInfoDocPath, "Searching For", "Course");
        databaseManager.updateTheField(moreInfoDocPath, "Searching For What Course", course);
        databaseManager.updateTheField(moreInfoDocPath, "User Is In Queue", "Course Queue");
        addUserToTheCourseQueue(course);
    }

    private void addUserToTheCourseQueue(String course) {
        String courseQueueDocPath = "Connecting/Course Queue";
        String userUID = firebaseAuth.getUid();
        databaseManager.createNewField(courseQueueDocPath, userUID, course);
        courseSelectedIcon.setVisibility(View.VISIBLE);
        spinner.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private void checkIfUserIsSearchingOrCancelingForLocation() {
        if (locationSelectedIcon.getVisibility() == View.VISIBLE) {
            //user is canceling
            String moreInfoDocPath = userEmail + "/More Info";
            databaseManager.updateTheField(moreInfoDocPath, "Canceling", "true");
            getWhatQueueIsTheUserInForCourse();
        } else {
            //user is searching
            locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lockOtherSearchingPossibilitiesForLocation(true);
            } else {
                Toast.makeText(getContext(), "Please enable your location", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void getWhatQueueIsTheUserInForCourse() {
        try {
            sleep(1000);
            Log.d(TAG, "Sleeping for 1000 millis");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (canCancelSearching.equals("false")) {

        }

        String moreInfoDocPath = userEmail + "/More Info";
        databaseManager.getFieldValue(moreInfoDocPath, "User Is In Queue", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                String queueName = value.toString();
                removeUserFromTheSearching(queueName);
            }
        });
    }


    public void toggleLocationIcon() {
        allSelectedIcon.setVisibility(View.INVISIBLE);
        if (locationSelectedIcon.getVisibility() == View.VISIBLE) {
            locationSelectedIcon.setVisibility(View.INVISIBLE);
        } else {
            locationSelectedIcon.setVisibility(View.VISIBLE);
        }
    }

    private void lockOtherSearchingPossibilitiesForLocation(boolean searchingByLocation) {
        interests.setEnabled(false);
        spinner.setEnabled(false);
        all.setEnabled(false);

        interests.setTextColor(greyColor.getColor());
        all.setTextColor(greyColor.getColor());
        if (searchingByLocation) {//user is searching by location
            location.setEnabled(false);
            location.setTextColor(greyColor.getColor());
            getLatitudeAndLongitude();
        } else {//user is just setting up for Location
            locationSelectedIcon.setVisibility(View.VISIBLE);
            location.setEnabled(true);
            location.setTextColor(blackColor.getColor());
        }

    }

    private void getLatitudeAndLongitude() {
        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled) {
            providerInfo = locationManager.GPS_PROVIDER;
        } else if (isNetworkEnabled) {
            providerInfo = locationManager.NETWORK_PROVIDER;
        }

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Failed to get Location", Toast.LENGTH_SHORT).show();
                    return;
                }
                mLocation = locationManager.getLastKnownLocation(providerInfo);

            }
            public void onProviderEnabled(@NonNull String provider) {

            }

            public void onProviderDisabled(@NonNull String provider) {

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(getContext(), "Failed to get Location", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(providerInfo, 1000, 0, locationListener);
        mLocation = locationManager.getLastKnownLocation(providerInfo);

//        while (mLocation == null) {
//
//        }

        NumberFormat formatter = new DecimalFormat("#0.00");
        latitude = formatter.format(mLocation.getLatitude());
        longitude = formatter.format(mLocation.getLongitude());

        saveLatitudeAndLongitudeInTheDatabase();
    }

    private void saveLatitudeAndLongitudeInTheDatabase() {
        String moreInfoDocPath = userEmail + "/More Info";
        databaseManager.updateTheField(moreInfoDocPath, "Latitude", latitude);
        databaseManager.updateTheField(moreInfoDocPath, "Longitude", longitude);

        databaseManager.updateTheField(moreInfoDocPath, "User Is In Queue", "Location Queue");
        databaseManager.updateTheField(moreInfoDocPath, "Searching For", "Location");
        addUserToTheLocationQueue();
    }

    private void addUserToTheLocationQueue() {
        String locationQueueDocPath = "Connecting/Location Queue";
        String userUID = firebaseAuth.getUid();
        databaseManager.createNewField(locationQueueDocPath, userUID, "Location");

        updateTheLayout();
        progressBar.setVisibility(View.GONE);
    }

    private void checkIfUserIsSearchingOrCancelingForAll() {
        if (allSelectedIcon.getVisibility() == View.VISIBLE) {
            //user is canceling
            String moreInfoDocPath = userEmail + "/More Info";
            databaseManager.updateTheField(moreInfoDocPath, "Canceling", "true");
            getWhatQueueIsTheUserInForCourse();

        } else {
            //user is searching
            locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
            if (spinner.getCount() < 2) {//checking if user has at least one course
                //user don't have any courses
                Toast.makeText(getContext(), "Please add at least one course on homepage: ", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {//checking if use has the location turn on
                //user don't have location turn on
                Toast.makeText(getContext(), "Please turn on you Location", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } else {//checking if use has at least one interest
                String userMoreInfoDocPath = userEmail + "/More Info";
                databaseManager.getFieldValue(userMoreInfoDocPath, "Interest Array", new FirebaseCallback() {
                    @Override
                    public void onCallback(Object value) {
                        String dbValue = value.toString();
                        if (dbValue.equals("none")) {
                            //user don't have any interests
                            Toast.makeText(getContext(), "Please add at least one Interest on homepage", Toast.LENGTH_LONG);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            lockOtherSearchingPossibilitiesForAll(true);
                        }
                    }
                });
            }
        }
    }

    public void toggleAllIcon() {
        courseSelectedIcon.setVisibility(View.INVISIBLE);
        locationSelectedIcon.setVisibility(View.INVISIBLE);
        interestsSelectedIcon.setVisibility(View.INVISIBLE);
        if(allSelectedIcon.getVisibility() == View.VISIBLE){
            allSelectedIcon.setVisibility(View.INVISIBLE);
        } else {
            allSelectedIcon.setVisibility(View.VISIBLE);
        }
    }

    private void lockOtherSearchingPossibilitiesForAll(boolean searchingForAll) {
        interests.setEnabled(false);
        spinner.setEnabled(false);
        location.setEnabled(false);

        interests.setTextColor(greyColor.getColor());
        location.setTextColor(greyColor.getColor());

        if (searchingForAll) {//user is searching by All
            all.setEnabled(false);
            all.setTextColor(greyColor.getColor());
            setUpDatabaseForAll();
        } else {//user is just setting up for All
            allSelectedIcon.setVisibility(View.VISIBLE);
            all.setEnabled(true);
            all.setTextColor(blackColor.getColor());
        }
    }

    private void setUpDatabaseForAll() {
        String moreInfoDocPath = userEmail + "/More Info";

        databaseManager.updateTheField(moreInfoDocPath, "User Is In Queue", "Interest Queue");
        databaseManager.updateTheField(moreInfoDocPath, "Searching For", "All");
        addUserToTheInterestQueueForAll();
    }

    private void addUserToTheInterestQueueForAll() {
        String interestQueueDocPath = "Connecting/Interest Queue";
        String userUID = firebaseAuth.getUid();

        databaseManager.createNewField(interestQueueDocPath, userUID, "All");
        all.setEnabled(true);
        allSelectedIcon.setVisibility(View.VISIBLE);
        all.setTextColor(blackColor.getColor());
        progressBar.setVisibility(View.GONE);
    }
}