package com.swiftant.docs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

public class Loader {
    Context context;

    public Loader(Context context) {
        this.context = context;
    }

    // You can also add a method to show the dialog easily
    public static void showDialog(Dialog dialog) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    // You can add more methods as needed, such as dismissing the dialog
    public static void dismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public Dialog init() {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_view);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}
