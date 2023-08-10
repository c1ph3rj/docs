package com.swiftant.docs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Environment;

import com.swiftant.docs.captureDocsPkg.ImageAdapter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    File FILE_SAVE_LOCATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        FILE_SAVE_LOCATION = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        List<File> imageFiles = loadImagesFromDirectory(); // Implement this method to load images from a directory

        imageAdapter = new ImageAdapter(imageFiles);
        recyclerView.setAdapter(imageAdapter);
    }

    private List<File> loadImagesFromDirectory() {
        List<File> imageFiles = new ArrayList<>();

        // Specify the directory path where your images are stored
        File directory = FILE_SAVE_LOCATION;;

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    // Filter only image files (modify this condition based on your file naming pattern)
                    return name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png");
                }
            });

            if (files != null) {
                imageFiles.addAll(Arrays.asList(files));
            }
        }

        return imageFiles;
    }



}
