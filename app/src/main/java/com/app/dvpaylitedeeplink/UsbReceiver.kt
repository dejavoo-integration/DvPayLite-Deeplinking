package com.app.dvpaylitedeeplink

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log

class UsbReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {

            "com.example.USB_PERMISSION" -> {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

                if (granted) {
                    Log.d("USB", "Permission GRANTED: $device")
                } else {
                    Log.e("USB", "Permission DENIED")
                }
            }
        }
    }
}