package com.focusonemotions.android.bauggcm.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.focusonemotions.android.baug.gcm.app.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    Context context;
    GoogleCloudMessaging gcm;
    String regid;
    String SENDER_ID = "REPLACE-IT-WITH-DEVELOPER-CONSOLE-PROJECT-ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String hidden = intent.getStringExtra("hidden");
        if(hidden != null){
            alertMessage(hidden);
        }
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId();

            if (regid == null || regid.isEmpty()) {
                registerGcmInBackground();
            }
        } else {
            alertMessage("No valid Google Play Services APK found.");
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                alertMessage("This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private void registerGcmInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Registration ID: "+regid;
                    //send to server
                    registerToServer(regid);
                } catch (IOException ex) {
                    msg = "Error: "+ ex.getMessage();
                }
                Log.d(TAG, msg);
                return msg;
            }
        }.execute(null, null, null);
    }

    private void registerToServer(String regid){
        String url = getString(R.string.server_url);
        String device_name = getDeviceName();
        new RegisterServer(context).execute(url, regid, device_name);
    }

    private String getRegistrationId() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = getString(R.string.property_reg_id);
        String registrationId = preferences.getString(getString(R.string.property_reg_id), "");
        if(registrationId.isEmpty()) {
            return "";
        }
        // MAYBE Check if app was updated
        return registrationId;
    }



    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public void alertMessage(String message){
        new AlertDialog.Builder(this)
                .setTitle("BAUG")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
