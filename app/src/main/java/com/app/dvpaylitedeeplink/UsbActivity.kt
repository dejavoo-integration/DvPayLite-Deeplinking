package com.app.dvpaylitedeeplink

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlin.concurrent.thread

class UsbActivity : AppCompatActivity() {

    private lateinit var usbManager: UsbManager
    private lateinit var spinner: Spinner
    private lateinit var statusText: TextView
    private lateinit var inputText: EditText
    private lateinit var outputText: TextView

    private val portItems = mutableListOf<SerialPortItem>()
    private var selectedItem: SerialPortItem? = null
    private var selectedPort: UsbSerialPort? = null
    private val readBuffer = StringBuilder()
    @Volatile
    private var isBusy = false
    private var statusTimerHandler: Handler? = null

    private val ACTION_USB_PERMISSION =
        "com.example.usbandroidapplication.USB_PERMISSION"

    // ===================== ACTIVITY =====================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usb)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        spinner = findViewById(R.id.spinner_usb)
        statusText = findViewById(R.id.text_status)
        inputText = findViewById(R.id.input_request)
        outputText = findViewById(R.id.output_response)

        outputText.isVerticalScrollBarEnabled = true
        outputText.movementMethod = android.text.method.ScrollingMovementMethod.getInstance()
        inputText.setOnClickListener {
            showKeyboard(inputText)  // Show keyboard when clicking XML input
        }

        findViewById<Button>(R.id.button_refresh).setOnClickListener {
            refreshUsbList()
        }

        findViewById<Button>(R.id.button_send).setOnClickListener {
            hideKeyboard()
            sendData()
        }

        if (!isUsbHostSupported()) {
            statusText.text = "USB Host NOT supported"
            return
        }

