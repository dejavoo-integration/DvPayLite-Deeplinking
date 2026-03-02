package com.app.dvpaylitedeeplink.cart.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import com.app.dvpaylitedeeplink.MyApp
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.UsbConnectionState
import com.app.dvpaylitedeeplink.UsbPosCallback
import com.app.dvpaylitedeeplink.UsbStatusListener
import com.app.dvpaylitedeeplink.cart.PrefsHelper
import com.app.dvpaylitedeeplink.logger.LoggerManager
import com.app.dvpaylitedeeplink.usb.UsbPosManager
import com.denovo.app.invokeiposgo.interfaces.TerminalAddListener
import com.denovo.app.invokeiposgo.launcher.IntentApplication
import org.json.JSONObject


class RegistrationActivity : AppCompatActivity() {

    private lateinit var btnConfirm: AppCompatButton
    private lateinit var ivBack: AppCompatImageView

    private lateinit var rgMode: RadioGroup
    private lateinit var rbDeepLink: RadioButton
    private lateinit var rbCloud: RadioButton
    private lateinit var rbUsb: RadioButton
    private lateinit var rbLocal: RadioButton

    private lateinit var layoutDeepLink: LinearLayout
    private lateinit var layoutCloud: LinearLayout
    private lateinit var layoutUsb: LinearLayout
    private lateinit var layoutLocal: LinearLayout

    private lateinit var edtDeepLinkTPN: AppCompatEditText
    private lateinit var edtCloudRegisterId: AppCompatEditText
    private lateinit var edtCloudAuthKey: AppCompatEditText
    private lateinit var edtLocalIpAddress: AppCompatEditText
    private lateinit var edtLocalRegisterId: AppCompatEditText
    private lateinit var edtUsbRegisterId: AppCompatEditText

    private lateinit var tvUsbStatus: AppCompatTextView
    private lateinit var tvDeviceStatus: AppCompatTextView
    private lateinit var btnUsbConnect: AppCompatButton

    private lateinit var usbPosManager: UsbPosManager

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var intentApplication: IntentApplication


    private var selectedMode: Mode = Mode.DEEPLINK

    enum class Mode {
        DEEPLINK,
        CLOUD,
        LOCAL,
        USB
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        initViews()
        initUI()
        usbPosManager = (application as MyApp).usbPosManager
        usbPosManager.init()


        intentApplication = IntentApplication(applicationContext)

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                intentApplication.handleResultCallBack(result)
            }
        setupListeners()
        btnConfirm = findViewById(R.id.btnConfirm)
        ivBack = findViewById<AppCompatImageView>(R.id.iv_back)

        ivBack.setOnClickListener {
            onBackPressed()
        }

