package edu.csun.compsci490.makefriendsapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

public class ImageViewPopActivity extends Activity {

    private ImageView imageView;
    private ImageView closeButtonImageView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_popup);
        String contentUri = getIntent().getStringExtra("imageUri");
        Log.d("ImageViewPopAct", "contentUri is " + contentUri);
        Uri contentUri1 = Uri.parse(contentUri);
        imageView = findViewById(R.id.imageView);
        closeButtonImageView = findViewById(R.id.close_button_image_view_for_image_popup);
        closeButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Glide.with(imageView.getContext())
                .load(contentUri.toString())
                .into(imageView);
    }
}
