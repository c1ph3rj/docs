package com.swiftant.docs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

public class GeneratedResult extends AppCompatActivity {
    TextView vehicleRegNoView;
    Button downloadPdfBtn;
    TextView companyNameView;
    Dialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_result);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Result");

        init();
    }

    void init() {
        try {
            companyNameView = findViewById(R.id.companyNameView);
            vehicleRegNoView = findViewById(R.id.vehicleRegNoView);
            downloadPdfBtn = findViewById(R.id.downloadPdfBtn);
            loader = new Loader(this).init();

            ScannedDoc scannedDoc =(ScannedDoc) getIntent().getSerializableExtra("ScannedDoc" );
            if(scannedDoc != null) {
                try {
                    companyNameView.setText(scannedDoc.companyName);
                    vehicleRegNoView.setText(scannedDoc.vehicleRegNo.toUpperCase(Locale.ROOT));
                    downloadPdfBtn.setOnClickListener(onClickDownloadPDF -> {
                        loader.show();
                        new Handler().postDelayed(() -> {
                            openPdfFile(scannedDoc.generatedPdfPath);
                            loader.dismiss();
                        }, 3000);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void openPdfFile(String filePath) {
        try {
            File pdfFile = new File(filePath);
            Uri pdfUri = FileProvider.getUriForFile(this, "com.swiftant.docs.fileprovider", pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}