        if (usbPosManager.isConnected()) {
            tvUsbStatus.text = "POS Connected"
            tvUsbStatus.setTextColor(Color.GREEN)
        } else {
            tvUsbStatus.text = "POS Disconnected"
            tvUsbStatus.setTextColor(Color.RED)
        }
    }

    private fun initViews() {

        rgMode = findViewById(R.id.rgMode)
        rbDeepLink = findViewById(R.id.rbDeepLink)
        rbCloud = findViewById(R.id.rbCloud)
        rbUsb = findViewById(R.id.rbUsb)
        rbLocal = findViewById(R.id.rbLocal)


        layoutDeepLink = findViewById(R.id.layoutDeepLink)
        layoutCloud = findViewById(R.id.layoutCloud)
        layoutUsb = findViewById(R.id.layoutUsb)
        layoutLocal = findViewById(R.id.layoutLocal)

        edtDeepLinkTPN = findViewById(R.id.edtDeepLinkTPN)
        edtCloudRegisterId = findViewById(R.id.edtCloudRegisterid)
        edtCloudAuthKey = findViewById(R.id.edtCloudAuthKey)
        edtLocalIpAddress = findViewById(R.id.edtLocalIpAddress)
        edtLocalRegisterId = findViewById(R.id.edtLocalRegisterId)
        edtUsbRegisterId = findViewById(R.id.edtUsbRegister)
        tvUsbStatus = findViewById(R.id.tvUsbStatus)
        btnUsbConnect = findViewById(R.id.btnUsbConnect)
        tvDeviceStatus = findViewById(R.id.tvDeviceStatus)

        btnConfirm = findViewById(R.id.btnConfirm)
        ivBack = findViewById(R.id.iv_back)
    }

    private fun initUI() {
            val savedMode = PrefsHelper.getMode(this)

            when (savedMode) {

                Mode.DEEPLINK.name -> {
                    rbDeepLink.isChecked = true
                    selectedMode = Mode.DEEPLINK
                    edtDeepLinkTPN.setText(PrefsHelper.getTpn(this))
                    showDeepLink()

                }

                Mode.CLOUD.name -> {
                    rbCloud.isChecked = true
                    selectedMode = Mode.CLOUD
                    edtCloudRegisterId.setText(PrefsHelper.getRegisterId(this))
                    edtCloudAuthKey.setText(PrefsHelper.getAuthId(this))
                    showCloud()

                }

                Mode.USB.name -> {
                    rbUsb.isChecked = true
                    selectedMode = Mode.USB
                    edtUsbRegisterId.setText(PrefsHelper.getRegisterId(this))
                    showUsb()

                }

                Mode.LOCAL.name -> {
                    rbLocal.isChecked = true
                    selectedMode = Mode.LOCAL
                    edtLocalRegisterId.setText(PrefsHelper.getRegisterId(this))
                    edtLocalIpAddress.setText(PrefsHelper.getIpAddress(this))
                    showLocal()

            }
        }

        ivBack.setOnClickListener {
            finish()
        }
    }

    private fun registerApp(
        intentApplication: IntentApplication,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        //   val jsonRequest:JSONObject = {“tpn”:”123456789012”, “applicationType”:”DVPAYLITE”}

        if(edtDeepLinkTPN.text.toString().trim().isEmpty()){
            throw Exception()
        }

        val jsonRequest = JSONObject()
        jsonRequest.put("tpn", edtDeepLinkTPN.text.toString().trim())
        jsonRequest.put("applicationType", "DVPAYLITE")

        intentApplication.setTerminalAddListener(object :
            TerminalAddListener {
            override fun onApplicationLaunched(addTerminal: JSONObject?) {
                Toast.makeText(this@RegistrationActivity, "onApplicationLaunched", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                Toast.makeText(this@RegistrationActivity, "onApplicationLaunchFailed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onTerminalAdded(terminalResult: JSONObject?) {
                Toast.makeText(this@RegistrationActivity, "onTerminalAdded", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }

            override fun onTerminalAddFailed(errorResult: JSONObject) {
                Toast.makeText(this@RegistrationActivity, "onTerminalAddFailed", Toast.LENGTH_SHORT).show()
            }
        })
        intentApplication.addTerminal(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun setupListeners() {

        rgMode.setOnCheckedChangeListener { _, checkedId ->

            hideAllLayouts()

            when (checkedId) {

                R.id.rbDeepLink -> {
                    LoggerManager.log(this, "Select DeepLink Mode")
                    selectedMode = Mode.DEEPLINK
                    edtDeepLinkTPN.setText("")
                    showDeepLink()

                }

                R.id.rbCloud -> {
                    LoggerManager.log(this, "Select Cloud Mode")
                    selectedMode = Mode.CLOUD
                    edtCloudRegisterId.setText(PrefsHelper.getRegisterId(this))
                    edtCloudAuthKey.setText(PrefsHelper.getAuthId(this))
                    showCloud()

                }

                R.id.rbLocal -> {
                    LoggerManager.log(this, "Select Local Mode")
                    selectedMode = Mode.LOCAL
                    edtLocalRegisterId.setText("")
                    edtLocalIpAddress.setText("")
                    showLocal()

                }

                R.id.rbUsb -> {
                    LoggerManager.log(this, "Select USB Mode")
                    selectedMode = Mode.USB
                    edtUsbRegisterId.setText(PrefsHelper.getRegisterId(this))
                    showUsb()
                    setupUsb()

                }
            }
        }

        btnConfirm.setOnClickListener {
            if (validateInputs()) {
                handleConfirm()
            }
        }
    }

    private fun hideAllLayouts() {
        layoutDeepLink.visibility = View.GONE
        layoutCloud.visibility = View.GONE
        layoutUsb.visibility = View.GONE
        layoutLocal.visibility = View.GONE
    }

    private fun showDeepLink() {
        layoutDeepLink.visibility = View.VISIBLE
    }

    private fun showCloud() {
        layoutCloud.visibility = View.VISIBLE
    }

    private fun showLocal() {
        layoutLocal.visibility = View.VISIBLE
    }

    private fun showUsb() {
        layoutUsb.visibility = View.VISIBLE
    }

    // ===============================
    // Validation
    // ===============================

    private fun validateInputs(): Boolean {

        when (selectedMode) {

            Mode.DEEPLINK -> {
                val value = edtDeepLinkTPN.text.toString().trim()
               /* if (value.isEmpty()) {
                    edtDeepLinkTPN.error = "Enter DeepLink TPN"
                    return false
                }*/
            }

            Mode.CLOUD -> {
                val registerId = edtCloudRegisterId.text.toString().trim()
                val authKey = edtCloudAuthKey.text.toString().trim()

                if (registerId.isEmpty()) {
                    LoggerManager.log(this, "Enter Cloud register Id")
                    edtCloudRegisterId.error = "Enter Cloud Register Id"
                    return false
                }

                if (authKey.isEmpty()) {
                    LoggerManager.log(this, "Enter Cloud Auth key")
                    edtCloudAuthKey.error = "Enter Cloud Auth Key"
                    return false
                }
            }

            Mode.LOCAL -> {
                val registerId = edtLocalRegisterId.text.toString().trim()
                val ipAddress = edtLocalIpAddress.text.toString().trim()

                if (registerId.isEmpty()) {
                    LoggerManager.log(this, "Enter Local register Id")
                    edtLocalRegisterId.error = "Enter Local Register Id"
                    return false
                }

                if (ipAddress.isEmpty()) {
                    LoggerManager.log(this, "Enter Local ip Address")
                    edtLocalIpAddress.error = "Enter pos device network  Ip Address"
                    return false
                }
            }

            Mode.USB -> {
                val usbRegisterId = edtUsbRegisterId.text.toString().trim()
                if (usbRegisterId.isEmpty()) {
                    LoggerManager.log(this, "Enter USB Register Id")
                    edtUsbRegisterId.error = "Enter Register ID"
                    return false
                }
            }
        }

        return true
    }

    // ===============================
    // Confirm Handling
    // ===============================

    private fun handleConfirm() {

        when (selectedMode) {

            Mode.DEEPLINK -> {
                val tpn = edtDeepLinkTPN.text.toString().trim()
                try {
                    registerApp(intentApplication, activityResultLauncher)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@RegistrationActivity,
                        "Unable to Register ",
                        Toast.LENGTH_LONG
                    ).show()
                }
                PrefsHelper.saveDeepLink(this, tpn)
                PrefsHelper.saveMode(this, Mode.DEEPLINK.name)
                Toast.makeText(this, "DeepLink Selected\nTPN: $tpn", Toast.LENGTH_SHORT).show()
                LoggerManager.log(this, "DeepLink mode Register")
                finish()

            }

            Mode.CLOUD -> {
                val registerId = edtCloudRegisterId.text.toString().trim()
                val authKey = edtCloudAuthKey.text.toString().trim()
                PrefsHelper.saveCloud(this, registerId, authKey)
                PrefsHelper.saveMode(this, Mode.CLOUD.name)
                Toast.makeText(this, "Cloud Selected\nregisterId: $registerId\nauthKey: $authKey", Toast.LENGTH_SHORT).show()
                LoggerManager.log(this, "Confirm Cloud mode Register RegisterId: $registerId AuthKey: $authKey")
                finish()
            }

            Mode.LOCAL -> {
                val ipAddress = edtLocalIpAddress.text.toString().trim()
                val registerId = edtLocalRegisterId.text.toString().trim()
                PrefsHelper.saveLocal(this, registerId, ipAddress)
                PrefsHelper.saveMode(this, Mode.LOCAL.name)
                Toast.makeText(this, "Local Selected\nRegister Id: $registerId\nIp Address: $ipAddress", Toast.LENGTH_SHORT).show()
                LoggerManager.log(this, "confirm Local mode Register RegisterId: $registerId IpAddress: $ipAddress")
                finish()
            }

            Mode.USB -> {
                if(usbPosManager.isConnected()){
                    val usbRegisterId = edtUsbRegisterId.text.toString().trim()
                    PrefsHelper.saveUsb(this, usbRegisterId)
                    PrefsHelper.saveMode(this, Mode.USB.name)
                    Toast.makeText(this, "USB Selected\nusbRegisterId: $usbRegisterId", Toast.LENGTH_SHORT).show()
                    LoggerManager.log(this, "Confirm USB mode Register RegisterId: $usbRegisterId")
                  //  validation()
                    finish()
                }else{
                    tvDeviceStatus.text = "POS Device Not connected"
                    LoggerManager.log(this, "Please Connect POS USB first")
                    Toast.makeText(this, "Please Connect POS USB first", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupUsb() {
        usbPosManager.setStatusListener(object : UsbStatusListener {
            override fun onStatusChanged(state: UsbConnectionState, message: String?) {
                runOnUiThread {
                    when (state) {
                        UsbConnectionState.CONNECTING -> {
                            tvUsbStatus.text = "Connecting..."
                            LoggerManager.log(this@RegistrationActivity, "USB Connecting ..")
                        }
                        UsbConnectionState.CONNECTED -> {
                            tvUsbStatus.text = "POS Connected"
                            LoggerManager.log(this@RegistrationActivity, "USB Connected")
                            tvUsbStatus.setTextColor(Color.GREEN)
                        }
                        UsbConnectionState.PERMISSION_DENIED -> {
                            tvUsbStatus.text = "Permission Denied"
                            LoggerManager.log(this@RegistrationActivity, "USB Permission Denied")
                            tvUsbStatus.setTextColor(Color.RED)
                        }
                        UsbConnectionState.DISCONNECTED -> {
                            tvUsbStatus.text = "POS Disconnected"
                            LoggerManager.log(this@RegistrationActivity, "USB Disconnected")
                            tvUsbStatus.setTextColor(Color.RED)
                        }
                        UsbConnectionState.ERROR -> {
                            tvUsbStatus.text = message ?: "USB Error"
                            LoggerManager.log(this@RegistrationActivity, "USB Error")
                            tvUsbStatus.setTextColor(Color.RED)
                        }
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        setupUsb()
    }


    fun validation(){
        val progressDialog = android.app.AlertDialog.Builder(this)
            .setTitle("Please wait")
            .setMessage("Checking Device connection..")
            .setCancelable(false) // cannot dismiss by tapping outside
            .create()
        progressDialog.show()
        LoggerManager.log(this@RegistrationActivity, "Processing Dialog open")
        LoggerManager.log(this, "Processing USB Validation")
        val request = "<request><PaymentType>Credit</PaymentType><TransType>VALIDATE</TransType><Amount>1.00</Amount><Tip>0.00</Tip><CashbackAmount>0.00</CashbackAmount><Frequency>OneTime</Frequency><CustomFee>0.00</CustomFee><RefId></RefId><RegisterId>${edtUsbRegisterId.text.toString()}</RegisterId><AuthKey>vPXjq5X8fn</AuthKey><PrintReceipt>No</PrintReceipt><SigCapture>No</SigCapture></request>"
        Log.i("my_tag","check usb Validation request ${request}")
        usbPosManager?.sendAndReceive(request, object : UsbPosCallback {
            override fun onResult(response: String?, totalBytes: Int) {
                runOnUiThread {

                    if (!isFinishing && progressDialog.isShowing) {
                        LoggerManager.log(this@RegistrationActivity, "Processing Dialog Close")
                        progressDialog.dismiss()
                    }

                    if (response != null) {
                        Log.i("USB", response)
                        tvDeviceStatus.setText("Device Info : $response")
                        LoggerManager.log(this@RegistrationActivity, "Device Info : ${response}")
                        Toast.makeText(
                            this@RegistrationActivity,
                            response,
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        LoggerManager.log(this@RegistrationActivity, "Device Info  : No Pos Device Connected")
                        tvDeviceStatus.setText("Device Info : No Pos Device Connected")
                        Toast.makeText(this@RegistrationActivity,
                            "No Pos Device Connected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                }
            }
        })
    }

}

