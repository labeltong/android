package com.team4.caucapstone.labeltong;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

public class Progressbar {
    private static AppCompatDialog progressDialog;


    public static void progressON(Activity activity) {
        if (activity == null) {
            return;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            return;
        }
        progressDialog = new AppCompatDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.progress);
        progressDialog.show();
    }
    public static void progressON(Activity activity, String message) {
        if (activity == null) {
            return;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressSET(message);
        } else {
            progressDialog = new AppCompatDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.progress);
            progressDialog.show();

        }
        TextView progress_message = (TextView) progressDialog.findViewById(R.id.loading_msg);
        if (!TextUtils.isEmpty(message)) {
            progress_message.setText(message);
        }
    }
    public static void progressSET(String message) {
        if (progressDialog == null || !progressDialog.isShowing())
            return;
        TextView progress_message = (TextView) progressDialog.findViewById(R.id.loading_msg);
        if (!TextUtils.isEmpty(message))
            progress_message.setText(message);
    }
    public static void progressOFF() {
        Log.d("PROGRESSOFF", "called");
        if (progressDialog != null && progressDialog.isShowing()) {
            Log.d("PROGRESSOFF", "performed");
            progressDialog.dismiss();
        }
    }

}
