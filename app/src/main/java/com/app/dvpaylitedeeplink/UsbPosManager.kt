package com.app.dvpaylitedeeplink.usb

import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import com.app.dvpaylitedeeplink.UsbConnectionState
import com.app.dvpaylitedeeplink.UsbStatusListener
import com.hoho.android.usbserial.driver.*
import kotlin.concurrent.thread

class UsbPosManager(private val context: Context) {
    private val appContext = context.applicationContext

    @Volatile
    private var lastState: UsbConnectionState? = null

    @Volatile
    private var lastMessage: String? = null


    companion object {
        const val ACTION_USB_PERMISSION = "com.app.dvpaylitedeeplink.USB_PERMISSION"
        private const val TAG = "UsbPosManager"
    }

    private val usbManager =
        appContext.getSystemService(Context.USB_SERVICE) as UsbManager

    private var serialPort: UsbSerialPort? = null
    private var statusListener: UsbStatusListener? = null
    private val readBuffer = StringBuilder()

    /* ================= INIT ================= */

    fun init() {
        registerReceiver()
        autoConnectUsb()
    }

    fun release() {
        try { appContext.unregisterReceiver(usbReceiver) } catch (_: Exception) {}
        closeDevice()
    }

    fun setStatusListener(listener: UsbStatusListener) {
        statusListener = listener

        //  Immediately notify current state
        lastState?.let {
            listener.onStatusChanged(it, lastMessage)
        }
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
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        usbManager.requestPermission(device, pendingIntent)
        Log.d(TAG, "Requesting USB permission")
    }

    /* ================= OPEN PORT ================= */

    private fun tryOpenDevice(device: UsbDevice) {
        val driver = UsbSerialProber.getDefaultProber()
            .findAllDrivers(usbManager)
            .find { it.device == device } ?: return

        val port = driver.ports.first()

        try {
            val connection = usbManager.openDevice(device)
            if (connection == null) {
                Log.e(TAG, "openDevice returned null")
                return
            }

            port.open(connection)
            port.setParameters(
                115200,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )

            serialPort = port
            Log.d(TAG, "USB PORT OPENED SUCCESSFULLY")
            showToast("POS Connected")
            lastState = UsbConnectionState.CONNECTED
            lastMessage = "POS Connected"
            statusListener?.onStatusChanged(lastState!!, lastMessage)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to open port", e)
        }
    }

    /* ================= SEND / RECEIVE ================= */

  /*  fun sendAndReceive(request: String, onResult: (String?) -> Unit) {
        val port = serialPort
        Log.d(TAG, "sendAndReceive called, portOpen=${port?.isOpen}")

        if (port == null || !port.isOpen) {
            Log.e(TAG, "Port not open")
            onResult(null)
            return
        }

        thread {
            try {
                port.write((request + "\r\n").toByteArray(), 2000)
                Log.d(TAG, "Sent: $request")

                val response = readResponse(port)
                Log.d(TAG, "Received: $response")

                onResult(response)

            } catch (e: Exception) {
                Log.e(TAG, "Send/Receive failed", e)
                onResult(null)
            }
        }
    }*/

