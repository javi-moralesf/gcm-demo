package com.focusonemotions.android.bauggcm.app;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * This {@code WakefulBroadcastReceiver} takes care of creating and managing a
 * partial wake lock for your app. It passes off the work of processing the GCM
 * message to an {@code IntentService}, while ensuring that the device does not
 * go back to sleep in the transition. The {@code IntentService} calls
 * {@code GcmBroadcastReceiver.completeWakefulIntent()} when it is ready to
 * release the wake lock.
 */

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String package_name = context.getPackageName();
        String class_name = GcmIntentService.class.getName();
        //Specify that GcmIntentService will handle the intent
        ComponentName component = new ComponentName(package_name, class_name);
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(component)));
        setResultCode(Activity.RESULT_OK);
    }
}
