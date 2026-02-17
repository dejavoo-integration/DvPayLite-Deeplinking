package com.app.dvpaylitedeeplink.cart.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.logger.LoggerManager
import com.app.dvpaylitedeeplink.printer.launcher.IntentPrintApplication
import com.app.dvpaylitedeeplink.printer.models.PrintErrorResult
import com.app.dvpaylitedeeplink.printer.models.PrintResult
import com.app.dvpaylitedeeplink.swipereader.common.MsrReader
import com.denovo.app.invokekozen.printer.interfaces.PrintLauncherInterface
import com.denovo.app.invokekozen.scanner.IScannerResult
import com.denovo.app.invokekozen.scanner.ScannerActivity
import com.denovo.app.invokekozen.swipereader.listeners.SwipeResult

class PeripheralActivity : AppCompatActivity() {

    companion object {
        private const val SCAN_TIMEOUT = 30000   // 30s
    }

    private lateinit var scannerStartBtn: AppCompatButton
    private lateinit var scannerStopBtn: AppCompatButton
    private lateinit var printerBtn: AppCompatButton
    private lateinit var swipeReaderBtn: AppCompatButton
    private lateinit var ivBack: AppCompatImageView
    private lateinit var scannerData: AppCompatTextView
    private lateinit var printStatus: AppCompatTextView
    private lateinit var swipeData: AppCompatTextView


    private lateinit var  scannerActivity: ScannerActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("My_tag","check device model: ${ Build.MODEL}")
        if( Build.MODEL == "P18"){
            scannerActivity = ScannerActivity(this, iscanResult, SCAN_TIMEOUT)
            setContentView(R.layout.activity_peripheral)
        }else{
            scannerActivity = ScannerActivity(iscanResult, SCAN_TIMEOUT)
            setContentView(R.layout.activity_peripheral_p8)
        }
        LoggerManager.log(this, "Open peripheralActivity")

        ivBack = findViewById<AppCompatImageView>(R.id.iv_back)
        scannerStartBtn = findViewById(R.id.btn_start)
        scannerStopBtn = findViewById(R.id.btn_stop)
        printerBtn = findViewById(R.id.btn_print)
        swipeReaderBtn = findViewById(R.id.btn_swipe)
        scannerData = findViewById(R.id.scanner_result)
        printStatus = findViewById(R.id.printer_result)
        swipeData = findViewById(R.id.swipe_result)


        ivBack.setOnClickListener {
            LoggerManager.log(this, "Clicked back button")
            onBackPressed()
        }

