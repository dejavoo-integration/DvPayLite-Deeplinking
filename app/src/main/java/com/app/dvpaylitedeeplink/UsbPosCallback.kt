package com.app.dvpaylitedeeplink

interface UsbPosCallback {
    fun onResult(response: String?, totalBytes: Int)
}