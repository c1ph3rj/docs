package com.swiftant.docs;

import android.content.Context;
import android.util.Log;

//import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class ActionLogs {
    public static final String ANSI_RESET = "\u001B";
    public static final String ANSI_YELLOW = "\u001B";
    public static final String ANSI_RED = "\u001B";
    Context context;
//    FirebaseCrashlytics firebaseCrashlytics;

    public ActionLogs(Context context) {
        this.context = context;
//        firebaseCrashlytics = FirebaseCrashlytics.getInstance();
    }

    public void recordException(Exception e, String methodName) {
        try {
//            firebaseCrashlytics.recordException(e);
            String errorOccurredIn = e.getStackTrace()[0].getFileName() + " - " + methodName + "\n";
            String errorMessage = e.getMessage();
            String errorDescription = e.toString();
            e.printStackTrace();
            Log.i("ERROR", "\n\n\nERROR OCCURRED :" + errorOccurredIn + "ERROR MESSAGE :" + errorMessage + "\n" + "ERROR DESCRIPTION :" + errorDescription + " \n\n\n\n\n\n\n\n\n\n");
        } catch (Exception er) {
            er.printStackTrace();
        }
    }
}
