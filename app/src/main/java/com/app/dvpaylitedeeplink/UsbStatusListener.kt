package com.app.dvpaylitedeeplink

interface UsbStatusListener {

    fun onStatusChanged(state: UsbConnectionState, message: String? = null)
}