        registerUsbReceiver()
        refreshUsbList()
        startUsbStatusTimer()
    }

    // ===================== USB LIST =====================

    private fun refreshUsbList() {
        portItems.clear()

        val drivers =
            UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)

        if (drivers.isEmpty()) {
            spinner.adapter = null
            statusText.text = "No USB devices found"
            return
        }

        drivers.forEach { driver ->
            driver.ports.forEach { port ->
                portItems.add(
                    SerialPortItem(
                        driver,
                        port,
                        "VID:${driver.device.vendorId} " +
                                "PID:${driver.device.productId} " +
                                "PORT:${port.portNumber}"
                    )
                )
            }
        }

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            portItems.map { it.displayName }
        )

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedItem = portItems[position]
                    statusText.text = "Port selected"

                    if (usbManager.hasPermission(selectedItem!!.driver.device)) {
                        statusText.text = "USB permission already granted"
                        openPort(selectedItem!!)
                    } else {
                        requestPermission(selectedItem!!)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        statusText.text = "USB device found (${portItems.size})"
    }

    // ===================== PERMISSION =====================

    private fun requestPermission(item: SerialPortItem) {
        statusText.text = "Requesting USB permission..."

        val permissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_USB_PERMISSION).setPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )

        usbManager.requestPermission(item.driver.device, permissionIntent)
    }

    // ===================== RECEIVER =====================

    private fun registerUsbReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                usbReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(usbReceiver, filter)
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    val granted = intent.getBooleanExtra(
                        UsbManager.EXTRA_PERMISSION_GRANTED,
                        false
                    )
                    if (granted) {
                        statusText.text = "USB permission granted"
                        selectedItem?.let { openPort(it) }
                    } else {
                        statusText.text = "USB permission denied"
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    selectedPort?.close()
                    selectedPort = null
                    statusText.text = "USB disconnected"
                    statusText.setTextColor(android.graphics.Color.RED)
                    isBusy = false
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    statusText.text = "USB attached, refreshing..."
                    refreshUsbList()
                }
            }
        }
    }

    // ===================== OPEN PORT =====================

    private fun openPort(item: SerialPortItem) {
        try {
            selectedPort?.close()

            val connection = usbManager.openDevice(item.driver.device)
            if (connection == null) {
                statusText.text = "Failed to open device"
                return
            }

            val port = item.port
            port.open(connection)
            port.setParameters(
                9600,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )

            selectedPort = port
            statusText.text = "USB Connected (Port ${port.portNumber})"
            statusText.setTextColor(android.graphics.Color.parseColor("#15803D"))

        } catch (e: Exception) {
            statusText.text = "Connection failed"
            statusText.setTextColor(android.graphics.Color.RED)
            Log.e("USB", "Open port error", e)
        }
    }

    // ===================== USB STATUS MONITORING =====================

    private fun isUsbConnected(): Boolean {
        return selectedPort?.isOpen == true
    }

    private fun startUsbStatusTimer() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                updateUsbStatus()
                handler.postDelayed(this, 3000) // every 3 seconds
            }
        }
        statusTimerHandler = handler
        handler.post(runnable)
    }

    private fun stopUsbStatusTimer() {
        statusTimerHandler?.removeCallbacksAndMessages(null)
        statusTimerHandler = null
    }

    private fun updateUsbStatus() {
        if (isUsbConnected()) {
            statusText.text = "USB Connected (Port ${selectedPort?.portNumber})"
            statusText.setTextColor(android.graphics.Color.parseColor("#15803D"))
        } else {
            statusText.text = "USB Disconnected"
            statusText.setTextColor(android.graphics.Color.RED)
            selectedPort?.close()
            selectedPort = null
        }
    }

    // ===================== SEND / RECEIVE =====================

    private fun sendData() {
        // Check connection first
        if (!isUsbConnected()) {
            statusText.text = "USB not connected"
            statusText.setTextColor(android.graphics.Color.RED)
            return
        }

        val text = inputText.text.toString()
        val port = selectedPort ?: run {
            statusText.text = "No port selected"
            return
        }

        if (text.isEmpty()) {
            statusText.text = "Input is empty"
            return
        }

        if (isBusy) {
            statusText.text = "POS is busy, wait for current transaction"
            return
        }

        // mark busy for this transaction
        isBusy = true

        runOnUiThread {
            outputText.setText("")
            statusText.text = "Waiting for POS transaction completion..."
        }

        thread {
            try {
                // 1) send request
                val sendBytes = (text + "\r\n").toByteArray()
                port.write(sendBytes, 1000)
                Log.d("USB-SEND", "Sent: $text")

                // 2) read full XML response for THIS transaction
                val fullResponse = readFullResponseAfterCompletion(
                    port = port,
                    startTag = "<response>",
                    closingTag = "</response>",
                    maxWaitTime = 120_000L,  // up to 120 seconds
                    idleTimeout = 30_000L    // tolerate 30 seconds of no data
                )

                runOnUiThread {
                    if (!fullResponse.isNullOrEmpty()) {
                        outputText.setText(fullResponse)
                        statusText.text = "Transaction completed!"
                        statusText.setTextColor(android.graphics.Color.parseColor("#15803D"))
                        Log.d("USB-RECV", "Full POS response received")
                    } else {
                        statusText.text = "Transaction incomplete or no response yet"
                        statusText.setTextColor(android.graphics.Color.parseColor("#F59E0B"))
                        Log.w("USB-RECV", "No full POS response received; buffer=${readBuffer.toString()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("USB", "Read/Write failed", e)
                runOnUiThread {
                    statusText.text = "Read/Write failed: ${e.message}"
                    statusText.setTextColor(android.graphics.Color.RED)
                }
            } finally {
                isBusy = false
            }
        }
    }

    /**
     * Reads full POS response only after the transaction is completed.
     * Waits until full XML is received with the closing tag.
     */
    private fun readFullResponseAfterCompletion(
        port: UsbSerialPort,
        startTag: String = "<response>",
        closingTag: String = "</response>",
        maxWaitTime: Long = 120000L,
        idleTimeout: Long = 30000L
    ): String? {
        val buffer = ByteArray(2048)
        val startTime = System.currentTimeMillis()
        var lastReadTime = startTime

        // clear any old data only at the *start* of a new transaction
        readBuffer.setLength(0)

        while (System.currentTimeMillis() - startTime < maxWaitTime) {
            val len = port.read(buffer, 1000) // blocking up to 1s
            if (len > 0) {
                val chunk = String(buffer, 0, len)
                readBuffer.append(chunk)
                lastReadTime = System.currentTimeMillis()

                val text = readBuffer.toString()
                val startIndex = text.indexOf(startTag)
                val endIndex = text.indexOf(closingTag)

                // found full XML
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    val endPos = endIndex + closingTag.length
                    val fullXml = text.substring(startIndex, endPos).trim()

                    // remove consumed message from buffer (in case more messages follow)
                    readBuffer.delete(0, endPos)

                    return fullXml
                }
            } else {
                // no data this cycle, check idle
                if (System.currentTimeMillis() - lastReadTime > idleTimeout) {
                    break
                }
            }
        }
        return null
    }

    // ===================== CLEANUP =====================

    override fun onDestroy() {
        stopUsbStatusTimer()
        unregisterReceiver(usbReceiver)
        selectedPort?.close()
        super.onDestroy()
    }

    private fun isUsbHostSupported(): Boolean {
        return packageManager.hasSystemFeature(
            PackageManager.FEATURE_USB_HOST
        )
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun showKeyboard(editText: EditText) {
        editText.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }
}

// ===================== MODEL =====================

data class SerialPortItem(
    val driver: UsbSerialDriver,
    val port: UsbSerialPort,
    val displayName: String
)


