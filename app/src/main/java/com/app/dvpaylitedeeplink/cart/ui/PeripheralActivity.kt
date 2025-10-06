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

        ivBack = findViewById<AppCompatImageView>(R.id.iv_back)
        scannerStartBtn = findViewById(R.id.btn_start)
        scannerStopBtn = findViewById(R.id.btn_stop)
        printerBtn = findViewById(R.id.btn_print)
        swipeReaderBtn = findViewById(R.id.btn_swipe)
        scannerData = findViewById(R.id.scanner_result)
        printStatus = findViewById(R.id.printer_result)
        swipeData = findViewById(R.id.swipe_result)


        ivBack.setOnClickListener {
            onBackPressed()
        }

        swipeReaderBtn.setOnClickListener{
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
                    }
                }

                override fun onFailure(error: String) {
                    // Show error
                    runOnUiThread {
                        Toast.makeText(this@PeripheralActivity, "Swipe failed: $error", Toast.LENGTH_SHORT).show()
                        swipeData.text = error
                    }
                }

                override fun onTimeOut(response: String?) {
                    runOnUiThread {
                        swipeData.text = "Swipe time out"
                        Toast.makeText(this@PeripheralActivity, "Swipe time out", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }


        printerBtn.setOnClickListener {
            Toast.makeText(this@PeripheralActivity, "Print Button Clicked", Toast.LENGTH_SHORT).show()
            clearAllData()
            printStatus.text = "Waiting for printing...."
            val request1= "<request>\n" +
                    "  <printer width=\"24\">\n" +
                    "    <L>Table:Table 1</L><R>Guests: 1</R>\n" +
                    "    <L>Time: 2025-05-02 10:27</L>\n" +
                    "    <L>1 Pechuga rellena</L><R>\$16.95</R>\n" +
                    "    <L>Qty: 1</L><R>Subtotal: \$16.95</R>\n" +
                    "    <L></L><R>Food Tax:\$1.02</R>\n" +
                    "    <L></L><R>Drink Tax:\$0.00</R>\n" +
                    "    <L></L><R>City Tax:\$0.17</R>\n" +
                    "    <L></L><R>Convience Fee:\$0.00</R>\n" +
                    "    <B><C>Total: \$18.14</C></B>\n" +
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
            val request = request2

            val intentPrintApplication = IntentPrintApplication(this)
            intentPrintApplication.setLaunchInterface(object : PrintLauncherInterface {

                override fun onPrintSuccess(printResult: com.denovo.app.invokekozen.printer.models.PrintResult?) {
                    Log.e("printer", "onPrintSuccess")
                    runOnUiThread {
                        Toast.makeText(this@PeripheralActivity, "Print success", Toast.LENGTH_SHORT).show()
                        printStatus.text = "SUCCESS"
                    }
                }

                override fun onPrintFailed(errorResult: com.denovo.app.invokekozen.printer.models.PrintErrorResult?) {
                    errorResult?.let {
                        runOnUiThread {
                            Toast.makeText(this@PeripheralActivity, "Print Failed", Toast.LENGTH_SHORT).show()
                            printStatus.text = "Failed"
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
            Toast.makeText(this@PeripheralActivity, "Scanner Start Button Clicked", Toast.LENGTH_SHORT).show()
            clearAllData()
            scannerData.text = "Please Scan barcode...."
            scannerActivity.startScan()
        }
        scannerStopBtn.setOnClickListener{
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
            }
            scannerActivity.stopScan()
        }

        override fun onFailure(errorMessage: String) {
            Log.e("Scan", "errorMessage----$errorMessage")
            runOnUiThread {
                scannerData.text = errorMessage
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