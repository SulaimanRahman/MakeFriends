package edu.csun.compsci490.makefriendsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

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

        // buttons
        TextView interests, location, all;
        interests = rootView.findViewById(R.id.tv_interests);
        location = rootView.findViewById(R.id.tv_location);
        all = rootView.findViewById(R.id.tv_all);

        Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner_Course);
        List<String> courseOptions = new ArrayList<>();
        courseOptions.add(0, "Course");
        courseOptions.add("COMP1");
        courseOptions.add("COMP2");
        courseOptions.add("COMP3");
        courseOptions.add("COMP4");
        courseOptions.add("COMP5");

        ArrayAdapter<String> dataAdapter;
        dataAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, courseOptions);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).equals("Course")){
                    // do nothing
                } else {
                    // on selected spinner item
                    String item = adapterView.getItemAtPosition(i).toString();
                    // search based on which item/course was selected here

                    // show selected spinner item
                    // Toast.makeText(adapterView.getContext(), "Selected: " + item, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub
            }
        });

        interests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // search for users which have interests similar to user

                // show waiting screen

                // if a user was found, start messaging activity with that user
                // else show users not found screen
            }
        });
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // search for users which have location similar to user

                // show waiting screen

                // if a user was found, start messaging activity with that user
                // else show users not found screen
            }
        });
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // search for users which have all similar to user

                // show waiting screen

                // if a user was found, start messaging activity with that user
                // else show users not found screen
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onClick(View view) {
    }
}