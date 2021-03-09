package edu.csun.compsci490.makefriendsapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.OnBalloonClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private DatabaseManager databaseManager;
    private UserSingleton userSingleton;
    private String userEmail;
    private FirebaseAuth firebaseAuth;

    private TextView firstAndLastName;
    private ImageView profilePicture;
    private EditText biographyTextField;
    private ImageButton logoutBtn;

    private ImageButton btnAddScheduleRow, btnRemoveScheduleRow;
    private TableLayout tableLayout;
    private TableRow tr;
    //private TextView sectionCell, courseCell, courseNumCell;
    private EditText sectionCell, courseCell, courseNumCell;

    private AutoCompleteTextView interestSearchBar;
    private final ArrayList<String> defaultInterests = new ArrayList<>();
    private final ArrayList<String> currentUserInterests = new ArrayList<>();
    private final ArrayList<Course> currentUserCourses = new ArrayList<>();
    private FlexboxLayout interestBubbleParent;
    private Button interestBubble;
    private Balloon balloon;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public HomeFragment() {
        // Required empty public constructor
        Log.d("HomeFagment", "worked constructor");
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        Log.d("HomeFagment", "worked");
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
        // Inflate the layout for this fragment
        Log.d("HomeFagment", "worked");

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //getActivity().startService(new Intent(getActivity(), BackgroundService.class));
      
        databaseManager = new DatabaseManager();
        userSingleton = UserSingleton.getInstance();
        userEmail = userSingleton.getEmail();
        firebaseAuth = FirebaseAuth.getInstance();

        firstAndLastName = view.findViewById(R.id.firstAndLastName);
        profilePicture = view.findViewById(R.id.profile_image);
        biographyTextField = view.findViewById(R.id.biographyTextField);
        logoutBtn = view.findViewById(R.id.btn_logOut);

        getUserFirstNameLastNameBiographyAndProfilePicture();
        getUserInterests();
        getUserSchedule();
        tableLayout = view.findViewById(R.id.tableLayout);
        tr = view.findViewById(R.id.tr1);
        btnAddScheduleRow = view.findViewById(R.id.btn_addSchedule);
        btnRemoveScheduleRow = view.findViewById(R.id.btn_removeSchedule);
        interestBubbleParent = view.findViewById(R.id.flexbox_interestBubbleParent);

        interestSearchBar = view.findViewById(R.id.interestSearchBar);
        getAllDefaultInterests();
        interestSearchBar.setAdapter(new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, defaultInterests));

        // if user clicks outside the searchbar the text inside clears
        interestSearchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (!defaultInterests.contains(interestSearchBar.getText().toString())){
                        interestSearchBar.setText(""); // clear your TextView
                    }
                }
            }
        });

        // allows user to add a custom interest
        interestSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(EditorInfo.IME_ACTION_DONE == actionId && !textView.getText().toString().isEmpty()){
                    addInterestBubble(textView.getText().toString());
                    interestSearchBar.setText("");
                }
                return false;
            }
        });

        // do stuff after item in interest search is clicked
        interestSearchBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                // hides the keyboard after an item is clicked
