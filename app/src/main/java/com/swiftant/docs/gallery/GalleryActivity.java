package com.swiftant.docs.gallery;

import static com.swiftant.docs.MainActivity.checkGPSStatus;
import static com.swiftant.docs.MainActivity.isNetworkConnected;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.swiftant.docs.ActionLogs;
import com.swiftant.docs.Alert;
import com.swiftant.docs.GeneratedResult;
import com.swiftant.docs.Loader;
import com.swiftant.docs.R;
import com.swiftant.docs.ScannedDoc;
import com.swiftant.docs.gallery.adatper.ImageAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GalleryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageAdapter imageAdapter;
    Button nextBtn;
    List<File> imageFiles;
    File FILE_SAVE_LOCATION;
    Alert alert;
    Dialog loader;
    boolean isCompanyDetected;
    ActionLogs actionLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Captured Documents");

        FILE_SAVE_LOCATION = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        init();


    }

    void init() {
        try {
            recyclerView = findViewById(R.id.recyclerView);
            nextBtn = findViewById(R.id.nextButton);
            alert = new Alert(this);
            actionLogs = new ActionLogs(this);
            loader = new Loader(this).init();

            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

            imageFiles = loadImagesFromDirectory(); // Implement this method to load images from a directory

            imageAdapter = new ImageAdapter(imageFiles);
            recyclerView.setAdapter(imageAdapter);

            nextBtn.setOnClickListener(onClickNext -> {
                try {
                    uploadImageApi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadImageApi() {
        String methodName = Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName();
        try {
            if (isNetworkConnected(this)) {
                if (checkGPSStatus(this)) {
                    Thread thread = new Thread(() -> {
                        File currentFile = imageFiles.get(imageFiles.size() - 1);
                        String appUrl = "https://api.ocr.space/parse/image";

                        OkHttpClient client = new OkHttpClient.Builder()
                                .connectTimeout(120, TimeUnit.SECONDS)
                                .writeTimeout(120, TimeUnit.SECONDS)
                                .readTimeout(120, TimeUnit.SECONDS)
                                .build();

                        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("file", currentFile.getAbsolutePath(),
                                        RequestBody.create(currentFile, MediaType.parse("application/octet-stream")))
                                .addFormDataPart("apikey", "K89085594988957")
                                .addFormDataPart("language", "eng")
                                .addFormDataPart("filetype", "jpeg")
                                .addFormDataPart("detectOrientation", "true")
                                .addFormDataPart("OCREngine", "2")
                                .addFormDataPart("isTable", "true")
                                .addFormDataPart("scale", "true")
                                .addFormDataPart("isOverlayRequired", "true")
                                .build();
                        Request request = new Request.Builder()
                                .url(appUrl)
                                .post(body)
                                .build();
                        Response staticResponse;
                        try {
                            runOnUiThread(() -> {
                                // LOADING INIT
                                if (!loader.isShowing()) {
                                    startLoadingView();
                                }
                            });
                            staticResponse = client.newCall(request).execute();
                            assert staticResponse.body() != null;
                            String staticRes = Objects.requireNonNull(staticResponse.body()).string();
                            Log.i(null, staticRes);

                            if (staticRes.isEmpty()) {
                                alert.somethingWentWrong();
                                loader.dismiss();
                            } else {
                                try {
                                    JSONObject response = new JSONObject(staticRes);
                                    JSONArray parsedResultsObj = response.getJSONArray("ParsedResults");
                                    for (int i = 0; i < parsedResultsObj.length(); i++) {
                                        JSONObject parsedResult = parsedResultsObj.getJSONObject(i);
                                        JSONArray Lines = parsedResult.getJSONObject("TextOverlay").getJSONArray("Lines");
                                        if(isCompanyDetected){
                                            break;
                                        }
                                        for (int j = 0; j < Lines.length(); j++) {
                                            JSONObject eachLine = Lines.getJSONObject(j);
                                            if (eachLine.getString("LineText").toLowerCase(Locale.ROOT).contains("cic")) {
                                                cicInsuranceDetails(Lines);
                                                break;
                                            }
//                                            else if(eachLine.getString("LineText").toLowerCase(Locale.ROOT).contains("pacis")){
//                                                KenIndianInsuranceDetails(Lines);
//                                            }
                                        }
                                    }
                                    runOnUiThread(()->{
                                        if(!isCompanyDetected){
                                            Toast.makeText(this, "Please Scan a valid Certificate!", Toast.LENGTH_SHORT).show();
                                        }
                                        loader.dismiss();
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                // LOADING STOP
                                stopLoadingView();
                                alert.somethingWentWrong();
                            });
                            actionLogs.recordException(e, e.getStackTrace()[0].getClassName() + " - " + methodName);
                        }
                    });
                    thread.start();
                } else {
                    try {
                        AlertDialog.Builder dialog = alert.withTitleAndMessage("Alert!", "GPS locations is not enabled.Please enable it");
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("Ok", (dialog1, which) -> {
                            //this will navigate user to the device location settings screen
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        });
                        dialog.show();
                    } catch (Exception e) {
                        actionLogs.recordException(e, e.getStackTrace()[0].getClassName() + " - " + methodName);
                        alert.somethingWentWrong();
                    }
                }
            } else {
                alert.toastAMessage(getString(R.string.noNetwork));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void KenIndianInsuranceDetails(JSONArray lines) {
        try {
            for (int j = 0; j < lines.length(); j++) {
                JSONObject eachLineObj = lines.getJSONObject(j);
                String eachLine = eachLineObj.getString("LineText");
                String currentLine = eachLine.toLowerCase(Locale.ROOT);
                if (currentLine.contains("reg. no. of vehicle") || currentLine.contains("reg.no.vehicle") || currentLine.contains("reg. no. of vehicle")) {
                    String vehicleRegNo = (eachLine.toLowerCase(Locale.ROOT).replace("registration", "")).replace(":", "");
                    if (!vehicleRegNo.isEmpty()) {
                        ScannedDoc scannedDoc = new ScannedDoc();
                        scannedDoc.companyName = "PACIS INSURANCE COMPANY LTD.";
                        scannedDoc.vehicleRegNo = vehicleRegNo;
                        Document generatePDF = new Document(PageSize.A4); // Use A4 page size or adjust as needed
                        PdfWriter.getInstance(generatePDF, new FileOutputStream(scannedDoc.generatedPdfPath));
                        generatePDF.open();

                        // Add images to the PDF
                        for (File eachFile : imageFiles) {
                            Image image = Image.getInstance(eachFile.getAbsolutePath());
//                            image.scaleToFit();
                            image.scaleToFit(PageSize.A4.getWidth() - generatePDF.leftMargin() - generatePDF.rightMargin(),
                                    PageSize.A4.getHeight() - generatePDF.topMargin() - generatePDF.bottomMargin());

                            // Center the image on the page
                            float x = (PageSize.A4.getWidth() - image.getScaledWidth()) / 2;
                            float y = (PageSize.A4.getHeight() - image.getScaledHeight()) / 2;
                            image.setAbsolutePosition(x, y);

                            generatePDF.add(image);
                            generatePDF.newPage();
                        }

                        // Close the document
                        generatePDF.close();
                        Intent generatedResultIntent = new Intent(this, GeneratedResult.class);
                        generatedResultIntent.putExtra("ScannedDoc", scannedDoc);
                        startActivity(generatedResultIntent);
                        loader.dismiss();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cicInsuranceDetails(JSONArray lines) {
        try {
            for (int j = 0; j < lines.length(); j++) {
                JSONObject eachLineObj = lines.getJSONObject(j);
                String eachLine = eachLineObj.getString("LineText");
                if (eachLine.toLowerCase(Locale.ROOT).contains("registration")) {
                    String vehicleRegNo = (eachLine.toLowerCase(Locale.ROOT).replace("registration", "")).replace(":", "");
                    if (!vehicleRegNo.isEmpty()) {
                        ScannedDoc scannedDoc = new ScannedDoc();
                        scannedDoc.companyName = "CIC General Insurance LTD.";
                        scannedDoc.vehicleRegNo = vehicleRegNo;
                        Document generatePDF = new Document(PageSize.A4); // Use A4 page size or adjust as needed
                        PdfWriter.getInstance(generatePDF, new FileOutputStream(scannedDoc.generatedPdfPath));
                        generatePDF.open();

                        // Add images to the PDF
                        for (File eachFile : imageFiles) {
                            Image image = Image.getInstance(eachFile.getAbsolutePath());
//                            image.scaleToFit();
                            image.scaleToFit(PageSize.A4.getWidth() - generatePDF.leftMargin() - generatePDF.rightMargin(),
                                    PageSize.A4.getHeight() - generatePDF.topMargin() - generatePDF.bottomMargin());

                            // Center the image on the page
                            float x = (PageSize.A4.getWidth() - image.getScaledWidth()) / 2;
                            float y = (PageSize.A4.getHeight() - image.getScaledHeight()) / 2;
                            image.setAbsolutePosition(x, y);

                            generatePDF.add(image);
                            generatePDF.newPage();
                        }

                        // Close the document
                        generatePDF.close();
                        Intent generatedResultIntent = new Intent(this, GeneratedResult.class);
                        generatedResultIntent.putExtra("ScannedDoc", scannedDoc);
                        startActivity(generatedResultIntent);
                        loader.dismiss();
                    }
                }
            }
        } catch (Exception e) {
            loader.dismiss();
            e.printStackTrace();
        }
    }

    private void startLoadingView() {
        try {
            loader.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopLoadingView() {
        try {
            loader.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<File> loadImagesFromDirectory() {
        List<File> imageFiles = new ArrayList<>();

        // Specify the directory path where your images are stored
        File directory = FILE_SAVE_LOCATION;

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> {
                // Filter only image files (modify this condition based on your file naming pattern)
                return name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png");
            });

            if (files != null) {
                imageFiles.addAll(Arrays.asList(files));
            }
        }

        Collections.reverse(imageFiles);

        return imageFiles;
    }
}
