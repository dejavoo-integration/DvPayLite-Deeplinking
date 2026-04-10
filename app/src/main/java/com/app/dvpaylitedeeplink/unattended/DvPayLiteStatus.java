package com.app.dvpaylitedeeplink.unattended;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.app.dvup.IDvPayLiteService;

public class DvPayLiteStatus {
    private IDvPayLiteService dvPayLiteService;
    private StatusCallBack statusCallBack;

    public void setStatusCallBack(StatusCallBack statusCallBack) {
        this.statusCallBack = statusCallBack;
    }

    private ServiceConnection payLiteConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dvPayLiteService = IDvPayLiteService.Stub.asInterface(service);
            startPolling();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dvPayLiteService = null;
            stopPolling();
        }
    };

    public void bindService(Activity activity){
        Log.d("DvUp", "bindService: ");
        Intent intent = new Intent("com.app.dvup.DvPayLite_SERVICE");
        intent.setPackage("com.denovo.app.denovopay");
        activity.bindService(intent, payLiteConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Activity activity) {
        try {
            if (dvPayLiteService != null) {
                stopPolling();
                activity.unbindService(payLiteConnection);
                dvPayLiteService = null;
                Log.d("DvUp", "Service unbound");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (dvPayLiteService != null) {
                try {
                    boolean isOnScreen = dvPayLiteService.isOnTargetScreen();
                    Log.d("DvUp", "Is target screen: " + isOnScreen);
                    // your logic
                    if (!isOnScreen) {
                        if (statusCallBack != null) {
                            statusCallBack.getStatus(true);
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            handler.postDelayed(this, 5000);
        }
    };

    private void startPolling() {
        Log.d("DvUp", "startPolling ");
        handler.post(pollRunnable);
    }

    public void stopPolling() {
        Log.d("DvUp", "stopPolling ");
        handler.removeCallbacksAndMessages(null);
    }


}
