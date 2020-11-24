package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ContactProfilePage extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_profile);
        StorageReference storageReference;
        final ImageView contactImg = findViewById(R.id.FPprofile_image);
        TextView contactBio = findViewById(R.id.FPbioField);
        TextView contactName = findViewById(R.id.FPFullName);
        TextView contactInterest = findViewById(R.id.FPInterest);

        String userName = "not yet set";
        String userBio = "blank";
        String userImg = "https://www.joyonlineschool.com/static/emptyuserphoto.png";
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            userName = extras.getString("userName");
            userBio = extras.getString("userBio");
            userImg = extras.getString("userImg");
        }

        contactName.setText(userName);
        contactBio.setText(userBio);
        storageReference = FirebaseStorage.getInstance().getReference().child(userImg);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext())
                        .load(uri.toString())
                        .placeholder(R.drawable.ic_baseline_account_circle_24)
                        .into(contactImg);
            }
        });

    }
}