package com.focusonemotions.android.bauggcm.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.focusonemotions.android.baug.gcm.app.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class RegisterServer extends AsyncTask<String, String, String> {

    final static String LOG = "RequestTask";
    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();
    private Context _context;

    public RegisterServer (Context context){
        _context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        String regid = params[1];
        String device_name = params[2];

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("regid", regid));
            nameValuePairs.add(new BasicNameValuePair("device_name", device_name));
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


            long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
            for (int i = 1; i <= MAX_ATTEMPTS; i++) {
                Log.d(LOG, "Attempt #" + i + " to register");
                try {
                    response = httpclient.execute(httppost);
                    StatusLine statusLine = response.getStatusLine();
                    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        out.close();
                        responseString = out.toString();
                        if(responseString.equalsIgnoreCase("ok")){
                            setRegistrationId(regid);
                        }
                        Log.d(LOG, responseString);
                    } else{
                        //Closes the connection.
                        response.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }

                    break;
                } catch (IOException e) {
                    Log.e(LOG, "Failed to register on attempt " + i + ":" + e);
                    if (i == MAX_ATTEMPTS) {
                        break;
                    }
                    try {
                        Log.d(LOG, "Sleeping for " + backoff + " ms before retry");
                        Thread.sleep(backoff);
                    } catch (InterruptedException e1) {
                        // Activity finished before we complete - exit.
                        Log.d(LOG, "Thread interrupted: abort remaining retries!");
                        Thread.currentThread().interrupt();
                        break;
                    }
                    // increase backoff exponentially
                    backoff *= 2;
                }
            }
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
    }

    private void setRegistrationId(String regid) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        preferences.edit().putString(_context.getString(R.string.property_reg_id), regid).commit();
    }
}
