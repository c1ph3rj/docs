package com.swiftant.docs.captureDocsPkg;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.swiftant.docs.GalleryActivity;
import com.swiftant.docs.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CaptureDocs extends AppCompatActivity implements SurfaceHolder.Callback {
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int PERMISSIONS_REQUEST = 99;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_READSTORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private SurfaceView surfaceView;
    Camera camera;
    CardView captureImgBtn;
    TextView imageCountView;
    File FILE_SAVE_LOCATION;
    FrameLayout imagePreviewLayout;
    ImageView imagePreviewView;
    private long mLastClickTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_docs);

        Objects.requireNonNull(getSupportActionBar()).hide();

        if (hasPermission()) {
            FILE_SAVE_LOCATION = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

            captureImgBtn = findViewById(R.id.captureBtn);
            imageCountView = findViewById(R.id.countView);
            imagePreviewLayout = findViewById(R.id.capturedImageLayout);
            checkImagePreviewLayoutVisibility();
            imagePreviewView = findViewById(R.id.capturedImageView);

            imagePreviewLayout.setVisibility(View.GONE);

            surfaceView = findViewById(R.id.previewView);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(this);
            captureImgBtn.setOnClickListener(onClickCapture -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                runOnUiThread(this::captureImage);
            });

            imagePreviewLayout.setOnClickListener(OnClickImagePreview-> {
                startActivity(new Intent(this, GalleryActivity.class));
            });
        } else {
            requestPermission();
        }
    }



    private void captureImage() {
        camera.takePicture(null, null, (bytes, camera) -> {
            try {
                int imageWidth = 1080;
                int imageHeight = 720;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                Bitmap newBitMap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, false);
                newBitMap = rotateImage(newBitMap, 90);
                camera.startPreview();
                createImageFileFromBitmap(newBitMap);
                checkImagePreviewLayoutVisibility();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkImagePreviewLayoutVisibility() {
        if (hasFilesInDirectory(FILE_SAVE_LOCATION)) {
            imagePreviewLayout.setVisibility(View.VISIBLE);
            imageCountView.setText(String.valueOf(getFileCountInDirectory(FILE_SAVE_LOCATION)));
            imagePreviewView.setImageBitmap(createBitmapFromFile(getMostRecentFile(FILE_SAVE_LOCATION)));
            if(getFileCountInDirectory(FILE_SAVE_LOCATION)>0){
                imagePreviewLayout.setVisibility(View.VISIBLE);
            }else {
                imagePreviewLayout.setVisibility(View.GONE);
            }
        } else {
            imagePreviewLayout.setVisibility(View.GONE);
        }
    }

    private File getMostRecentFile(File directory) {
        File mostRecentFile = null;

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null && files.length > 0) {
                long maxTimestamp = Long.MIN_VALUE;

                for (File file : files) {
                    if (file.isFile() && file.lastModified() > maxTimestamp) {
                        mostRecentFile = file;
                        maxTimestamp = file.lastModified();
                    }
                }
            }
        }

        return mostRecentFile;
    }


    private Bitmap createBitmapFromFile(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888; // Adjust the configuration as needed

        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    private int getFileCountInDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                return files.length;
            }
        }
        return 0;
    }

    private boolean hasFilesInDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            return files != null && files.length > 0;
        }
        return false;
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.i(null, String.valueOf(checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED));
            Log.i(null, String.valueOf(checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED));
            Log.i(null, String.valueOf(checkSelfPermission(PERMISSION_READSTORAGE) == PackageManager.PERMISSION_GRANTED));
            Log.i(null, String.valueOf(checkSelfPermission(PERMISSION_LOCATION) == PackageManager.PERMISSION_GRANTED));
            return checkSelfPermission(PERMISSION_CAMERA) ==
                    PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_READSTORAGE) ==
                    PackageManager.PERMISSION_GRANTED;
        } else {
            Log.i(null, String.valueOf(checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED));
            Log.i(null, String.valueOf(checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED));
            Log.i(null, String.valueOf(checkSelfPermission(PERMISSION_READSTORAGE) == PackageManager.PERMISSION_GRANTED));
            Log.i(null, String.valueOf(checkSelfPermission(PERMISSION_LOCATION) == PackageManager.PERMISSION_GRANTED));
            return checkSelfPermission(PERMISSION_CAMERA) ==
                    PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_READSTORAGE) ==
                    PackageManager.PERMISSION_GRANTED;
        }
    }


    private void requestPermission() {
        try {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) ||
                    shouldShowRequestPermissionRationale(PERMISSION_STORAGE) ||
                    shouldShowRequestPermissionRationale(PERMISSION_READSTORAGE) ||
                    shouldShowRequestPermissionRationale(PERMISSION_LOCATION) ||
                    shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                Toast.makeText(this, "Camera, Location and storage permissions are required", Toast.LENGTH_LONG).show();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{PERMISSION_LOCATION, PERMISSION_CAMERA, PERMISSION_STORAGE, PERMISSION_READSTORAGE}, PERMISSIONS_REQUEST);
            } else {
                requestPermissions(new String[]{PERMISSION_LOCATION, PERMISSION_CAMERA, PERMISSION_STORAGE, PERMISSION_READSTORAGE}, PERMISSIONS_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (!allPermissionsGranted) {
                // Redirect to app settings permissions page
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(R.string.permissions_required_title);
                dialog.setMessage(R.string.permissions_required);
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            } else {
                surfaceView = findViewById(R.id.previewView);
                surfaceView.getHolder().addCallback(this);

            }
        }
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        try {
            // Open the front-facing camera
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            // Get the current display rotation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();

            // Set the display orientation based on the current display rotation
            switch (rotation) {
                case Surface.ROTATION_0:
                    camera.setDisplayOrientation(90);
                    break;
                case Surface.ROTATION_90:
                    camera.setDisplayOrientation(0);
                    break;
                case Surface.ROTATION_180:
                    camera.setDisplayOrientation(270);
                    break;
                case Surface.ROTATION_270:
                    camera.setDisplayOrientation(180);
                    break;
            }
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Handle surface changes if needed
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            camera.stopPreview();
            camera.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createImageFileFromBitmap(Bitmap bitmap) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        Log.i(TAG, imageFileName);
        File storageDir = FILE_SAVE_LOCATION;

        if (storageDir == null) {
            throw new IOException("External storage directory is null");
        }

        File imageFile = new File(storageDir, imageFileName);

        FileOutputStream outputStream = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.close();
    }

}