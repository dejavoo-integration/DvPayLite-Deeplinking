package com.app.dvpaylitedeeplink

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
        checkForAlreadyConnectedDevices()
        registerReceiver()
        //autoConnectUsb()
    }

    private fun checkForAlreadyConnectedDevices() {

        val deviceList = usbManager.deviceList
        Log.d(TAG,"USB device count = ${deviceList.size}")

        if (deviceList.isEmpty()) {
            Log.d(TAG, "No USB devices connected")
            return
        }

        val device = deviceList.values.first()

        Log.d(TAG, "Found USB device: ${device.deviceName}")

        autoConnectUsb()
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
        Log.d(TAG, "Checking permission for device: ${device.deviceName}")

        if (usbManager.hasPermission(device)) {
            Log.d(TAG, "USB permission already granted")
            tryOpenDevice(device)
        } else {
            Log.d(TAG, "USB permission NOT granted → requesting")
            requestPermission(device)
        }
    }

    private fun requestPermission(device: UsbDevice) {
        val intent = Intent(ACTION_USB_PERMISSION)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, pendingIntent)
    }

    /* ================= OPEN PORT ================= */

    private fun tryOpenDevice(device: UsbDevice) {
        Log.d(TAG,"Trying to open USB device")

        if (serialPort?.isOpen == true){
            Log.d(TAG,"Port already open")
            return
        }

        val driver = UsbSerialProber.getDefaultProber()
            .findAllDrivers(usbManager)
            .find { it.device == device } ?: return

        val port = driver.ports[0]

        try {

            val connection = usbManager.openDevice(device) ?: return

            port.open(connection)

            Thread.sleep(300) // POS devices need delay

            try {
                port.setParameters(
                    115200,
                    8,
                    UsbSerialPort.STOPBITS_1,
                    UsbSerialPort.PARITY_NONE
                )
            } catch (e: Exception) {
                Log.w(TAG, "setParameters failed, continuing anyway")
            }

            try {
                port.dtr = true
                port.rts = true
            } catch (e: Exception) {
                Log.w(TAG, "DTR/RTS not supported")
            }

            serialPort = port

            Log.i(TAG, "USB PORT CONNECTED")

            showToast("POS Connected")

            statusListener?.onStatusChanged(
                UsbConnectionState.CONNECTED,
                "POS Connected"
            )

        } catch (e: Exception) {

            Log.e(TAG, "Failed to open USB port", e)

            closeDevice()
        }
    }


    /* ================= SEND & RECEIVE ================= */

    fun sendAndReceive(request: String, callback: UsbPosCallback) {



        thread(start = true) {

            try {

                val port = serialPort

                if (port == null || !port.isOpen) {
                    callback.onResult(null, 0)
                    return@thread
                }

                /*  CLEAR OLD DATA (VERY IMPORTANT) */

                // 1. Clear internal buffer
                synchronized(readBuffer) {
                    readBuffer.setLength(0)
                }

                // 2. Clear USB input buffer (flush old unread data)
                try {
                    val tempBuffer = ByteArray(1024)
                    while (port.read(tempBuffer, 100) > 0) {
                        // keep reading until buffer empty
                    }
                    Log.i(TAG, "USB buffer cleared")
                } catch (e: Exception) {
                    Log.w(TAG, "Buffer clear error", e)
                }

                /* ---------------- WRITE REQUEST ---------------- */

                val requestBytes = (request + "\r\n").toByteArray(Charsets.UTF_8)

                writeInChunks(port, requestBytes)

                Log.i(TAG, "USB SENT: $request")

                /* ---------------- READ RESPONSE ---------------- */

                val buffer = ByteArray(4096)
                val startTime = System.currentTimeMillis()
                val timeout = 120_000L

                while (System.currentTimeMillis() - startTime < timeout) {

                    val len = port.read(buffer, 2000)

                    if (len > 0) {

                        val chunk = String(buffer, 0, len, Charsets.UTF_8)

                        synchronized(readBuffer) {

                            readBuffer.append(chunk)
                            val fullText = readBuffer.toString()

                            if (fullText.contains("</xmp>")) {

                                val start = fullText.indexOf("<xmp>")
                                val end = fullText.indexOf("</xmp>")

                                if (start != -1 && end != -1) {

                                    val xml = fullText.substring(start, end + 6)

                                    Log.i(TAG, "USB XML COMPLETE")

                                    callback.onResult(xml, xml.length)
                                    return@thread
                                }
                            }

                            if (isJsonComplete(fullText)) {
                                Log.i(TAG, "USB JSON COMPLETE")

                                callback.onResult(fullText, fullText.length)
                                return@thread
                            }
                        }
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
                if (fullText.contains("</response></xmp>")){
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

                    Handler(Looper.getMainLooper()).postDelayed({
                        autoConnectUsb()
                    }, 1500)
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

    private fun isJsonComplete(text: String): Boolean {
        return try {
            val trimmed = text.trim()

            if (trimmed.startsWith("{")) {
                org.json.JSONObject(trimmed)
                true
            } else if (trimmed.startsWith("[")) {
                org.json.JSONArray(trimmed)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun writeInChunks(port: UsbSerialPort, data: ByteArray) {

        val chunkSize = 256
        var offset = 0

        while (offset < data.size) {

            val length = minOf(chunkSize, data.size - offset)

            val chunk = data.copyOfRange(offset, offset + length)

            try {

                port.write(chunk, 5000)

            } catch (e: Exception) {

                Log.e(TAG, "USB write retry")

                Thread.sleep(200)

                port.write(chunk, 5000)
            }

            offset += length

            Thread.sleep(10) // give POS time to process
        }
    }

}
