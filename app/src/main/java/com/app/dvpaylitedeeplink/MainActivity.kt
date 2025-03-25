package com.app.dvpaylitedeeplink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.denovo.app.invokeiposgo.enums.ApplicationType
import com.denovo.app.invokeiposgo.enums.TransactionType
import com.denovo.app.invokeiposgo.interfaces.*
import com.denovo.app.invokeiposgo.launcher.IntentApplication
import com.denovo.app.invokeiposgo.models.InvokeApp
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var registerApp:AppCompatButton
    private lateinit var makeTransaction:AppCompatButton
    private lateinit var terminalTPN:AppCompatEditText
    private lateinit var transactionAmout:AppCompatEditText
    private lateinit var transactionRefId:AppCompatEditText
    private lateinit var editTextTip:AppCompatEditText
    private lateinit var transactionSpinner: Spinner
    private lateinit var paymentSpinner: Spinner
    private lateinit var receiptSpinner: Spinner
    private lateinit var txnType:String
    private lateinit var paymentType:String
    private lateinit var receiptType:String
    private lateinit var buttonGetTPN:AppCompatButton
    private lateinit var buttonDeviceDetails:AppCompatButton
    private lateinit var buttonStatusCheck:AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerApp = findViewById(R.id.register_app)
        makeTransaction = findViewById(R.id.make_transaction)
        transactionSpinner = findViewById(R.id.transaction_spinner)
        paymentSpinner = findViewById(R.id.payment_spinner)
        receiptSpinner = findViewById(R.id.receipt_spinner)
        terminalTPN = findViewById(R.id.tpn)
        transactionAmout = findViewById(R.id.transaction_amount)
        transactionRefId = findViewById(R.id.transaction_refId)
        buttonGetTPN = findViewById(R.id.buttonGetTPN)
        buttonDeviceDetails = findViewById(R.id.buttonDeviceDetails)
        buttonStatusCheck = findViewById(R.id.buttonStatusCheck)
        editTextTip = findViewById(R.id.editTextTip)
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
        paymentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                paymentType = parent?.getItemAtPosition(position).toString()
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
            "SALE", "REFUND" -> {
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
            "TICKET" -> {
                processTicketTransaction(intentApplication, activityResultLauncher)
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
}