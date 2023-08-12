package com.swiftant.docs;

import android.os.Environment;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScannedDoc implements Serializable {
    public String vehicleRegNo;
    public String generatedPdfPath;
    public String scannedDocName;
    public String companyName;

    public ScannedDoc() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        scannedDocName = generateUniqueName();
        generatedPdfPath = new File(downloadsDir, scannedDocName).getAbsolutePath();
    }

    String generateUniqueName() {
        // Get the current date and time
        Date currentDate = new Date();

        // Format the date and time as a string
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm", Locale.getDefault());
        String dateString = dateFormat.format(currentDate);

        // Combine the prefix and formatted date to create a unique name

        return "generated_pdf_" + dateString + ".pdf";
    }
}
