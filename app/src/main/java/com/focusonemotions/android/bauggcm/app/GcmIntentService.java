package com.focusonemotions.android.bauggcm.app;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.focusonemotions.android.baug.gcm.app.R;


public class GcmIntentService extends IntentService {
    public static final String TAG = "GcmFocusOnEmotionsTest";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    public void onHandleIntent(Intent intent){
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            String title = extras.getString("title");
            String message = extras.getString("message");
            Boolean hidden = extras.getString("hidden").equalsIgnoreCase("true");
            Boolean icon = extras.getString("icon").equalsIgnoreCase("true");
            Boolean audio = extras.getString("audio").equalsIgnoreCase("true");

            // Post notification of received message.
            sendNotification(hidden, title, message, icon, audio);
            Log.i(TAG, "Received: " + extras.toString());

        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Boolean hidden, String title, String msg, Boolean icon,
                                  Boolean audio) {
        Context context = getApplicationContext();
        if(audio){
            MediaPlayer mp = MediaPlayer.create(context, R.raw.ffvii_victory);
            mp.start();
        }
        if(hidden){
            Intent dialogIntent = new Intent(context, MainActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogIntent.putExtra("hidden", "This is a hidden notification");
            startActivity(dialogIntent);
            return;
        }
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        if(icon){
            mBuilder.setSmallIcon(R.drawable.ic_launcher);
        }else{
            mBuilder.setSmallIcon(R.drawable.ic_launcher2);
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}

