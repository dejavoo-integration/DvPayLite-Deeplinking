package com.app.dvpaylitedeeplink

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.NotificationCompat
import com.app.dvpaylitedeeplink.unattended.DvPayLiteStatus
import com.denovo.app.invokeiposgo.enums.ApplicationType
import com.denovo.app.invokeiposgo.enums.TransactionType
import com.denovo.app.invokeiposgo.interfaces.GetDeviceListener
import com.denovo.app.invokeiposgo.interfaces.GetTPNListener
import com.denovo.app.invokeiposgo.interfaces.SettlementListener
import com.denovo.app.invokeiposgo.interfaces.TerminalAddListener
import com.denovo.app.invokeiposgo.interfaces.TransactionListener
import com.denovo.app.invokeiposgo.launcher.IntentApplication
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var registerApp:AppCompatButton
    private lateinit var makeTransaction:AppCompatButton
    private lateinit var terminalTPN:AppCompatEditText
    private lateinit var transactionAmout:AppCompatEditText
    private lateinit var transactionRefId:AppCompatEditText
    private lateinit var editTextTip:AppCompatEditText
    private lateinit var editTextIsvID:AppCompatEditText
    private lateinit var transactionSpinner: Spinner
    private lateinit var cardAcceptanceSpinner: Spinner
    private lateinit var paymentSpinner: Spinner
    private lateinit var receiptSpinner: Spinner
    private lateinit var approvalSpinner: Spinner
    private lateinit var txnType:String
    private lateinit var cardAcceptanceData:String
    private var paymentType:String = ""
    private lateinit var receiptType:String
    private lateinit var isTxnStatusScreenRequired:String
    private lateinit var buttonGetTPN:AppCompatButton
    private lateinit var buttonDeviceDetails:AppCompatButton
    private lateinit var buttonStatusCheck:AppCompatButton
    val dvPayLiteStatus = DvPayLiteStatus()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerApp = findViewById(R.id.register_app)
        makeTransaction = findViewById(R.id.make_transaction)
        transactionSpinner = findViewById(R.id.transaction_spinner)
        cardAcceptanceSpinner = findViewById(R.id.acceptance_spinner)
        paymentSpinner = findViewById(R.id.payment_spinner)
        receiptSpinner = findViewById(R.id.receipt_spinner)
        approvalSpinner = findViewById(R.id.approval_spinner)
        terminalTPN = findViewById(R.id.tpn)
        transactionAmout = findViewById(R.id.transaction_amount)
        transactionRefId = findViewById(R.id.transaction_refId)
        buttonGetTPN = findViewById(R.id.buttonGetTPN)
        buttonDeviceDetails = findViewById(R.id.buttonDeviceDetails)
        buttonStatusCheck = findViewById(R.id.buttonStatusCheck)
        editTextTip = findViewById(R.id.editTextTip)
        editTextIsvID = findViewById(R.id.edittext_isvId)
        val intentApplication = IntentApplication(applicationContext)

        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            intentApplication.handleResultCallBack(result)
        }

        buttonStatusCheck.setOnClickListener(View.OnClickListener {
            try {
                processStatusCheck(intentApplication, activityResultLauncher)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@MainActivity,
                    "Unable to check txn status ",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        buttonDeviceDetails.setOnClickListener(View.OnClickListener {
            getDeviceDetails(intentApplication, activityResultLauncher)
        })

        buttonGetTPN.setOnClickListener {
            getTpn(intentApplication, activityResultLauncher)
           /* val intent = Intent(this@MainActivity, MainActivity2::class.java)
            startActivity(intent)*/
        }

        registerApp.setOnClickListener {
            try {
                registerApp(intentApplication, activityResultLauncher)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@MainActivity,
                    "Unable to Register ",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        makeTransaction.setOnClickListener {
            try {
                makeTransaction(intentApplication, activityResultLauncher)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@MainActivity,
                    "Unable to make Transaction ",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        transactionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                txnType = parent?.getItemAtPosition(position).toString()
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do something when nothing is selected
            }
        }
        cardAcceptanceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                cardAcceptanceData = parent?.getItemAtPosition(position).toString()
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do something when nothing is selected
            }
        }
        paymentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                paymentType = parent?.getItemAtPosition(position).toString()
                updateTransactionSpinner()
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do something when nothing is selected
            }
        }

        receiptSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                receiptType = if (parent?.getItemAtPosition(position).toString() == "Empty(For Testing)"){
                   ""
                }else{
                   parent?.getItemAtPosition(position).toString()
                }
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do something when nothing is selected
            }
        }
        approvalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent?.getItemAtPosition(position).toString()
                isTxnStatusScreenRequired = when (selected) {
                    "Empty(For Testing)" -> {
                        ""
                    }
                    "No Tag(For Testing)" -> {
                        "No Tag"
                    }
                    else -> {
                        parent?.getItemAtPosition(position).toString()
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do something when nothing is selected
            }
        }

        updateTransactionSpinner()

    }

    private fun updateTransactionSpinner() {
        val transactionArray = when (paymentType) {
            "GIFT" -> {
                resources.getStringArray(R.array.Transactions_Gift)
            }
            "LOYALTY" -> {
                resources.getStringArray(R.array.Transactions_Loyalty)
            }
            else -> {
                resources.getStringArray(R.array.Transactions)
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transactionArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        transactionSpinner.adapter = adapter
    }

    private fun processStatusCheck(
        intentApplication: IntentApplication,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        if(transactionRefId.text.toString().isEmpty()){
            throw Exception()
        }

        val jsonRequest = JSONObject()
        jsonRequest.put("type", TransactionType.STATUS)
        jsonRequest.put("applicationType", "DVPAYLITE")
        jsonRequest.put("refId", transactionRefId.text.toString())
        Log.e("Request", "Request: $jsonRequest")

        intentApplication.setTransactionListener(object :
            TransactionListener {
            override fun onApplicationLaunched(result: JSONObject?) {
                //application launched success json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunched: " + result.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                //application launched failed json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunchFailed: $errorResult",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionSuccess(transactionResult: JSONObject?) {
                //Transaction Success json data
                Log.e("DVPAYLITE", "transactionResult.toString() - ${transactionResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + transactionResult.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionFailed(errorResult: JSONObject) {
                //Transaction Failed json data
                Log.e("DVPAYLITE", "errorResult.toString() - ${errorResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionFailed: $errorResult",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        intentApplication.performTransaction(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun getDeviceDetails(
        intentApplication: IntentApplication,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        intentApplication.setGetDeviceListener(object : GetDeviceListener {
            override fun onApplicationLaunched(response: JSONObject?) {
                Log.i("getTpn", "onApplicationLaunched")
            }

            override fun onApplicationLaunchFailed(response: JSONObject?) {
                Log.i("getTpn", "onApplicationLaunchFailed")
                Log.i("getTPN", "Payment app not installed. Please contact support team.")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + response.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onGetDevice(p0: JSONObject?) {
                Log.i("getTpn", "onGetDevice:" + p0.toString())
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + p0.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onGetDeviceFailed(p0: JSONObject?) {
                Log.i("getTpn", "onGetDeviceFailed:" + p0.toString())
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + p0.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

        })
        intentApplication.getDevice(getTPNRequest(), activityResultLauncher)
    }


    private fun getTpn(
        intentApplication: IntentApplication,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        intentApplication.setGetTPNListener(object : GetTPNListener {
            override fun onApplicationLaunched(response: JSONObject?) {
                Log.i("getTpn", "onApplicationLaunched")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + response.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onApplicationLaunchFailed(response: JSONObject?) {
                Log.i("getTpn", "onApplicationLaunchFailed")
                Log.i("getTPN", "Payment app not installed. Please contact support team.")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + response.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onGetTPN(response: JSONObject?) {
                Log.i("getTpn", "onGetTPN: $response")
                try {
                    response?.getString("tpn")?.let {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "TPN:" + it, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onTPNFailed(response: JSONObject?) {
                Log.i("getTpn", "onTPNFailed")
                Log.i("getTpn", "Enter terminal details manually and save it.")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + response.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        intentApplication.getTPN(getTPNRequest(), activityResultLauncher)
    }

    private fun getTPNRequest(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("applicationType", ApplicationType.DVPAYLITE)
        } catch (e: Exception) {
            e.printStackTrace()
            return jsonObject
        }
        Log.i("getTpn", "getTpn request: $jsonObject")
        return jsonObject
    }

    private fun makeTransaction(
        intentApplication: IntentApplication,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        when(txnType) {
            "SALE", "REFUND", "ACTIVATE", "REDEEM", "RELOAD", "REISSUE", "ADDPOINTS" -> {
                processSaleOrRefundTxn(intentApplication, activityResultLauncher)
            }

            "TIP ADJUST" -> {
                processTipAdjustTxn(intentApplication, activityResultLauncher)
            }

            "SETTLE" -> {
                processSettlement(intentApplication, activityResultLauncher)
            }

            "VOID" -> {
                processVoidTxn(intentApplication, activityResultLauncher)
            }

            "PRE_AUTH" -> {
                processAuthTxn(intentApplication, activityResultLauncher)
            }
            "INC_AUTH" -> {
                processIncAuthTxn(intentApplication, activityResultLauncher)
            }
            "TICKET" -> {
                processTicketTransaction(intentApplication, activityResultLauncher)
            }

            "INQUIRE", "DEACTIVATE" -> {
                processInquiryOrDeactivateGiftTxn(intentApplication, activityResultLauncher)
            }
        }
    }

    private fun processTicketTransaction(intentApplication: IntentApplication, activityResultLauncher: ActivityResultLauncher<Intent>) {
        if(transactionAmout.text.toString().isEmpty()){
            throw Exception()
        }

        if(transactionRefId.text.toString().isEmpty()){
            throw Exception()
        }

        val jsonRequest = JSONObject()
        jsonRequest.put("type", txnType)
        jsonRequest.put("amount", transactionAmout.text.toString())
        jsonRequest.put("tip", editTextTip.text.toString())
        jsonRequest.put("applicationType", "DVPAYLITE")
        jsonRequest.put("refId", transactionRefId.text.toString())
        jsonRequest.put("receiptType", receiptType)
        jsonRequest.put("IsvId", editTextIsvID.text.toString())
        if (isTxnStatusScreenRequired != "No Tag") {
            jsonRequest.put("isTxnStatusScreenRequired", isTxnStatusScreenRequired)
        }

        Log.e("DVPAYLITE","jsonRequest--$jsonRequest")

        intentApplication.setTransactionListener(object :
            TransactionListener {
            override fun onApplicationLaunched(result: JSONObject?) {
                //application launched success json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunched: " + result.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                //application launched failed json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunchFailed: $errorResult",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionSuccess(transactionResult: JSONObject?) {
                //Transaction Success json data
                Log.e("DVPAYLITE", "transactionResult.toString() - ${transactionResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + transactionResult.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionFailed(errorResult: JSONObject) {
                //Transaction Failed json data
                Log.e("DVPAYLITE", "errorResult.toString() - ${errorResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionFailed: $errorResult",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        intentApplication.performTransaction(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun processSettlement(intentApplication: IntentApplication, activityResultLauncher: ActivityResultLauncher<Intent>) {
        val jsonRequest = JSONObject()
        jsonRequest.put("type", txnType)
        jsonRequest.put("applicationType", "DVPAYLITE")

        Log.e("DVPAYLITE","jsonRequest--$jsonRequest")

        intentApplication.setSettlementListener(object :
            SettlementListener {

            override fun onSettlementSuccess(p0: JSONObject?) {
                Log.e("DVPAYLITE", "settlementResult.toString() - ${p0.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onSettlementSuccess " + p0.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onSettlementFailed(p0: JSONObject?) {
                Log.e("DVPAYLITE", "settle errorResult.toString() - ${p0.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onSettlementFailed: $p0",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        intentApplication.settleBatch(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun processTipAdjustTxn(intentApplication: IntentApplication,
                                    activityResultLauncher: ActivityResultLauncher<Intent>) {

        if(transactionAmout.text.toString().isEmpty()){
            throw Exception()
        }

        if(transactionRefId.text.toString().isEmpty()){
            throw Exception()
        }

        val jsonRequest = JSONObject()
        jsonRequest.put("type", txnType)
        jsonRequest.put("amount", transactionAmout.text.toString())
        jsonRequest.put("tip", editTextTip.text.toString())
        jsonRequest.put("applicationType", "DVPAYLITE")
        jsonRequest.put("refId", transactionRefId.text.toString())
        jsonRequest.put("IsvId", editTextIsvID.text.toString())

        Log.e("DVPAYLITE","jsonRequest--$jsonRequest")

        intentApplication.setTransactionListener(object :
            TransactionListener {
            override fun onApplicationLaunched(result: JSONObject?) {
                //application launched success json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunched: " + result.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                //application launched failed json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunchFailed: $errorResult",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionSuccess(transactionResult: JSONObject?) {
                //Transaction Success json data
                Log.e("DVPAYLITE", "transactionResult.toString() - ${transactionResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + transactionResult.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionFailed(errorResult: JSONObject) {
                //Transaction Failed json data
                Log.e("DVPAYLITE", "errorResult.toString() - ${errorResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionFailed: $errorResult",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        intentApplication.performTransaction(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun processSaleOrRefundTxn( intentApplication: IntentApplication,
                                        activityResultLauncher: ActivityResultLauncher<Intent>) {
        if(transactionAmout.text.toString().isEmpty()){
            throw Exception()
        }

        val jsonRequest = JSONObject()
        jsonRequest.put("type", txnType)
        jsonRequest.put("paymentType", paymentType)
        jsonRequest.put("amount", transactionAmout.text.toString())
        jsonRequest.put("tip", editTextTip.text.toString())
        jsonRequest.put("applicationType", "DVPAYLITE")
        jsonRequest.put("refId", "DL"+Utils.generateRandom(12))
        jsonRequest.put("receiptType", receiptType)
        jsonRequest.put("IsvId", editTextIsvID.text.toString())
        when (cardAcceptanceData) {
            "Empty(For Testing)" -> {
                jsonRequest.put("cardAcceptanceTime","")
            }
            "Random Value(30sec)" -> {
                jsonRequest.put("cardAcceptanceTime","30")
            }
            "Never" -> {
                jsonRequest.put("cardAcceptanceTime","Never")
            }

        }

        if (isTxnStatusScreenRequired != "No Tag") {
            jsonRequest.put("isTxnStatusScreenRequired", isTxnStatusScreenRequired)
        }
            /*//  Attach L2/L3 only if enabled
            if (enableL2L3Items) {
                val l2l3Data = buildL2L3Data()
                for (key in l2l3Data.keys()) {
                    jsonRequest.put(key, l2l3Data.get(key))
                }
            }*/
        Log.e("Request", "Request: $jsonRequest")

        intentApplication.setTransactionListener(object :
            TransactionListener {
            override fun onApplicationLaunched(result: JSONObject?) {
                //application launched success json data
                //listenDvPayLiteScreen(intentApplication, jsonRequest, activityResultLauncher)
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunched: " + result.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                //stopPolling()
                //application launched failed json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunchFailed: $errorResult",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionSuccess(transactionResult: JSONObject?) {
                //stopPolling()
                //Transaction Success json data
                Log.e("DVPAYLITE", "transactionResult.toString() - ${transactionResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + transactionResult.toString(),
                    Toast.LENGTH_LONG
                ).show()

                var sign = transactionResult!!.get("sign")
                Log.e("DVPAYLITE", "transactionResult.toString() - sign -- $sign")
            }

            override fun onTransactionFailed(errorResult: JSONObject) {
                //stopPolling()
                //Transaction Failed json data
                Log.e("DVPAYLITE", "errorResult.toString() - ${errorResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionFailed: $errorResult",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        intentApplication.performTransaction(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun listenDvPayLiteScreen(
        intentApplication: IntentApplication,
        jsonRequest: JSONObject,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        try {
            Handler().postDelayed(Runnable {
                stopPolling()
                dvPayLiteStatus.bindService(this@MainActivity)
                dvPayLiteStatus.setStatusCallBack {
                    intentApplication.performTransaction(
                        jsonRequest,
                        activityResultLauncher
                    )
                }
            },5000)
        } catch (e: Exception) {
            Log.e("Request", "Request: ${e.toString()}")
        }
    }

    private fun stopPolling(){
        try {
            dvPayLiteStatus.unbindService(this@MainActivity)
        } catch (e: Exception) {
        }
    }

    private fun processAuthTxn( intentApplication: IntentApplication,
                                        activityResultLauncher: ActivityResultLauncher<Intent>) {
        if(transactionAmout.text.toString().isEmpty()){
            throw Exception()
        }

        val jsonRequest = JSONObject()
        jsonRequest.put("type", txnType)
        jsonRequest.put("amount", transactionAmout.text.toString())
        jsonRequest.put("applicationType", "DVPAYLITE")
        jsonRequest.put("refId", "DL"+Utils.generateRandom(12))
        jsonRequest.put("receiptType", receiptType)
        jsonRequest.put("IsvId", editTextIsvID.text.toString())
        when (cardAcceptanceData) {
            "Empty(For Testing)" -> {
                jsonRequest.put("cardAcceptanceTime","")
            }
            "Random Value(30sec)" -> {
                jsonRequest.put("cardAcceptanceTime","30")
            }
            "Never" -> {
                jsonRequest.put("cardAcceptanceTime","Never")
            }
        }
        if (isTxnStatusScreenRequired != "No Tag") {
            jsonRequest.put("isTxnStatusScreenRequired", isTxnStatusScreenRequired)
        }
        Log.e("Request", "Request: $jsonRequest")

        intentApplication.setTransactionListener(object :
            TransactionListener {
            override fun onApplicationLaunched(result: JSONObject?) {
                //application launched success json data
                listenDvPayLiteScreen(intentApplication,jsonRequest,activityResultLauncher)
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunched: " + result.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                //application launched failed json data
                stopPolling()
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunchFailed: $errorResult",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionSuccess(transactionResult: JSONObject?) {
                //Transaction Success json data
                stopPolling()
                Log.e("DVPAYLITE", "transactionResult.toString() - ${transactionResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + transactionResult.toString(),
                    Toast.LENGTH_LONG
                ).show()

                var sign = transactionResult!!.get("sign")
                Log.e("DVPAYLITE", "transactionResult.toString() - sign -- $sign")
            }

            override fun onTransactionFailed(errorResult: JSONObject) {
                stopPolling()
                //Transaction Failed json data
                Log.e("DVPAYLITE", "errorResult.toString() - ${errorResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionFailed: $errorResult",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        intentApplication.performTransaction(
            jsonRequest,
            activityResultLauncher
        )
    }
    private fun processIncAuthTxn( intentApplication: IntentApplication,
                                        activityResultLauncher: ActivityResultLauncher<Intent>) {
        if(transactionAmout.text.toString().isEmpty()){
            throw Exception()
        }

        val jsonRequest = JSONObject()
        jsonRequest.put("type", txnType)
        jsonRequest.put("amount", transactionAmout.text.toString())
        jsonRequest.put("applicationType", "DVPAYLITE")
        jsonRequest.put("refId", transactionRefId.text.toString())
        jsonRequest.put("IsvId", editTextIsvID.text.toString())
        Log.e("Request", "Request: $jsonRequest")

        intentApplication.setTransactionListener(object :
            TransactionListener {
            override fun onApplicationLaunched(result: JSONObject?) {
                //application launched success json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunched: " + result.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                //application launched failed json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunchFailed: $errorResult",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionSuccess(transactionResult: JSONObject?) {
                //Transaction Success json data
                Log.e("DVPAYLITE", "transactionResult.toString() - ${transactionResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + transactionResult.toString(),
                    Toast.LENGTH_LONG
                ).show()

                var sign = transactionResult!!.get("sign")
                Log.e("DVPAYLITE", "transactionResult.toString() - sign -- $sign")
            }

            override fun onTransactionFailed(errorResult: JSONObject) {
                //Transaction Failed json data
                Log.e("DVPAYLITE", "errorResult.toString() - ${errorResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionFailed: $errorResult",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        intentApplication.performTransaction(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun processVoidTxn( intentApplication: IntentApplication,
                                        activityResultLauncher: ActivityResultLauncher<Intent>) {
        if(transactionRefId.text.toString().isEmpty()){
            throw Exception()
        }

        val jsonRequest = JSONObject()
        jsonRequest.put("type", txnType)
        jsonRequest.put("applicationType", "DVPAYLITE")
        jsonRequest.put("refId", transactionRefId.text.toString())
        jsonRequest.put("receiptType", receiptType)
        jsonRequest.put("IsvId", editTextIsvID.text.toString())
        if (isTxnStatusScreenRequired != "No Tag") {
            jsonRequest.put("isTxnStatusScreenRequired", isTxnStatusScreenRequired)
        }
        Log.e("Request", "Request: $jsonRequest")

        intentApplication.setTransactionListener(object :
            TransactionListener {
            override fun onApplicationLaunched(result: JSONObject?) {
                //application launched success json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunched: " + result.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                //application launched failed json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunchFailed: $errorResult",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionSuccess(transactionResult: JSONObject?) {
                //Transaction Success json data
                Log.e("DVPAYLITE", "transactionResult.toString() - ${transactionResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + transactionResult.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionFailed(errorResult: JSONObject) {
                //Transaction Failed json data
                Log.e("DVPAYLITE", "errorResult.toString() - ${errorResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionFailed: $errorResult",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        intentApplication.performTransaction(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun registerApp(
        intentApplication: IntentApplication,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        //   val jsonRequest:JSONObject = {“tpn”:”123456789012”, “applicationType”:”DVPAYLITE”}

        if(terminalTPN.text.toString().isEmpty()){
            throw Exception()
        }

        val jsonRequest = JSONObject()
        jsonRequest.put("tpn", terminalTPN.text.toString())
        jsonRequest.put("applicationType", "DVPAYLITE")

        intentApplication.setTerminalAddListener(object :
            TerminalAddListener {
            override fun onApplicationLaunched(addTerminal: JSONObject?) {
                //application launched success json data
                Toast.makeText(this@MainActivity, "onApplicationLaunched", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                //application launched failed json data
                Toast.makeText(this@MainActivity, "onApplicationLaunchFailed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onTerminalAdded(terminalResult: JSONObject?) {
                //Terminal added json data
                Toast.makeText(this@MainActivity, "onTerminalAdded", Toast.LENGTH_SHORT).show()
            }

            override fun onTerminalAddFailed(errorResult: JSONObject) {
                //Terminal add failed json data
                Toast.makeText(this@MainActivity, "onTerminalAddFailed", Toast.LENGTH_SHORT).show()
            }
        })
        intentApplication.addTerminal(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun validateIsvID(isvId: String): String {
        if (isvId.length !in 6..12) {
            editTextIsvID.error = "ISV ID must be between 6 and 12 digits"
            return ""
        }
        return isvId
    }

    private fun processInquiryOrDeactivateGiftTxn(
        intentApplication: IntentApplication,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        val jsonRequest = JSONObject()
        jsonRequest.put("type", txnType)
        jsonRequest.put("paymentType", paymentType)
        jsonRequest.put("applicationType", "DVPAYLITE")
        jsonRequest.put("refId", "DL" + Utils.generateRandom(12))
        jsonRequest.put("receiptType", receiptType)
        jsonRequest.put("IsvId", editTextIsvID.text.toString())
        if (isTxnStatusScreenRequired != "No Tag") {
            jsonRequest.put("isTxnStatusScreenRequired", isTxnStatusScreenRequired)
        }
        Log.e("Request", "Request: $jsonRequest")

        intentApplication.setTransactionListener(object :
            TransactionListener {
            override fun onApplicationLaunched(result: JSONObject?) {
                //application launched success json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunched: " + result.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                //application launched failed json data
                Toast.makeText(
                    this@MainActivity,
                    "onApplicationLaunchFailed: $errorResult",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionSuccess(transactionResult: JSONObject?) {
                //Transaction Success json data
                Log.e("DVPAYLITE", "transactionResult.toString() - ${transactionResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionSuccess: " + transactionResult.toString(),
                    Toast.LENGTH_LONG
                ).show()

                var sign = transactionResult!!.get("sign")
                Log.e("DVPAYLITE", "transactionResult.toString() - sign -- $sign")
            }

            override fun onTransactionFailed(errorResult: JSONObject) {
                //Transaction Failed json data
                Log.e("DVPAYLITE", "errorResult.toString() - ${errorResult.toString()}")
                Toast.makeText(
                    this@MainActivity,
                    "onTransactionFailed: $errorResult",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        intentApplication.performTransaction(
            jsonRequest,
            activityResultLauncher
        )
    }

   /* override fun onResume() {
        super.onResume()
        enableL2L3Items = PrefsHelper.getL2L3LineItems(this)
        Log.i("CartActivity", "Enable l2l3 Items $enableL2L3Items")
    }

    private fun buildL2L3Data(): JSONObject {


        val basePrice = transactionAmout.text.toString().toDoubleOrNull() ?: 0.0
        val quantity = 1
        var shippingAmount = 0.0
        var freightAmount = 0.0
        val itemBaseAmount = basePrice * quantity
        val discountRate = 0.5
        val localTaxRate = 0.5
        val stateTaxRate = 0.5
        val discountAmt = (itemBaseAmount * discountRate) / 100
        val localTaxAmt = (itemBaseAmount * localTaxRate) / 100
        val stateTaxAmt = (itemBaseAmount * stateTaxRate) / 100
        val totalTaxAmt = localTaxAmt + stateTaxAmt
        val totalTaxRate = stateTaxRate + localTaxRate
        val itemTotalAmount = itemBaseAmount - discountAmt + totalTaxAmt
        var altTaxAmount = 0.0
        var dutyAmount = 0.0
        return JSONObject().apply {
            put("IsvId", "23454")
            put("cardAcceptanceTime", "")
            put("TaxAmount", formatToTwoDecimalPlaces(totalTaxAmt))
            put("LocalTaxFlag", "1")
            put("NationalTaxAmount", formatToTwoDecimalPlaces(stateTaxAmt))
            put("LocalTaxAmount", formatToTwoDecimalPlaces(localTaxAmt))
            put("DestZipCode", "840")
            put("CustomerVatReg", "")
            put("SummaryCommodityCode", "0987")
            put("TaxRateApplied", "")
            put("TotalDiscountAmount", formatToTwoDecimalPlaces(discountAmt))
            put("PoNumber",  Utils.generateRandom(6))
            put("QuantityExpIndicator", "0")
            put("FreightAmount", formatToTwoDecimalPlaces(freightAmount))
            put("DutyAmount", formatToTwoDecimalPlaces(dutyAmount))
            put("ShipfromZipCode", "90")
            put("DestCountryCode", "840")
            put("LineItemCount", "1")
            put("AltTaxAmount", "")
            put("PurchaseIdentifier", Utils.generateRandom(9))
            put("CustomIdentifier", "")
            put("MerchantTaxId", "0")
            put("VatInvNum", "98989")
            put("ShippingAmount", formatToTwoDecimalPlaces(shippingAmount))
            put("totalLTaxAmount", formatToTwoDecimalPlaces(localTaxAmt))
            put("PurchaseIdFormatCode","")
            put("OrderDate",Utils.getCurrentDateYYMMDD())
            put("AltTaxIndicator","1")

          // Add line items
            val group = JSONObject().apply {
                put("CommodityCode", "10") // Optional: static or map from your Item model
                put("Description", "this is the product description")
                put("ProductCode", "2012") // optional
                put("Quantity", quantity)
                put("UnitOfMeasure", "ITM") // You can change to Kg, Pcs, etc.
                put("UnitCost",formatToTwoDecimalPlaces(basePrice) )
                put("DiscountAmount", formatToTwoDecimalPlaces(discountAmt))
                put("DiscountRate", discountRate)
                put("LocalTaxAmount", formatToTwoDecimalPlaces(localTaxAmt))
                put("NationalTaxAmount",formatToTwoDecimalPlaces(stateTaxAmt))
                put("NationalTaxRate","")
                put("LocalTaxRate", localTaxRate)
                put("StateTaxRate", stateTaxRate)
                put("TaxAmount", formatToTwoDecimalPlaces(totalTaxAmt))
                put("TaxRate", totalTaxRate)
                put("TotalAmount",formatToTwoDecimalPlaces(itemTotalAmount))
                put("DiscountIndicator", "Y")
                put("NetGrossIndicator", "N")
                put("DebitCreditIndicator", "D")
                put("QuantityExpIndicator", "N")
                put("DiscountRateExp", "2")
                put("ExtLineAmount", formatToTwoDecimalPlaces(itemBaseAmount))
                put("AltTaxAmount",formatToTwoDecimalPlaces(altTaxAmount))
                put("AltTaxID", "")
                put("ItemQuantityDecimal", "2")
                put("TaxTypeApplied", "")
                put("UnitPriceDecimal", "2")
                put("TaxIndicator", "1")
            }

            val groupArray = JSONArray  ().apply { put(group) }
            val level3LineItems = JSONObject().apply {
                put("group", groupArray)
            }
            put("Level3LineItems", level3LineItems)
        }
    }
    private fun formatToTwoDecimalPlaces(value: Double): String {
        return if (value==DEFAULT_VALUE) "0.00" else (String.format("%.2f", value))
    }*/

}