    fun sendAndReceive(request: String, onResult: (String?) -> Unit) {
        /*val request1 = "{\n" +
                "  \"Amount\": 25,\n" +
                "  \"TipAmount\": 2.5,\n" +
                "  \"ExternalReceipt\": \"\",\n" +
                "  \"PaymentType\": \"Credit\",\n" +
                "  \"ReferenceId\": \"111\",\n" +
                "  \"PrintReceipt\": \"No\",\n" +
                "  \"GetReceipt\": \"No\",\n" +
                "  \"MerchantNumber\": null,\n" +
                "  \"InvoiceNumber\": \"\",\n" +
                "  \"CaptureSignature\": false,\n" +
                "  \"GetExtendedData\": true,\n" +
                "  \"IsReadyForIS\": false,\n" +
                "  \"Tpn\": \"170725957498\",\n" +
                "  \"RegisterId\": \"1234\",\n" +
                "  \"Authkey\": \"zbhRAW9N6x\",\n" +
                "  \"CustomFields\": {},\n" +
                "  \"TransType\": \"Sale\"\n" +
                "}"*/
        val port = serialPort
        Log.d(TAG, "sendAndReceive called, portOpen=${port?.isOpen}")
        Log.d(TAG, "sendAndReceive called, portOpen=${request}")

        if (port == null || !port.isOpen) {
            Log.e(TAG, "Port not open")
            onResult(null)
            return
        }

        thread {
            try {
                // Convert request to bytes
              //  val bytes = (request + "\n").toByteArray(Charsets.UTF_8)
                var offset = 0
                val chunkSize = 32 // POS usually supports 32 bytes max per write
                val sendBytes = (request + "\r\n").toByteArray()
                port.write(sendBytes, 1000)
               /* while (offset < bytes.size) {
                    val length = minOf(chunkSize, bytes.size - offset)
                    val chunk = bytes.copyOfRange(offset, offset + length)

                    // Timeout > 0
                    port.write(chunk, 3000)
                    offset += length

                    // Small delay so POS can process
                    Thread.sleep(20)
                }*/

             //   Log.d(TAG, "Chunked write completed (${bytes.size} bytes)")
                Log.d("USB-SEND", "Sent: $request")

                // Now read response
                val response = readResponse(port)
                Log.d(TAG, "Received: $response")

                onResult(response)

            } catch (e: Exception) {
                Log.e(TAG, "Send/Receive failed", e)
                onResult(null)
            }
        }
    }


    private fun readResponse(port: UsbSerialPort): String? {
        val buffer = ByteArray(2048)
        val start = System.currentTimeMillis()
        readBuffer.setLength(0)

        while (System.currentTimeMillis() - start < 120_000) {
            val len = port.read(buffer, 1000)
            if (len > 0) {
                val chunk = String(buffer, 0, len)
                readBuffer.append(chunk)

                val text = readBuffer.toString().trim()
                if (
                    (text.startsWith("{") && text.endsWith("}")) ||
                    (text.startsWith("<") && text.endsWith(">"))
                ) {
                    return text
                }
            }
        }
        return null
    }

    /* ================= USB RECEIVER ================= */

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(appContext: Context, intent: Intent) {
            when (intent.action) {

                ACTION_USB_PERMISSION -> {
                    val device =
                        intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    val granted =
                        intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED,
                            false
                        )

                    Log.d(TAG, "Permission result: $granted")

                    if (granted && device != null) {
                        tryOpenDevice(device)
                    }
                }

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device =
                        intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (device != null) {
                        autoConnectUsb()
                    }
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    Log.d(TAG, "USB detached")
                    showToast("USB detached")
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
            appContext.registerReceiver(
                usbReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            appContext.registerReceiver(usbReceiver, filter)
        }
    }

    /* ================= CLOSE ================= */

    fun closeDevice() {
        try { serialPort?.close() } catch (_: Exception) {}
        serialPort = null
        Log.d(TAG, "USB port closed")
      //  showToast("POS Disconnected")
        lastState = UsbConnectionState.DISCONNECTED
        lastMessage = "POS Disconnected"
        statusListener?.onStatusChanged(lastState!!, lastMessage)
    }

    private fun showToast(message: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            android.widget.Toast.makeText(
                appContext.applicationContext,
                message,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }


    /* ================= CHUNKED WRITE ================= */
    private fun writeInChunks(port: UsbSerialPort, data: String) {
        val bytes = data.toByteArray(Charsets.UTF_8)
        var offset = 0
        val chunkSize = 32 // POS usually supports 32 bytes max per write

        while (offset < bytes.size) {
            val length = minOf(chunkSize, bytes.size - offset)
            val chunk = bytes.copyOfRange(offset, offset + length)

            // Timeout > 0 is required
            port.write(chunk, 3000)
            offset += length

            // Small delay so POS can process
            Thread.sleep(20)
        }

        // POS may expect newline at the end
        port.write("\n".toByteArray(), 2000)

        Log.d(TAG, "Chunked write completed (${bytes.size} bytes)")
    }
}
