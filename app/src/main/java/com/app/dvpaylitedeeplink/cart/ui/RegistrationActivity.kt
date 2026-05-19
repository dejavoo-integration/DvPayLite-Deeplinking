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
import java.util.logging.Logger


class RegistrationActivity : AppCompatActivity() {

    private lateinit var btnConfirm: AppCompatButton
    private lateinit var ivBack: AppCompatImageView

    private lateinit var rgMode: RadioGroup
    private lateinit var rgAvs: RadioGroup
    private lateinit var rgLoaderLogo: RadioGroup
    private lateinit var rbDeepLink: RadioButton
    private lateinit var rbCloud: RadioButton
    private lateinit var rbUsb: RadioButton
    private lateinit var rfJsonLocal: RadioButton
    private lateinit var rfXmlLocal: RadioButton
    private lateinit var rfJsonCloud: RadioButton
    private lateinit var rfXmlCloud: RadioButton
    private lateinit var rfJsonUsb: RadioButton
    private lateinit var rfxmlUsb: RadioButton
    private lateinit var rbLocal: RadioButton
    private lateinit var rbAvsYes: RadioButton
    private lateinit var rbAvsNo: RadioButton
    private lateinit var rbLogoYes: RadioButton
    private lateinit var rbLogoNo: RadioButton

    private lateinit var layoutDeepLink: LinearLayout
    private lateinit var layoutCloud: LinearLayout
    private lateinit var layoutUsb: LinearLayout
    private lateinit var layoutLocal: LinearLayout

    private lateinit var edtDeepLinkTPN: AppCompatEditText

    private lateinit var edtPrimaryColor: AppCompatEditText
    private lateinit var edtSecondaryColor: AppCompatEditText
    private lateinit var edtNegativeColor: AppCompatEditText
    private lateinit var edtFont: AppCompatEditText

    private lateinit var edtCloudRegisterId: AppCompatEditText
    private lateinit var edtCloudAuthKey: AppCompatEditText
    private lateinit var edtLocalIpAddress: AppCompatEditText
    private lateinit var edtLocalRegisterId: AppCompatEditText
    private lateinit var edtUsbRegisterId: AppCompatEditText
    private lateinit var edtUsbTPNNumber: AppCompatEditText

    private lateinit var tvUsbStatus: AppCompatTextView
    private lateinit var tvDeviceStatus: AppCompatTextView
    private lateinit var btnUsbConnect: AppCompatButton

    private lateinit var usbPosManager: UsbPosManager

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var intentApplication: IntentApplication

    private lateinit var cbBulkTest: SwitchCompat
    private lateinit var edtBulkCount: AppCompatEditText

    private var selectedMode: Mode = Mode.DEEPLINK
    private var selectedAvs: SelectionOption = SelectionOption.YES
    private var selectedLogo: SelectionOption = SelectionOption.YES

    enum class Mode {
        DEEPLINK,
        CLOUD,
        LOCAL,
        USB
    }

    enum class SelectionOption {
        YES,
        NO
    }

    enum class RequestFormat {
        XML,
        JSON
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
        rfJsonCloud = findViewById(R.id.rbCloudJson)
        rfXmlCloud = findViewById(R.id.rbCloudXml)
        rfJsonLocal = findViewById(R.id.rbLocalJson)
        rfXmlLocal = findViewById(R.id.rbLocalXml)
        rfxmlUsb = findViewById(R.id.rbUsbXml)
        rfJsonUsb = findViewById(R.id.rbUsbJson)


        layoutDeepLink = findViewById(R.id.layoutDeepLink)
        layoutCloud = findViewById(R.id.layoutCloud)
        layoutUsb = findViewById(R.id.layoutUsb)
        layoutLocal = findViewById(R.id.layoutLocal)

        edtDeepLinkTPN = findViewById(R.id.edtDeepLinkTPN)

        edtPrimaryColor = findViewById(R.id.edt_primary_color)
        edtSecondaryColor = findViewById(R.id.edt_secondary_color)
        edtNegativeColor = findViewById(R.id.edt_negative_color)
        edtFont = findViewById(R.id.edt_font_family)

