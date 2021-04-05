package edu.csun.compsci490.makefriendsapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ContactProfilePage extends AppCompatActivity {
    private FlexboxLayout interestBubbleParent;
    private Button interestBubble;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_profile);
        StorageReference storageReference;
        final ImageView contactImg = findViewById(R.id.FPprofile_image);
        TextView contactBio = findViewById(R.id.biographyTextField);
        TextView contactName = findViewById(R.id.FPFullName);
        interestBubbleParent = findViewById(R.id.flexbox_interestBubbleParent);

        //TextView contactInterest = findViewById(R.id.FPInterest);

        String userName = "not yet set";
        String userBio = "blank";
        String userImg = "https://www.joyonlineschool.com/static/emptyuserphoto.png";
        String interest = "none";
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            userName = extras.getString("userName").toUpperCase();
            userBio = extras.getString("userBio");
            userImg = extras.getString("userImg");
            interest = extras.getString("interests");
        }
        for(String temp : interest.split(" ")){
            interestBubble = new Button(getApplicationContext());
            interestBubble.setText(temp);
            interestBubble.setTextSize(16);
            interestBubble.setTextColor(getResources().getColor(R.color.white));
            interestBubble.setBackgroundResource(R.drawable.interest_bubble);
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, 80);
            params.setMargins(10,5,10,15);
            interestBubble.setLayoutParams(params);
            interestBubbleParent.addView(interestBubble);
        }
        contactName.setText(userName);
        contactBio.setText(userBio);
        //contactInterest.setText(interest);
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
