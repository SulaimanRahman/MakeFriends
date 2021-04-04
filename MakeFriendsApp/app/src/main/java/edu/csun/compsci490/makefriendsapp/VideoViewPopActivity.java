package edu.csun.compsci490.makefriendsapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.net.URI;

import static android.provider.CalendarContract.CalendarCache.URI;

public class VideoViewPopActivity extends Activity {

    private VideoView videoView;
    private ImageView closeButtonImageView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_view_popup);
        String contentUri = getIntent().getStringExtra("videoUri");
        Log.d("VideoViewPopAct", "contentUri is " + contentUri);
        Uri contentUri1 = Uri.parse(contentUri);
        videoView = findViewById(R.id.videoView);
        closeButtonImageView = findViewById(R.id.close_button_image_view);
        closeButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        MediaController mediaController = new MediaController(videoView.getContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(contentUri1);
        videoView.start();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);



        int width = dm.widthPixels;
        int height = dm.heightPixels;
        //getWindow().setLayout((int)(width), (int)(height));
    }
}