        rgAvs = findViewById(R.id.rgavs)
        rbAvsYes = findViewById(R.id.rbyes)
        rbAvsNo = findViewById(R.id.rbno)
        rgLoaderLogo = findViewById(R.id.rglogo)
        rbLogoYes = findViewById(R.id.rblogoyes)
        rbLogoNo = findViewById(R.id.rblogono)

        edtCloudRegisterId = findViewById(R.id.edtCloudRegisterid)
        edtCloudAuthKey = findViewById(R.id.edtCloudAuthKey)
        edtLocalIpAddress = findViewById(R.id.edtLocalIpAddress)
        edtLocalRegisterId = findViewById(R.id.edtLocalRegisterId)
        edtUsbRegisterId = findViewById(R.id.edtUsbRegister)
        edtUsbTPNNumber = findViewById(R.id.edtUsbTpn)
        tvUsbStatus = findViewById(R.id.tvUsbStatus)
        tvDeviceStatus = findViewById(R.id.tvDeviceStatus)

        cbBulkTest = findViewById(R.id.cbBulkTest)
        edtBulkCount = findViewById(R.id.edtBulkCount)

        btnConfirm = findViewById(R.id.btnConfirm)
        ivBack = findViewById(R.id.iv_back)

        edtPrimaryColor.setText("#03C880")
        edtSecondaryColor.setText("#616161")
        edtNegativeColor.setText("#BD0F17")
    }

    private fun initUI() {
        val savedMode = PrefsHelper.getMode(this)

        when (savedMode) {

            Mode.DEEPLINK.name -> {
                rbDeepLink.isChecked = true
                selectedMode = Mode.DEEPLINK
                edtDeepLinkTPN.setText(PrefsHelper.getTpn(this))
                edtPrimaryColor.setText(PrefsHelper.getPrimaryColor(this))
                edtSecondaryColor.setText(PrefsHelper.getSecondaryColor(this))
                edtNegativeColor.setText(PrefsHelper.getNegativeColor(this))
                edtFont.setText(PrefsHelper.getFont(this))

                val savedAvs = PrefsHelper.getSelectedAvs(this)
                when (savedAvs) {
                    SelectionOption.YES.name -> {
                        rbAvsYes.isChecked = true
                        rbAvsNo.isChecked = false
                    }
                    SelectionOption.NO.name -> {
                        rbAvsYes.isChecked = false
                        rbAvsNo.isChecked = true
                    }
                    else -> {
                        rbAvsYes.isChecked = true
                        rbAvsNo.isChecked = false
                    }
                }

                val savedLogo = PrefsHelper.getSelectedLogo(this)
                when (savedLogo) {
                    SelectionOption.YES.name -> {
                        rbLogoYes.isChecked = true
                        rbLogoNo.isChecked = false
                    }
                    SelectionOption.NO.name -> {
                        rbLogoYes.isChecked = false
                        rbLogoNo.isChecked = true
                    }
                    else -> {
                        rbLogoYes.isChecked = true
                        rbLogoNo.isChecked = false
                    }
                }

                showDeepLink()

            }

            Mode.CLOUD.name -> {
                rbCloud.isChecked = true
                selectedMode = Mode.CLOUD
                edtCloudRegisterId.setText(PrefsHelper.getRegisterId(this))
                edtCloudAuthKey.setText(PrefsHelper.getAuthId(this))
                restoreRequestFormat()
                showCloud()

            }

            Mode.USB.name -> {
                rbUsb.isChecked = true
                selectedMode = Mode.USB
                edtUsbRegisterId.setText(PrefsHelper.getRegisterId(this))
                edtUsbTPNNumber.setText(PrefsHelper.getTPNNumber(this))
                restoreRequestFormat()
                showUsb()

            }

            Mode.LOCAL.name -> {
                rbLocal.isChecked = true
                selectedMode = Mode.LOCAL
                edtLocalRegisterId.setText(PrefsHelper.getRegisterId(this))
                edtLocalIpAddress.setText(PrefsHelper.getIpAddress(this))
                restoreRequestFormat()
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
                    cbBulkTest.visibility = View.GONE
                    showDeepLink()

                }

                R.id.rbCloud -> {
                    LoggerManager.log(this, "Select Cloud Mode")
                    selectedMode = Mode.CLOUD
                    edtCloudRegisterId.setText(PrefsHelper.getRegisterId(this))
                    edtCloudAuthKey.setText(PrefsHelper.getAuthId(this))
                    cbBulkTest.visibility = View.GONE
                    showCloud()

                }

                R.id.rbLocal -> {
                    LoggerManager.log(this, "Select Local Mode")
                    selectedMode = Mode.LOCAL
                    edtLocalRegisterId.setText("")
                    edtLocalIpAddress.setText("")
                    cbBulkTest.visibility = View.GONE
                    showLocal()

                }

                R.id.rbUsb -> {
                    LoggerManager.log(this, "Select USB Mode")
                    selectedMode = Mode.USB
                    edtUsbRegisterId.setText(PrefsHelper.getRegisterId(this))
                    edtUsbTPNNumber.setText(PrefsHelper.getTPNNumber(this))
                    showUsb()
                    setupUsb()

                    //  Show bulk option only for USB
                    cbBulkTest.visibility = View.VISIBLE

                    // Restore saved values
                    val isBulk = PrefsHelper.isUsbBulkEnabled(this)
                    val count = PrefsHelper.getUsbBulkCount(this)

                    cbBulkTest.isChecked = isBulk

                    if (isBulk) {
                        edtBulkCount.visibility = View.VISIBLE
                        edtBulkCount.setText(count.toString())
                    } else {
                        edtBulkCount.visibility = View.GONE
                    }

                }
            }
        }

