package com.swiftant.docs;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.swiftant.docs.captureDocsPkg.CaptureDocs;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            deleteAllFilesInDirectory(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
        }catch (Exception e){
            e.printStackTrace();
        }

        startActivity(new Intent(this, CaptureDocs.class));
    }

    private void deleteAllFilesInDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        Log.d(TAG, "File deleted: " + file.getAbsolutePath());
                    } else {
                        Log.e(TAG, "Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

}