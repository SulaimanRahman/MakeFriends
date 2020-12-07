package edu.csun.compsci490.makefriendsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.auth.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String TAG = SettingsFragment.class.getName();

    private SwitchMaterial locationSwitch;
    private LocationManager locationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private Location location;

    private String provider_info;

    private LocationListener locationListener;

    private EditText oldPasswordTextField;
    private EditText newPasswordTextField;

    private Button saveButton;
    private UserSingleton userSingleton;
    private DatabaseManager databaseManager;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        databaseManager = new DatabaseManager();
        oldPasswordTextField = v.findViewById(R.id.old_password_et);
        newPasswordTextField = v.findViewById(R.id.new_password_et);
        saveButton = v.findViewById(R.id.save_password_btn);
        userSingleton = UserSingleton.getInstance();

        locationSwitch = v.findViewById(R.id.location_switch);

        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled) {
            provider_info = locationManager.GPS_PROVIDER;
        } else if (isNetworkEnabled) {
            provider_info = locationManager.NETWORK_PROVIDER;
        }
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
//                double latitude = location.getLatitude();
//                double longitude = location.getLongitude();
//                String message = "New Latitude: " + latitude + "New Longitude: " + longitude;
//                Log.d(TAG, message);
            }

            public void onProviderEnabled(@NonNull String provider) {
                Log.d(TAG, "Provider enabled: " + provider);
            }

            public void onProviderDisabled(@NonNull String provider) {
                Log.d(TAG, "Provider disabled: " + provider);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "Provider status changed: " + provider);
            }
        };

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationSwitch.setChecked(true);
            locationManager.requestLocationUpdates(provider_info, 1000, 0, locationListener);

            location = locationManager.getLastKnownLocation(provider_info);
        } else {
            locationSwitch.setChecked(false);
        }

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compousndButton, boolean activated) {
                if (activated) {
                    ActivityCompat.requestPermissions(getActivity(), new String[] {
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION },
                            1);
                } else {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    locationSwitch.setChecked(false);
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPasswordEntered = oldPasswordTextField.getText().toString();
                if (oldPasswordTextField.getText().length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter your old password", Toast.LENGTH_SHORT).show();
                } else if (newPasswordTextField.getText().length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter your new password", Toast.LENGTH_SHORT).show();
                } else if (!oldPasswordEntered.equals(userSingleton.getPassword())) {
                    Toast.makeText(getActivity().getApplicationContext(), "Wrong old password", Toast.LENGTH_SHORT).show();
                } else if (newPasswordTextField.getText().length() < 6) {
                    Toast.makeText(getActivity().getApplicationContext(), "New password must be longer than 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    databaseManager.resetPassword(newPasswordTextField.getText().toString());
                    Toast.makeText(getContext(), "Password Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }



}