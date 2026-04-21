package com.app.dvpaylitedeeplink;

import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.app.dejaup.IDejaUpService;

public class DejaUpService extends Service {

    private final IDejaUpService.Stub binder = new IDejaUpService.Stub() {
        @Override
        public void onDvPayLiteGoingToReboot(int seconds) {
            showRebootNotification(seconds);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void showRebootNotification(int seconds) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String message = "DvPayLite is going to reboot in " + seconds + " secs for maintenance";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Maintenance Alert")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX) // 🔥 VERY IMPORTANT
                .setDefaults(Notification.DEFAULT_ALL)        // 🔥 sound + vibration
                .setAutoCancel(true);

        manager.notify(1001, builder.build());
    }
}
