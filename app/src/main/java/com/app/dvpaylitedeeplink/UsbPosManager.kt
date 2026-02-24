package com.app.dvpaylitedeeplink.usb

import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.app.dvpaylitedeeplink.UsbConnectionState
import com.app.dvpaylitedeeplink.UsbPosCallback
import com.app.dvpaylitedeeplink.UsbStatusListener
import com.hoho.android.usbserial.driver.*
import kotlin.concurrent.thread

class UsbPosManager(private val context: Context) {

    companion object {
        const val ACTION_USB_PERMISSION = "com.app.dvpaylitedeeplink.USB_PERMISSION"
        private const val TAG = "UsbPosManager"
    }

    private val usbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    private var serialPort: UsbSerialPort? = null
    private var statusListener: UsbStatusListener? = null

    // shared buffer (cleared per request)
    private val readBuffer = StringBuilder()

    /* ================= INIT ================= */

    fun init() {
        registerReceiver()
        //autoConnectUsb()
    }

    fun release() {
        try { context.unregisterReceiver(usbReceiver) } catch (_: Exception) {}
        closeDevice()
    }

    fun setStatusListener(listener: UsbStatusListener) {
        statusListener = listener
    }

    fun isConnected(): Boolean = serialPort?.isOpen == true

    /* ================= AUTO CONNECT ================= */

    private fun autoConnectUsb() {
        val drivers = UsbSerialProber.getDefaultProber()
            .findAllDrivers(usbManager)

        if (drivers.isEmpty()) {
            Log.d(TAG, "No USB devices found")
            return
        }

        val driver = drivers.first()
        val device = driver.device

        if (usbManager.hasPermission(device)) {
            tryOpenDevice(device)
        } else {
            requestPermission(device)
        }
    }

    private fun requestPermission(device: UsbDevice) {
        val intent = Intent(ACTION_USB_PERMISSION)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        usbManager.requestPermission(device, pendingIntent)
    }

    /* ================= OPEN PORT ================= */

    private fun tryOpenDevice(device: UsbDevice) {
        if (serialPort?.isOpen == true) return

        val driver = UsbSerialProber.getDefaultProber()
            .findAllDrivers(usbManager)
            .find { it.device == device } ?: return

        val port = driver.ports.first()

        try {
            val connection = usbManager.openDevice(device) ?: return
            port.open(connection)
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

            // Key: Set DTR/RTS to true to tell the POS we are ready to receive
            port.dtr = true
            port.rts = true

            serialPort = port
            Log.i(TAG, "USB PORT RE-CONNECTED")
            showToast("POS Connected")
            statusListener?.onStatusChanged(UsbConnectionState.CONNECTED, "POS Connected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open USB port", e)
        }
    }

    /* ================= SEND & RECEIVE ================= */

    fun sendAndReceive(request: String, callback: UsbPosCallback) {
        thread {
            try {
                // 1. Clear buffer before starting
                synchronized(readBuffer) { readBuffer.setLength(0) }

                // 2. Initial Send (Wait up to 3 seconds for port to be ready)
                val startTime = System.currentTimeMillis()
                while (serialPort == null || !serialPort!!.isOpen) {
                    if (System.currentTimeMillis() - startTime > 5000) {
                        callback.onResult(null, 0)
                        return@thread
                    }
                    Thread.sleep(200)
                }

                val sendBytes = (request + "\r\n").toByteArray(Charsets.UTF_8)
                serialPort?.write(sendBytes, 3000)
                Log.i(TAG, "USB SENT: $request")

                // 3. Resilient Read Loop (120 second timeout)
                val readTimeout = 120_000L
                val loopStart = System.currentTimeMillis()
                val buffer = ByteArray(2048)

                while (System.currentTimeMillis() - loopStart < readTimeout) {
                    val currentPort = serialPort // Capture current instance

                    if (currentPort != null && currentPort.isOpen) {
                        try {
                            val len = currentPort.read(buffer, 1000)
                            if (len > 0) {
                                val chunk = String(buffer, 0, len, Charsets.UTF_8)
                                synchronized(readBuffer) {
                                    readBuffer.append(chunk)
                                    val fullText = readBuffer.toString()

                                    if (fullText.contains("</xmp>")) {
                                        Log.i(TAG, "USB COMPLETE")
                                        callback.onResult(fullText, fullText.length)
                                        return@thread
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Read error (likely disconnect), waiting for reconnect...")
                            Thread.sleep(1000) // Wait for BroadcastReceiver to fix the port
                        }
                    } else {
                        // Port is null or closed, just wait for the autoConnect logic to kick in
                        Thread.sleep(500)
                    }
                }

                Log.e(TAG, "USB READ TIMEOUT")
                callback.onResult(null, 0)

            } catch (e: Exception) {
                Log.e(TAG, "Global sendAndReceive Error", e)
                callback.onResult(null, 0)
            }
        }
    }

    /* ================= READ COMPLETE RESPONSE ================= */

    private fun readResponseWithBytes(
        port: UsbSerialPort
    ): Pair<String?, Int> {

        val buffer = ByteArray(2048)
        val startTime = System.currentTimeMillis()
        var totalBytes = 0

        readBuffer.setLength(0)

        while (System.currentTimeMillis() - startTime < 120_000) {

            val len = port.read(buffer, 1000)

            if (len > 0) {
                totalBytes += len

                val chunk = String(buffer, 0, len, Charsets.UTF_8)
                readBuffer.append(chunk)

                Log.i(TAG, "USB chunk received: $len bytes")

                val fullText = readBuffer.toString()

                // POS XML termination condition
                if (fullText.contains("</xmp>")) {
                    Log.i(TAG, "USB COMPLETE bytes=$totalBytes")
                    return Pair(fullText, totalBytes)
                }
            }
        }

        Log.e(TAG, "USB TIMEOUT bytes=$totalBytes")
        return Pair(null, totalBytes)
    }

    /* ================= USB RECEIVER ================= */

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {

                ACTION_USB_PERMISSION -> {
                    val device =
                        intent.getParcelableExtra<UsbDevice>(
                            UsbManager.EXTRA_DEVICE
                        )

                    val granted =
                        intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED,
                            false
                        )

                    if (granted && device != null) {
                        tryOpenDevice(device)
                    }
                }

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    autoConnectUsb()
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    closeDevice()
                }
            }
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                usbReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(usbReceiver, filter)
        }
    }

    /* ================= CLOSE ================= */

    fun closeDevice() {
        try { serialPort?.close() } catch (_: Exception) {}
        serialPort = null
           showToast("POS Disconnected")
        statusListener?.onStatusChanged(
            UsbConnectionState.DISCONNECTED,
            "POS Disconnected"
        )
    }

    private fun showToast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                context.applicationContext,
                msg,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
