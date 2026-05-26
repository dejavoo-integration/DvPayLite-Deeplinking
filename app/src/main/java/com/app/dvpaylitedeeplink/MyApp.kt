package com.app.dvpaylitedeeplink

import android.app.Application

class MyApp : Application() {

    lateinit var usbPosManager: UsbPosManager
        private set

    override fun onCreate() {
        super.onCreate()
        usbPosManager = UsbPosManager(this)
    }
}