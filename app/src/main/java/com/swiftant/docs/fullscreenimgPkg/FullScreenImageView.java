package com.swiftant.docs.fullscreenimgPkg;

import static com.swiftant.docs.gallery.adatper.ImageAdapter.selectedFile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.swiftant.docs.R;

public class FullScreenImageView extends AppCompatActivity {
    ZoomableImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_view);

        init();
    }

    void init() {
        try {
            if(selectedFile == null){
                finish();
            }else {
                imageView.setImageFile(selectedFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}