        swipeReaderBtn.setOnClickListener{
            LoggerManager.log(this, "Clicked Swipe Reader Button")
            Toast.makeText(this@PeripheralActivity, "Please swipe card", Toast.LENGTH_SHORT).show()
            clearAllData()
            swipeData.text = "Please swipe your card ...."
            val msrReader = MsrReader(this, 30) // timeout is in seconds (your class expects seconds, not ms)
            msrReader.read(object : SwipeResult {
                override fun onSuccess(data: String) {
                    // Show card swipe data in TextView
                    runOnUiThread {
                        Toast.makeText(this@PeripheralActivity, "Swipe success", Toast.LENGTH_SHORT).show()
                        swipeData.text = data
                        LoggerManager.log(this@PeripheralActivity, "Swipe Reader Data : ${data}")
                    }
                }

                override fun onFailure(error: String) {
                    // Show error
                    runOnUiThread {
                        Toast.makeText(this@PeripheralActivity, "Swipe failed: $error", Toast.LENGTH_SHORT).show()
                        swipeData.text = error
                        LoggerManager.log(this@PeripheralActivity, "Swipe Reader Data Failed : ${error}")
                    }
                }

                override fun onTimeOut(response: String?) {
                    runOnUiThread {
                        swipeData.text = "Swipe time out"
                        LoggerManager.log(this@PeripheralActivity, "Swipe Reader TimeOut")
                        Toast.makeText(this@PeripheralActivity, "Swipe time out", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }


        printerBtn.setOnClickListener {
            LoggerManager.log(this@PeripheralActivity, "Clicked printer Button")
            Toast.makeText(this@PeripheralActivity, "Print Button Clicked", Toast.LENGTH_SHORT).show()
            clearAllData()
            printStatus.text = "Waiting for printing...."
            val request1= "<request>\n" +
                    "  <printer width=\"24\">\n" +
                    "    <L>Test text format</L>\n" +
                    "    <L>Normal text</L>\n" +
                    "    <LG>\n" +
                    "      <L>Large Text</L>\n" +
                    "    </LG>\n" +
                    "    <INV>\n" +
                    "      <L>Inverted text</L>\n" +
                    "    </INV>\n" +
                    "    <CD>\n" +
                    "      <L>Condensed text</L>\n" +
                    "    </CD>\n" +
                    "    <B>\n" +
                    "      <L>Bold text</L>\n" +
                    "    </B>\n" +
                    "    <INV>\n" +
                    "      <LG>\n" +
                    "        <L>Large Inverted text</L>\n" +
                    "      </LG>\n" +
                    "    </INV>\n" +
                    "    <CD>\n" +
                    "      <LG>\n" +
                    "        <L>Large Condensed text</L>\n" +
                    "      </LG>\n" +
                    "    </CD>\n" +
                    "    <LG>\n" +
                    "      <B>\n" +
                    "        <L>Large Bold text</L>\n" +
                    "      </B>\n" +
                    "    </LG>\n" +
                    "    <INV>\n" +
                    "      <B>\n" +
                    "        <L>Bold Inverted text</L>\n" +
                    "      </B>\n" +
                    "    </INV>\n" +
                    "    <CD>\n" +
                    "      <B>\n" +
                    "        <L>Bold Condensed text</L>\n" +
                    "      </B>\n" +
                    "    </CD>\n" +
                    "    <BR />\n" +
                    "    <L>Text alignment</L>\n" +
                    "    <L>Left text</L>\n" +
                    "    <C>Center text</C>\n" +
                    "    <R>Right text</R>\n" +
                    "    <L>Left text</L>\n" +
                    "    <R>Right text</R>\n" +
                    "    <BR />\n" +
                    "    <L>Image</L>\n" +
                    "    <IMG>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</IMG>\n" +
                    "    <BR />\n" +
                    "    <L>Line Width</L>\n" +
                    "    <L>123456789012345678901234567890</L>\n" +
                    "    <BR />\n" +
                    "    <L>QR code</L>\n" +
                    "    <QR>https://app.theneo.io/dejavoo/spin</QR>\n" +
                    "    <BR />\n" +
                    "    <L>Feed</L>\n" +
                    "    <BR /><BR /><BR /><BR /><BR /><BR /><BR /><BR />\n" +
                    "  </printer>\n" +
                    "</request>"

            val request2="<request>\n" +
                    "  <RegisterId>1842001</RegisterId>\n" +
                    "  <AuthKey>NQnb45vZr6</AuthKey>\n" +
                    "  <printer width=\"24\">\n" +
                    "    <C></C>\n" +
                    "    <C>___________________________</C>\n" +
                    "    <C></C>\n" +
                    "    <C>QTY ITEM              PRICE</C>\n" +
                    "    <C></C>\n" +
                    "    <C>--- Seat: 1 ---</C>\n" +
                    "    <C>1   Wings (20 pc) ... 11.95</C>\n" +
                    "    <C>1   Chicken Finger...  7.95</C>\n" +
                    "    <C>      2 For 1              </C>\n" +
                    "    <C>___________________________</C>\n" +
                    "    <C></C>\n" +
                    "    <C>     Sub Total:       19.90</C>\n" +
                    "    <C>       Bev Tax:        0.00</C>\n" +
                    "    <C>      More Tax:        0.20</C>\n" +
                    "    <C>     Sales Tax:        1.20</C>\n" +
                    "    <C></C>\n" +
                    "    <C>  TOTAL (Cash):       21.30</C>\n" +
                    "    <C>  TOTAL (Non-Cash):   22.30</C>\n" +
                    "    <C></C>\n" +
                    "    <C>     Amt. Paid:       22.30</C>\n" +
                    "    <C></C>\n" +
                    "    <C>    DUE (Cash):        0.00</C>\n" +
                    "    <C> DUE (Non-Cash):       0.00</C>\n" +
                    "    <C></C>\n" +
                    "    <C>Thank you for visiting us!</C>\n" +
                    "    <C></C>\n" +
                    "  </printer>\n" +
                    "  <img></img>\n" +
                    "</request>"
            val request = request1



            val intentPrintApplication = IntentPrintApplication(this)
            intentPrintApplication.setLaunchInterface(object : PrintLauncherInterface {

                override fun onPrintSuccess(printResult: com.denovo.app.invokekozen.printer.models.PrintResult?) {
                    Log.e("printer", "onPrintSuccess")
                    runOnUiThread {
                        Toast.makeText(this@PeripheralActivity, "Print success", Toast.LENGTH_SHORT).show()
                        printStatus.text = "SUCCESS"
                        LoggerManager.log(this@PeripheralActivity, "Printer Success")
                    }
                }

                override fun onPrintFailed(errorResult: com.denovo.app.invokekozen.printer.models.PrintErrorResult?) {
                    errorResult?.let {
                        runOnUiThread {
                            Toast.makeText(this@PeripheralActivity, "Print Failed", Toast.LENGTH_SHORT).show()
                            printStatus.text = "Failed"
                            LoggerManager.log(this@PeripheralActivity, "Printer Failed")
                        }
                        Log.e("printer", "onPrintFailed----${it.errorMessage}")
                        Log.e("printer", "onPrintFailed----${it.errorCode}")
                        Log.e("printer", "onPrintFailed----${it.errorException}")
                    }
                }
            })
                 Log.i("my_tag","check request"+request);
            intentPrintApplication.launchPrinter(request)
        }

        scannerStartBtn.setOnClickListener{
            LoggerManager.log(this@PeripheralActivity, "Clicked Scanner start Button")
            Toast.makeText(this@PeripheralActivity, "Scanner Start Button Clicked", Toast.LENGTH_SHORT).show()
            clearAllData()
            scannerData.text = "Please Scan barcode...."
            scannerActivity.startScan()
        }
        scannerStopBtn.setOnClickListener{
            LoggerManager.log(this@PeripheralActivity, "Clicked Scanner Stop Button")
            clearAllData()
            Toast.makeText(this@PeripheralActivity, "Scanner Stop Button Clicked", Toast.LENGTH_SHORT).show()
            scannerActivity.stopScan()
        }

    }

    val iscanResult = object : IScannerResult {
        override fun onSuccess(result: String) {
            Log.e("Scan", "result----$result")
            runOnUiThread {
                scannerData.text = result
                LoggerManager.log(this@PeripheralActivity, "Scanner Result : $result")
            }
            scannerActivity.stopScan()
        }

        override fun onFailure(errorMessage: String) {
            Log.e("Scan", "errorMessage----$errorMessage")
            runOnUiThread {
                scannerData.text = errorMessage
                LoggerManager.log(this@PeripheralActivity, "Scanner Result Failure : $errorMessage")
            }
            scannerActivity.stopScan()
        }
    }


    private fun clearAllData(){
        scannerData.text = ""
        printStatus.text = ""
        swipeData.text = ""
    }
}