        rgAvs.setOnCheckedChangeListener { _, checkedId ->

            when (checkedId) {

                R.id.rbyes -> {
                    Log.d("Deeplink", "Select Avs Yes")
                    LoggerManager.log(this, "Select Avs Yes")
                    selectedAvs = SelectionOption.YES
                }

                R.id.rbno -> {
                    Log.d("Deeplink", "Select Avs No")
                    LoggerManager.log(this, "Select Avs No")
                    selectedAvs = SelectionOption.NO

                }
            }
        }

        rgLoaderLogo.setOnCheckedChangeListener { _, checkedId ->

            when (checkedId) {

                R.id.rblogoyes -> {
                    Log.d("Deeplink", "Select Logo Yes")
                    LoggerManager.log(this, "Select Logo yes")
                    selectedLogo = SelectionOption.YES

                }

                R.id.rblogono -> {
                    Log.d("Deeplink", "Select Logo No")
                    LoggerManager.log(this, "Select Logo no")
                    selectedLogo = SelectionOption.NO

                }
            }
        }

        btnConfirm.setOnClickListener {
            Log.d("Deeplink", "Select btnConfirm")
            if (validateInputs()) {
                handleConfirm()
            }
        }
        cbBulkTest.setOnCheckedChangeListener { _, isChecked ->
            edtBulkCount.visibility = if (isChecked) View.VISIBLE else View.GONE
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
                val tpnNumber = edtUsbTPNNumber.text.toString().trim()
                if ((usbRegisterId.isEmpty() && tpnNumber.isEmpty())) {
                    LoggerManager.log(this, "Enter Register Id or Tpn Number")
                    edtUsbRegisterId.error = "Enter Register Id or Tpn Number"
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
        Log.d("Deeplink", "Select handleConfirm")

        when (selectedMode) {

            Mode.DEEPLINK -> {
                val tpn = edtDeepLinkTPN.text.toString().trim()
                val primaryColor = edtPrimaryColor.text.toString().trim()
                val secondaryColor = edtSecondaryColor.text.toString().trim()
                val negativeColor = edtNegativeColor.text.toString().trim()
                val font = edtFont.text.toString().trim()

                Log.d("Deeplink", "selectedAvs.name----"+selectedAvs.name)
                Log.d("Deeplink", "selectedLogo.name----"+selectedLogo.name)

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
                PrefsHelper.saveDeepLink(this, tpn,primaryColor,secondaryColor,negativeColor,selectedAvs.name,selectedLogo.name, font)
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
                val format = getSelectedRequestFormat()
                PrefsHelper.saveRequestFormat(this, format)
                Toast.makeText(this, "Cloud Selected\nregisterId: $registerId\nauthKey: $authKey", Toast.LENGTH_SHORT).show()
                LoggerManager.log(this, "Confirm Cloud mode Register RegisterId: $registerId AuthKey: $authKey")
                finish()
            }

            Mode.LOCAL -> {
                val ipAddress = edtLocalIpAddress.text.toString().trim()
                val registerId = edtLocalRegisterId.text.toString().trim()
                PrefsHelper.saveLocal(this, registerId, ipAddress)
                PrefsHelper.saveMode(this, Mode.LOCAL.name)
                val format = getSelectedRequestFormat()
                PrefsHelper.saveRequestFormat(this, format)
                Toast.makeText(this, "Local Selected\nRegister Id: $registerId\nIp Address: $ipAddress", Toast.LENGTH_SHORT).show()
                LoggerManager.log(this, "confirm Local mode Register RegisterId: $registerId IpAddress: $ipAddress")
                finish()
            }

            Mode.USB -> {

                if (!usbPosManager.isConnected()) {
                    tvDeviceStatus.text = "POS Device Not connected"
                    Toast.makeText(this, "Please Connect POS USB first", Toast.LENGTH_SHORT).show()
                    return
                }

                val usbRegisterId = edtUsbRegisterId.text.toString().trim()
                val tpnNumber = edtUsbTPNNumber.text.toString().trim()

                if (!(usbRegisterId.isEmpty() || tpnNumber.isEmpty())) {
                    edtUsbRegisterId.error = "Enter Register ID or Tpn Number"
                    return
                }

                PrefsHelper.saveUsb(this, usbRegisterId, tpnNumber)
                PrefsHelper.saveMode(this, Mode.USB.name)

                val format = getSelectedRequestFormat()
                PrefsHelper.saveRequestFormat(this, format)

                // ✅ BULK SAVE ONLY (NO EXECUTION)
                val isBulk = cbBulkTest.isChecked

                var count = 1

                if (isBulk) {
                    val countText = edtBulkCount.text.toString().trim()

                    if (countText.isEmpty()) {
                        edtBulkCount.error = "Enter transaction count"
                        return
                    }

                    count = try {
                        countText.toInt()
                    } catch (e: Exception) {
                        edtBulkCount.error = "Invalid number"
                        return
                    }

                    if (count <= 0) {
                        edtBulkCount.error = "Invalid count"
                        return
                    }

                    // Safety limit
                    if (count > 1000) count = 1000
                }

                PrefsHelper.saveUsbBulkConfig(this, isBulk, count)

                Toast.makeText(
                    this,
                    "USB Settings Saved\nBulk: $isBulk Count: $count",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
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
        if (usbPosManager.isConnected()) {
            tvUsbStatus.text = "POS Connected"
            tvUsbStatus.setTextColor(Color.GREEN)
        } else {
            tvUsbStatus.text = "POS Disconnected"
            tvUsbStatus.setTextColor(Color.RED)
        }
        setupUsb()
    }

    private fun getSelectedRequestFormat(): RequestFormat {

        return when (selectedMode) {

            Mode.CLOUD -> {
                if (rfJsonCloud.isChecked) RequestFormat.JSON else RequestFormat.XML
            }

            Mode.LOCAL -> {
                if (rfJsonLocal.isChecked) RequestFormat.JSON else RequestFormat.XML
            }

            Mode.USB -> {
                if (rfJsonUsb.isChecked) RequestFormat.JSON else RequestFormat.XML
            }

            else -> RequestFormat.XML
        }
    }

    private fun restoreRequestFormat() {

        val savedFormat = PrefsHelper.getRequestFormat(this)

        when (selectedMode) {

            Mode.CLOUD -> {
                if (savedFormat == RequestFormat.JSON) {
                    rfJsonCloud.isChecked = true
                } else {
                    rfXmlCloud.isChecked = true
                }
            }

            Mode.LOCAL -> {
                if (savedFormat == RequestFormat.JSON) {
                    rfJsonLocal.isChecked = true
                } else {
                    rfXmlLocal.isChecked = true
                }
            }

            Mode.USB -> {
                if (savedFormat == RequestFormat.JSON) {
                    rfJsonUsb.isChecked = true
                } else {
                    rfxmlUsb.isChecked = true
                }
            }

            else -> {}
        }
    }

}

