package com.swiftant.docs;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Alert {
    Context context;

    public Alert(Context context) {
        this.context = context;
    }

    public void somethingWentWrong() {
        String methodName = Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName();
        try {
            String errorText = "Something Went Wrong! Please try again later.";
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setCancelable(false);
            alert.setMessage(errorText);
            alert.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
            //    mCrashlytics.recordException(e);
            //    MainActivity.MobileErrorLog( e.getStackTrace()[0].getFileName() + " - " + methodName,  e.getMessage(), e.toString());
        }
    }

    public void fromJsonArray(JSONArray jsonArray) {
        String methodName = Objects.requireNonNull(new Object() {
        }.getClass().getEnclosingMethod()).getName();
        try {
            JSONObject index = jsonArray.getJSONObject(0);
            String errorText;
            try {
                errorText = index.getString("errorText");
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setCancelable(false);
                alert.setMessage(errorText);
                alert.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
                alert.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            somethingWentWrong();
            e.printStackTrace();
            //    mCrashlytics.recordException(e);
            //    MainActivity.MobileErrorLog( e.getStackTrace()[0].getFileName() + " - " + methodName,  e.getMessage(), e.toString());
        }
    }

    public AlertDialog.Builder withTitleAndMessage(String title, String message) {
        return new AlertDialog.Builder(context).setTitle(title).setMessage(message);
    }

    public AlertDialog withMessage(String message) {
        return new AlertDialog.Builder(context).setMessage(message).create();
    }

    public void toastSomethingWentWrong() {
        Toast.makeText(context, context.getString(R.string.admin), Toast.LENGTH_SHORT).show();
    }

    public void toastAMessage(String message) {
        Toast.makeText(context, message + " ", Toast.LENGTH_SHORT).show();
    }
}