//                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);

                //get whatever autocomplete text was selected and add it to a bubble
                addInterestBubble(adapterView.getItemAtPosition(i).toString());
                // clear your TextView
                interestSearchBar.setText("");
            }
        });

        // implementation "com.github.skydoves:balloon:1.2.5"
        // balloon is the minus button which pops up after clicking an interest
        balloon = new Balloon.Builder(getActivity().getApplicationContext())
                .setArrowVisible(false)
                .setIconDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_baseline_remove_circle_24))
                .setIconSize(20)
                .setIconSpace(0)
                .setBalloonAnimation(BalloonAnimation.ELASTIC)
                .setLifecycleOwner(getActivity())
                .build();
        // add listener to remove the clicked interest bubble
        balloon.setOnBalloonClickListener(new OnBalloonClickListener() {
            @Override
            public void onBalloonClick(@NotNull View view) {
                removeInterestBubble(interestBubble);
            }
        });

        profilePicture.setOnClickListener(this);
        //biographyTextField.setOnClickListener(this);
        biographyTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    saveBiography();
                }
            }
        });
        logoutBtn.setOnClickListener(this);
        btnAddScheduleRow.setOnClickListener(this);
        btnRemoveScheduleRow.setOnClickListener(this);

        /* // helper code to populate the interests on the firebase db
        String[] interestData = {"Programming", "Web Development", "Computer Science", "Linear Algebra", "Android Development",
                                "iOS development", "Linux", "Gaming", "Working Out", "Hiking",
                                "Embedded Systems", "Machine Learning", "Cooking", "Tennis", "Dodgers",
                                "Internet of Things", "Java Language", "Cooking", "Baseball", "Basketball", "Soccer",
                                "Volleyball"};

        // helper code to populate db w interests
        int dataLength = interestData.length;
        for(int i = 0; i < dataLength; i++){
            databaseManager.updateTheField("Default/All Interests", "Interest" + (i+1), interestData[i]);
        }*/
        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_image:
                Intent getProfilePictureURI = new Intent(Intent.ACTION_GET_CONTENT);
                getProfilePictureURI.setType("*/*");
                startActivityForResult(getProfilePictureURI, 1);
                break;
            case R.id.btn_addSchedule:
                if(tableLayout.getChildCount() < 7){
                    addTableRow("","","");
                } else {
                    Toast.makeText(getContext(),"Schedule is limited to 6 courses",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_removeSchedule:
                if(tableLayout.getChildCount() > 1){
                    removeTableRow();
                } else {
                    Toast.makeText(getContext(),"No courses to remove",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_logOut:
                logUserOut();
                break;
        }
    }

    // removes a row from the schedule table
    private void removeTableRow() {
        tableLayout.removeViewAt(tableLayout.getChildCount()-1);
        if(currentUserCourses.size() > 0){

            // update in fb
            final CollectionReference cref = db.collection(userEmail).document("More Info").collection("Courses");
            String sectionOfCourseToDelete = currentUserCourses.get(currentUserCourses.size()-1).getSectionNumber();
            cref.whereEqualTo("Section", sectionOfCourseToDelete).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            cref.document(document.getId()).delete();
                        }
                    } else {
                        Log.d("FBcourseDeletion", "Error getting documents: ", task.getException());
                    }
                }
            });
            currentUserCourses.remove(currentUserCourses.size()-1);
        }
    }

    // adds a row to the schedule
    private void addTableRow(String section, String course, String courseNumber) {
        tr = new TableRow(getContext());
        sectionCell = new EditText(getContext());
        courseCell = new EditText(getContext());
        courseNumCell = new EditText(getContext());
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, .25f);
        tr.setLayoutParams(tableParams);

        final Course courseAddition = new Course();

        sectionCell.setTextSize(16);
        sectionCell.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        sectionCell.setGravity(Gravity.CENTER_HORIZONTAL);
        sectionCell.setPadding(10,0,10,15);
        sectionCell.setLayoutParams(rowParams);
        sectionCell.setText(section);
        rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, .50f);
        sectionCell.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!sectionCell.hasFocus()){
                    // TODO: save user section to firebase here
                    courseAddition.setSectionNumber(sectionCell.getText().toString());
                    //Toast.makeText(getContext(), "course saved", Toast.LENGTH_SHORT).show();
                    if(courseAddition.hasData()){
                        if(currentUserCourses.contains(courseAddition)){
                            Toast.makeText(getContext(), "Duplicate course", Toast.LENGTH_SHORT).show();
                        } else{
                            saveTheCourse(courseAddition);
                        }
                    }
                }
            }
        });

        courseCell.setTextSize(16);
        courseCell.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        courseCell.setGravity(Gravity.CENTER_HORIZONTAL);
        courseCell.setPadding(10,0,10,15);
        courseCell.setLayoutParams(rowParams);
        courseCell.setText(course);
        courseCell.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!courseCell.hasFocus()) {
                    // TODO: save user course to firebase here
                    courseAddition.setCourse(courseCell.getText().toString());
                    if(courseAddition.hasData()){
                        if(currentUserCourses.contains(courseAddition)){
                            Toast.makeText(getContext(), "Duplicate course", Toast.LENGTH_SHORT).show();
                        } else{
                            saveTheCourse(courseAddition);
                        }
                        //Toast.makeText(getContext(), "course saved", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, .25f);
        courseNumCell.setTextSize(16);
        courseNumCell.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        courseNumCell.setGravity(Gravity.CENTER_HORIZONTAL);
        courseNumCell.setPadding(10,0,10,15);
        courseNumCell.setLayoutParams(rowParams);
        courseNumCell.setText(courseNumber);
        courseNumCell.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!courseNumCell.hasFocus()) {
                    // TODO: save user course# to firebase here
                    courseAddition.setCourseNumber(courseNumCell.getText().toString());
                    if(courseAddition.hasData()){
                        if(currentUserCourses.contains(courseAddition)){
                            Toast.makeText(getContext(), "Duplicate course", Toast.LENGTH_SHORT).show();
                        } else{
                            saveTheCourse(courseAddition);
                        }
                        //Toast.makeText(getContext(), "course saved", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tr.addView(sectionCell);
        tr.addView(courseCell);
        tr.addView(courseNumCell);

        tableLayout.addView(tr);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                //if (resultCode == RESULT_OK) {
                String path = data.getData().getPath();
                Uri uri = data.getData();
                saveUserProfilePicture(uri);
                saveUserProfilePictureUri(uri);
                //}
                break;
        }
    }

    public void getUserFirstNameLastNameBiographyAndProfilePicture() {
        databaseManager.getAllDocumentDataInHashMap(userEmail + "/Profile", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                if (value == null) {
                    //failed to the get user data
                    return;
                }
                HashMap<String, Object> data = (HashMap) value;

                String firstName = String.valueOf(data.get("First Name"));
                String lastName = String.valueOf(data.get("Last Name"));
                String biography = String.valueOf(data.get("Biography"));
                String pictureUploaded = String.valueOf(data.get("Profile Picture Uri"));
                /*
                set all the data where it suppose to be
                check if biography is null then set "edit your biography here"
                 */

                firstAndLastName.setText(firstName + " " + lastName);

                if (!biography.equals("null")){
                    biographyTextField.setText(biography);
                }

                if (pictureUploaded != null) {
                    getUserProfilePicture(pictureUploaded);
                } else {
                    //user has not uploaded any pictures
                }

            }
        });
    }


    public void getUserProfilePicture(String picPath) {

        databaseManager.getTheFileUriFromFirebaseStorage(picPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                if (value == null) {
                    //failed to get the user profile picture
                    return;
                }
                Uri uri = (Uri) value;
                //implementation 'com.github.bumptech.glide:glide:4.8.0'
                /*
                use the following to set the picture on the image view:
                Glide.with(getApplicationContext()).load(uri.toString()).into(nameOfTheImageView);
                 */

                Glide.with(getActivity().getApplicationContext()).load(uri.toString()).into(profilePicture);

            }
        });
    }

    public void getUserSchedule() {
//        databaseManager.getAllDocumentsInArrayListFromCollection(userEmail + "/More Info/Courses/", new FirebaseCallback() {
//            @Override
//            public void onCallback(Object value) {
//                if (value == null) {
//                    //no data exists
//                    return;
//                }
//
//                ArrayList<DocumentSnapshot> documents = (ArrayList) value;
//
//                for(int i = 0; i < documents.size(); i++) {
//                    String sectionNumber = documents.get(i).get("Section Number").toString();
//                    String course = documents.get(i).get("Course").toString();
//                    String courseNumber = documents.get(i).get("Course Number").toString();
//
//                    // TODO: add user schedule in dynamic table
//                    Course loadedCourse = new Course(sectionNumber, course, courseNumber);
//                    currentUserCourses.add(loadedCourse);
//                    addTableRow(sectionNumber, course, courseNumber);
//                }
//
//            }
//        });


        db.collection(userEmail).document("More Info").collection("Courses").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String sectionNumber = (String) document.get("Section");
                        String course = (String) document.get("Course");
                        String courseNumber = (String) document.get("Course Number");

                        // TODO: add user schedule in dynamic table
                        Course loadedCourse = new Course(sectionNumber, course, courseNumber);
                        currentUserCourses.add(loadedCourse);
                        addTableRow(sectionNumber, course, courseNumber);
                    }
                } else {
                    Log.d("GETUSERSCHED", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void getUserInterests() {
        databaseManager.getFieldValue(userEmail + "/More Info", "Interest Array", new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {

                if (value == null) {
                    //failed to get user interest
                    return;
                }

                if (value.equals("none")) {
                    //not interests exists
                } else {
                    ArrayList<String> interests = (ArrayList) value;

                    if (interests.size() == 0) {
                        //the user hasn't added any interests
                        return;
                    }


                    for (int i = 0; i < interests.size(); i++) {
                        String interest = interests.get(i);
                        String lowerInterest = interest.toLowerCase();
                        currentUserInterests.add(lowerInterest);
                    }

                    generateUserInterestBubbles();
                }
            }
        });
    }

    public void saveUserProfilePicture(final Uri uri) {
        databaseManager.saveFileInFirebaseStorage(userEmail + "/ProfilePic", uri, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                saveUserProfilePictureUri(uri);
                getUserProfilePicture(uri.getPath());//basically this is to refresh and show the picture

            }
        });
    }

    public void saveUserProfilePictureUri (Uri uri) {
        String documentPath = userEmail + "/Profile";
        //databaseManager.updateTheField(documentPath, "Profile Picture Uri", uri.toString());
        databaseManager.updateTheField(documentPath, "Profile Picture Uri", userEmail + "/ProfilePic");
    }

    public void saveBiography() {
        String biography = biographyTextField.getText().toString();//get biography from the textField

        if (biography.equals("") || biography.equals("edit your biography here")) {
            databaseManager.updateTheField(userEmail + "/Profile", "Biography", null);
            //set the biography textField to "edit your biography here"

            return;
        }

        databaseManager.updateTheField(userEmail + "/Profile", "Biography", biography);
        Toast.makeText(getContext(),"Biography saved",Toast.LENGTH_SHORT).show();
    }

    public void saveTheCourse(final Course courseToSave) {
        currentUserCourses.add(courseToSave);
        //set the following variables and get the data from the textFields
        final String sectionNumber = courseToSave.getSectionNumber();
        final String course = courseToSave.getCourse();
        final String courseNumber = courseToSave.getCourseNumber();

        Map<String, String> courseInfo = new HashMap<>();
        courseInfo.put("Section", sectionNumber);
        courseInfo.put("Course", course);
        courseInfo.put("Course Number", courseNumber);


        db.collection(userEmail).document("More Info").collection("Courses").document().set(courseInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Course saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
//        databaseManager.checkIfThisDocumentExists(userEmail + "/More Info/Courses/" + courseToSave.getSectionNumber(), new FirebaseCallback() {
//            @Override
//            public void onCallback(Object value) {
//                if (value == null) {
//                    //failed to check if the document exists
//                    return;
//                }
//
//                boolean documentExists = (boolean) value;
//
//                if (documentExists) {
//                    //show a toast that the course already exists in the
//                    return;
//                }
//
//                currentUserCourses.add(courseToSave);
//
//                String docPath = userEmail + "/More Info/Courses/" + sectionNumber;
//                databaseManager.createNewField(docPath, "Section Number", courseToSave.getSectionNumber());
//                databaseManager.createNewField(docPath, "Course", courseToSave.getCourse());
//                databaseManager.createNewField(docPath, "Course Number", courseToSave.getCourseNumber());
////                databaseManager.createNewField(userEmail + "/" + sectionNumber, "Subject", subject);
////                databaseManager.createNewField(userEmail + "/" + sectionNumber, "sectionNumber", sectionNumber);
////                databaseManager.createNewField(userEmail + "/" + sectionNumber, "Semester" , semester);
////                databaseManager.createNewField(userEmail + "/" + sectionNumber, "Year", year);
//            }
        //});

    }

    public void saveInterest() {//When they are adding interest, there should no interest show up that has already been added
        final String interest = null;
        final String documentPath = userEmail + "/More Info";

//        databaseManager.getFieldValue(documentPath, "Interest Array", new FirebaseCallback() {
//            @Override
//            public void onCallback(Object value) {
//                if (value == null) {
//                    //failed to get user interests
//                    return;
//                }
//
//                ArrayList interestArray = (ArrayList) value;
//
//                interestArray.add(interest);
//
//                databaseManager.updateTheField(documentPath, "Interest Array", interestArray);
//
//            }
//        });
        databaseManager.updateTheField(documentPath, "Interest Array", currentUserInterests);
    }

    public void getAllDefaultInterests() {
        String documentPath = "Default/All Interests";

        databaseManager.getAllDocumentDataInHashMap(documentPath, new FirebaseCallback() {
            @Override
            public void onCallback(Object value) {
                if (value == null) {
                    //failed to get the data
                    return;
                }

                ArrayList<String> interests = new ArrayList();
                HashMap<String, Object> data = (HashMap) value;
                for (int i = 0; i < data.size(); i++) {
                    String key = "Interest" + (i + 1);
                    interests.add(data.get(key).toString());
                }

                /*
                maybe create a global variable array list that you can use to bring this up
                when the user is searching for the interests or maybe load this everytime the user
                comes to the homepage
                 */
                defaultInterests.addAll(interests);
            }
        });
    }

    public void logUserOut(){
        firebaseAuth.signOut();
        SharedPreferences userLocalDatabase = getActivity().getSharedPreferences("userDetails", 0);
        SharedPreferences.Editor sharePreferenceEditor = userLocalDatabase.edit();
        sharePreferenceEditor.putString("AutoLogin", "false");
        sharePreferenceEditor.commit();
        Intent serviceIntent = new Intent(getContext(), NotificationService.class);
        getActivity().stopService(serviceIntent);
        startActivity(new Intent(getContext(), MainActivity.class));
        getActivity().finish();
    }

    public void addInterestBubble(String interest){
        // if interest does not already exist in the current users interests on firebase then,
        // add interest bubble else clear input
        interest = interest.toLowerCase();
        if(currentUserInterests.contains(interest)) {
            //clear search interests bar
            Toast.makeText(getContext(),"Duplicate interest",Toast.LENGTH_SHORT).show();
            interestSearchBar.setText("");
        } else {
            // add interest to layout
            interestBubble = new Button(getActivity().getApplicationContext());
            interestBubble.setText(interest);
            interestBubble.setTextSize(16);
            interestBubble.setTextColor(getResources().getColor(R.color.white));
            interestBubble.setBackgroundResource(R.drawable.interest_bubble);
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, 80);
            params.setMargins(10,5,10,15);
            interestBubble.setLayoutParams(params);
            interestBubbleParent.addView(interestBubble);
            // add interest to users interest on firebase
            currentUserInterests.add(interest);
            saveInterest();
            interestBubble.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    balloon.showAlignRight(view, -35, -30);
                    interestBubble = (Button) view;
                }
            });
        }
    }

    public void removeInterestBubble(Button interest){
        // remove interest from users interest on firebase
        String interestToDelete = interest.getText().toString();
        int removeIndex = currentUserInterests.indexOf(interestToDelete);
        currentUserInterests.remove(removeIndex);
        // method automatically updates and deletes the field even without explicitly calling delete field
        saveInterest();

        // remove textview/bubble which contains interest from flexbox layout (interestBubbleParent)
        interestBubbleParent.removeView(interest);
        balloon.dismiss();
    }

    private void generateUserInterestBubbles() {
        if(!currentUserInterests.isEmpty()){
            for(String interest: currentUserInterests){
                // add interest to layout
                interestBubble = new Button(getActivity().getApplicationContext());
                interestBubble.setText(interest);
                interestBubble.setTextSize(16);
                interestBubble.setTextColor(getResources().getColor(R.color.white));
                interestBubble.setBackgroundResource(R.drawable.interest_bubble);
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, 80);
                params.setMargins(10,5,10,15);
                interestBubble.setLayoutParams(params);
                interestBubbleParent.addView(interestBubble);
                interestBubble.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        balloon.showAlignRight(view, -35, -30);
                        interestBubble = (Button) view;
                    }
                });
            }
        }
    }
}