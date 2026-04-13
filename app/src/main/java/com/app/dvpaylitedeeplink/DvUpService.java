package com.app.dvpaylitedeeplink;

import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.app.dvup.IDvUpService;

public class DvUpService extends Service {

    private final IDvUpService.Stub binder = new IDvUpService.Stub() {
        @Override
        public void onDvPayLiteGoingToReboot() {
            Log.d("DvUp", "DvPayLite will reboot in 30 seconds");
            showRebootNotification();
            // Optional: prepare (stop tasks, logs, etc.)
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void showRebootNotification() {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Maintenance Alert")
                .setContentText("DvPayLite is going to reboot in 30 secs for maintenance")
                .setPriority(NotificationCompat.PRIORITY_MAX) // 🔥 VERY IMPORTANT
                .setDefaults(Notification.DEFAULT_ALL)        // 🔥 sound + vibration
                .setAutoCancel(true);

        manager.notify(1001, builder.build());
    }
}
