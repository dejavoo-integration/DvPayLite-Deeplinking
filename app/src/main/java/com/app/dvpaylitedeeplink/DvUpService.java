package com.app.dvpaylitedeeplink;

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

            // Optional: prepare (stop tasks, logs, etc.)
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
