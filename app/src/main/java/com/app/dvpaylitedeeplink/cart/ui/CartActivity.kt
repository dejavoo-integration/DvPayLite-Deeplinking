package com.app.dvpaylitedeeplink.cart.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dvpaylitedeeplink.BuildConfig
import com.app.dvpaylitedeeplink.JsonPreviewActivity
import com.app.dvpaylitedeeplink.MainActivity
import com.app.dvpaylitedeeplink.MyApp
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.UsbPosCallback
import com.app.dvpaylitedeeplink.Utils
import com.app.dvpaylitedeeplink.cart.PrefsHelper
import com.app.dvpaylitedeeplink.cart.adapters.CartAdapter
import com.app.dvpaylitedeeplink.cart.interfaces.TypeSelectionInterface
import com.app.dvpaylitedeeplink.cart.models.Item
import com.app.dvpaylitedeeplink.cart.models.LoadItems
import com.app.dvpaylitedeeplink.dialogs.TxnCompletePopUp
import com.app.dvpaylitedeeplink.logger.LoggerManager
import com.app.dvpaylitedeeplink.usb.UsbPosManager
import com.denovo.app.invokeiposgo.interfaces.SettlementListener
import com.denovo.app.invokeiposgo.interfaces.TransactionListener
import com.denovo.app.invokeiposgo.launcher.IntentApplication
import com.google.android.material.navigation.NavigationView
import com.hoho.android.usbserial.driver.UsbSerialPort
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit


class CartActivity : AppCompatActivity() {

    companion object {
        private const val EXTERNAL_RRN_PREFIX = "DL"
        private const val DEFAULT_VALUE = 0.00
    }

    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var lineItemCheckBox: CheckBox
    private lateinit var transactionTypesRecyclerView: RecyclerView
    private lateinit var amountsContainer: LinearLayout
    private lateinit var checkoutButton: Button
    private lateinit var cancelButton: Button
    private lateinit var imageViewMore: ImageView
    private lateinit var cardViewAmountSection: CardView
    private lateinit var referenceIDLinear: LinearLayout
    private lateinit var referenceIDEditText: AppCompatEditText
    private lateinit var amountEditText: AppCompatEditText
    private lateinit var proceedButton: AppCompatButton
    private lateinit var externalRRNLinear: LinearLayout
    private var externalRRN: String? = null
    private var ticketAmt: Double = 0.00
    private var selectedTransactionType: String = LoadItems.SALE
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var ivHamburger: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private val cart = LoadItems().cart
    private var showApproval = false
    private var showBreakup = false
    private var showDual = false
    private var showTipScreen = false
    private var enableLineItems = false
    private var enableL2L3Items = false
    private var showJsonPreview = false
    private var spinRequest = false
    private var txnTotalAmount: Double =0.0
    private var customerTip: Double = 0.00
    private lateinit var usbManager: UsbManager
    private var serialPort: UsbSerialPort? = null
    private  var usbPosManager: UsbPosManager ? = null
    private lateinit var transactionMode: String
    private lateinit var registerId: String
    private lateinit var authKey: String
    private lateinit var ipAddress: String
    var ticketAmount = 0.00


    private lateinit var intentApplication: IntentApplication
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        LoggerManager.log(this, " Open CartActivity")
        activity = this
        context = this as Context

        usbPosManager = (application as MyApp).usbPosManager
        usbPosManager?.init()

        transactionTypesRecyclerView = findViewById(R.id.transactionTypesRecyclerView)
        lineItemCheckBox = findViewById(R.id.ac_lineItemCheckBox)
        itemsRecyclerView = findViewById(R.id.itemsRecyclerView)
        amountsContainer = findViewById(R.id.amountsContainer)
        checkoutButton = findViewById(R.id.checkoutButton)
        cancelButton = findViewById(R.id.cancelButton)
        imageViewMore = findViewById(R.id.imageViewMore)
        cardViewAmountSection = findViewById(R.id.cardViewAmountSection)
        referenceIDLinear = findViewById(R.id.ac_referenceIDLinear)
        referenceIDEditText = findViewById(R.id.ac_referenceIDEditText)
        amountEditText = findViewById(R.id.ac_amountEditText)
        proceedButton = findViewById(R.id.ac_proceedButton)
        externalRRNLinear = findViewById(R.id.ac_externalRRNLinear)

        //For drawer
        drawerLayout = findViewById(R.id.drawer_layout)


        ivHamburger = findViewById(R.id.iv_hamburger)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        // Optional: disable default title if you have a custom one in layout
        supportActionBar?.setDisplayShowTitleEnabled(false)



        ivHamburger.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        navigationView = findViewById(R.id.navigation_view)
        updateMenuVisibility()
        navigationView.getHeaderView(0)
            .findViewById<TextView>(R.id.tv_version)
            .text = "Version: ${BuildConfig.VERSION_NAME}"
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_registration -> {
                    LoggerManager.log(this, "Select Registration")
                    val intent = Intent(this, RegistrationActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                }
                R.id.nav_configure -> {
                    LoggerManager.log(this, "Select Configure")
                    val intent = Intent(this, OptionSelectionActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                }
                R.id.nav_sale -> {
                    LoggerManager.log(this, "Select Sale")
                    selectedTransactionType =LoadItems.SALE
                    hideSoftKeyboard()
                    showProductsListLayout()
                }
                R.id.nav_refund -> {
                    LoggerManager.log(this, "Select Refund")
                    selectedTransactionType =LoadItems.REFUND
                    hideSoftKeyboard()
                    showProductsListLayout()
                }
                R.id.nav_preAuth -> {
                    LoggerManager.log(this, "Select Preauth")
                    selectedTransactionType =LoadItems.PRE_AUTH
                    hideSoftKeyboard()
                    showProductsListLayout()
                }
                R.id.nav_void -> {
                    LoggerManager.log(this, "Select Void")
                    selectedTransactionType =LoadItems.VOID
                    if (externalRRN != null) {
                        referenceIDEditText.setText(externalRRN)
                    }
                    showReferenceIDLayout(true)
                }
                R.id.nav_ticket -> {
                    LoggerManager.log(this, "Select Ticket")
                    selectedTransactionType =LoadItems.TICKET
                        if (externalRRN != null) {
                        referenceIDEditText.setText(externalRRN)
                            amountEditText.setText(ticketAmt?.toDouble().toString())
                    }
                    showReferenceIDLayout(true)
                }
                R.id.nav_settlement -> {
                    LoggerManager.log(this, "Select Settlement")
                    selectedTransactionType = LoadItems.SETTLEMENT
                    showReferenceIDLayout(false)
                }

                R.id.nav_statusCheck -> {
                    LoggerManager.log(this, "Select Status Check")
                    selectedTransactionType = "STATUS"
                    if (externalRRN != null) {
                        referenceIDEditText.setText(externalRRN)
                    }
                    showReferenceIDLayout(true)
                }
                R.id.nav_peripheral -> {
                    LoggerManager.log(this, "Select Peripheral")
                    val intent = Intent(this, PeripheralActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        LoadItems().loadTransactionTypes(
            context,
            transactionTypesRecyclerView,
            object : TypeSelectionInterface {
                override fun onSelected(type: String) {
                    selectedTransactionType = type
                    Log.e("CartActivity", "transactionType--$type")
                    when (type) {
                        LoadItems.SALE,
                        LoadItems.REFUND,
                        LoadItems.PRE_AUTH -> {
                            hideSoftKeyboard()
                            showProductsListLayout()
                        }

                        LoadItems.VOID,
                        LoadItems.TICKET -> {
                            if (externalRRN!=null){
                                referenceIDEditText.setText(externalRRN)
                                amountEditText.setText(ticketAmt?.toDouble().toString())
                            }
                            showReferenceIDLayout(true)
                        }
                        LoadItems.SETTLEMENT -> {
                            showReferenceIDLayout(false)
                        }

                        else -> {
                            showReferenceIDLayout(true)
                        }
                    }
                }
            })

        intentApplication = IntentApplication(context)
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                intentApplication.handleResultCallBack(result)
            }


        if (Build.MODEL == "P18") {
            itemsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        } else {
            itemsRecyclerView.layoutManager = GridLayoutManager(this, 1)
        }
        itemsRecyclerView.adapter = CartAdapter(this, cart.items,
            { amount, count, position ->
                // Handle item click logic here
                Log.d(
                    "CartActivity",
                    "Item at position $position clicked: Amount = $amount, Count = $count"
                )
                LoggerManager.log(this@CartActivity, "Item at position $position clicked: Amount = $amount, Count = $count")
            },
            { totalAmount ->
                // Update cart total dynamically
                updateTotalAmount(totalAmount)
            }
        )

        updateAmounts()

        proceedButton.setOnClickListener{
            hideSoftKeyboard()
            showProductsListLayout()
            var spinXml = ""
            when (selectedTransactionType) {
                LoadItems.VOID,
                LoadItems.TICKET -> {
                    var ticketAmount = 0.0
                    val refIdFromEditText = referenceIDEditText.text.toString()
                    if (refIdFromEditText.isNotEmpty()) {
                        val adapter = itemsRecyclerView.adapter as CartAdapter
                        val selectedItems = adapter.getSelectedItems()
                        val externalRRN = EXTERNAL_RRN_PREFIX+refIdFromEditText
                        if(selectedTransactionType.equals(LoadItems.TICKET)){
                             ticketAmount = amountEditText.text.toString().toDouble()
                        }
                        val jsonRequest = getPayloadJSON(externalRRN,DEFAULT_VALUE,selectedItems,ticketAmount)
                        LoggerManager.log(this, "Clicked Proceed Button ${selectedTransactionType}")
                        if(transactionMode == "USB"){
                             spinXml = getPayloadSpinXML(externalRRN,DEFAULT_VALUE,selectedItems,ticketAmount)
                            usbRequest(spinXml)
                        }else if(transactionMode == "CLOUD"){
                            spinXml = getPayloadSpinXML(externalRRN,DEFAULT_VALUE,selectedItems,ticketAmount)
                            cloudRequest(spinXml)
                        }else if(transactionMode == "LOCAL"){
                            spinXml = getPayloadSpinXML(externalRRN,DEFAULT_VALUE,selectedItems,ticketAmount)
                            localRequest(spinXml)
                        }else{
                            processSaleTxn(intentApplication, activityResultLauncher, jsonRequest)
                        }
                    }else{
                        LoggerManager.log(this, "Enter External RRN")
                        Toast.makeText(this, "Please enter External RRN", Toast.LENGTH_SHORT).show()
                    }
                }
                LoadItems.SETTLEMENT ->{

                    if(transactionMode == "USB"){
                        spinXml = getPayloadSpinXML("",DEFAULT_VALUE,emptyList(),ticketAmount)
                        usbRequest(spinXml)
                    }else if(transactionMode == "CLOUD"){
                        spinXml = getPayloadSpinXML("",DEFAULT_VALUE,emptyList(),ticketAmount)
                        cloudRequest(spinXml)
                    }else if(transactionMode == "LOCAL"){
                        spinXml = getPayloadSpinXML("",DEFAULT_VALUE,emptyList(),ticketAmount)
                        localRequest(spinXml)
                    }else{
                        processSettlement(intentApplication, activityResultLauncher)
                    }
                }
               "STATUS" ->{
                   val refIdFromEditText = referenceIDEditText.text.toString()
                   val externalRRN = EXTERNAL_RRN_PREFIX+refIdFromEditText
                   if(transactionMode == "USB"){
                       spinXml = getPayloadSpinXML(externalRRN,DEFAULT_VALUE,emptyList(),ticketAmount)
                       usbRequest(spinXml)
                   }else if(transactionMode == "CLOUD"){
                       spinXml = getPayloadSpinXML(externalRRN,DEFAULT_VALUE,emptyList(),ticketAmount)
                       cloudRequest(spinXml)
                   }else if(transactionMode == "LOCAL"){
                       spinXml = getPayloadSpinXML(externalRRN,DEFAULT_VALUE,emptyList(),ticketAmount)
                       localRequest(spinXml)
                   }
                }
            }
        }


        checkoutButton.setOnClickListener {
            LoggerManager.log(this, "Clicked Checkout Button")
            val adapter = itemsRecyclerView.adapter as? CartAdapter
            val selectedItems = adapter?.getSelectedItems().orEmpty()

            if (selectedItems.isEmpty()) {
                Log.e("CartActivity", "No items selected!")
                Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedItemNames = selectedItems.map { it.name }
            Log.e("CartActivity", "Selected Item Names: $selectedItemNames")

            txnTotalAmount = selectedItems.sumOf { it.price * it.quantity }

            Log.e("CartActivity", "txnTotalAmount before open user selection:::$txnTotalAmount")

            val intent = Intent(this, TipAndFeeActivity::class.java).apply {
                LoggerManager.log(this@CartActivity, "Move to TipAndFee Activity Screen")
                putExtra("txnAmount", txnTotalAmount)
            }
            startActivityForResult(intent, 123)
        }
        cancelButton.setOnClickListener {
            LoggerManager.log(this@CartActivity, "Clear Cart")
            clearCart()
        }


        imageViewMore.setOnClickListener {
            LoggerManager.log(this@CartActivity, "Move to Old MainActivity")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getUserConfig()
        updateMenuVisibility()
        Log.i("CartActivity",
            "Show Approval Screen: $showApproval------ Show Breakup Screen: $showBreakup---- Show Dual Screen: $showDual----- Enable Line Items $enableLineItems----- Show Tip Screen: $showTipScreen----- Enable l2l3 Items $enableL2L3Items----show Json Preview $showJsonPreview")
    }

    private fun getUserConfig() {
        showApproval = PrefsHelper.getApproval(this)
        showBreakup = PrefsHelper.getBreakup(this)
        showDual = PrefsHelper.getDual(this)
        showTipScreen = PrefsHelper.getTipScreenStatus(this)
        enableLineItems = PrefsHelper.getLineItems(this)
        enableL2L3Items = PrefsHelper.getL2L3LineItems(this)
        showJsonPreview =PrefsHelper.getJsonPreviewStatus(this)
        transactionMode = PrefsHelper.getMode(this).toString()
        registerId = PrefsHelper.getRegisterId(this).toString()
        authKey = PrefsHelper.getAuthId(this).toString()
        ipAddress = PrefsHelper.getIpAddress(this).toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("CartActivity", "onActivityResult called with requestCode: $requestCode, resultCode: $resultCode")

        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            Log.d("CartActivity", "Request code matched and result OK")

            getUserConfig()
            Log.d("CartActivity", "User config loaded")
            Log.d("CartActivity", "context--$context")


            if (data != null) {
                customerTip = data.getDoubleExtra("tip",0.00)!!
                Log.d("CartActivity", "Customer tip received: $customerTip")
                updateTotalAmount(txnTotalAmount)
            } else {
                Log.w("CartActivity", "Intent data is null; no tip received")
            }

            val adapter = itemsRecyclerView.adapter as CartAdapter
            val selectedItems = adapter.getSelectedItems()
            Log.d("CartActivity", "Selected items count: ${selectedItems.size}")

            if (selectedItems.isEmpty()) {
                Log.e("CartActivity", "No items selected in onActivityResult!")
                return
            }

            val totalAmount = selectedItems.sumOf { it.price * it.quantity }
            Log.d("CartActivity", "Calculated total amount: $totalAmount")

            externalRRN = Utils.generateRandom(12).toString()
            ticketAmt = totalAmount
            Log.d("CartActivity", "Generated external RRN: $externalRRN")
            var jsonRequest: JSONObject = JSONObject()
            var spinRequest:String = ""
            Log.d("CartActivity", "registration transactionMode  : $transactionMode")
            if(transactionMode == "USB"){
               spinRequest = getPayloadSpinXML(EXTERNAL_RRN_PREFIX + externalRRN, totalAmount,selectedItems,ticketAmt)
                usbRequest(spinRequest)
            }else if(transactionMode == "CLOUD"){
                spinRequest = getPayloadSpinXML(EXTERNAL_RRN_PREFIX + externalRRN, totalAmount,selectedItems,ticketAmt)
                cloudRequest(spinRequest)
            }else if(transactionMode == "LOCAL"){
                spinRequest = getPayloadSpinXML(EXTERNAL_RRN_PREFIX + externalRRN, totalAmount,selectedItems,ticketAmt)
                localRequest(spinRequest)
            }else{
                jsonRequest = getPayloadJSON(EXTERNAL_RRN_PREFIX + externalRRN, totalAmount,selectedItems,ticketAmt)
                Log.d("CartActivity", "Initialized JSON payload")

                val cartObject = JSONObject()

                val itemsArray = JSONArray(selectedItems.map { item ->
                    JSONObject().apply {
                        put("Name", item.name)
                        put("Price", formatToTwoDecimalPlaces(item.price))
                        put("CardPrice", formatToTwoDecimalPlaces(item.price * 1.04))
                        put("Quantity", item.quantity)
                        put("AdditionalInfo", item.additionalInfo)

                        if (!item.modifiers.isNullOrEmpty()) {
                            val modifiersArray = JSONArray(item.modifiers!!.map { modifier ->
                                JSONObject().apply {
                                    put("Name", modifier.name)
                                    put("Options", JSONArray(modifier.options?.map { option ->
                                        JSONObject().apply {
                                            put("Name", option.name)
                                            put("Price", formatToTwoDecimalPlaces(option.price))
                                            put("Quantity", option.quantity)
                                        }
                                    }))
                                }
                            })
                            put("Modifiers", modifiersArray)
                        }
                    }
                })
                Log.d("CartActivity", "Items array created with ${selectedItems.size} items")

                val cardAmountsArray = JSONArray(cart.amounts.map { amount ->
                    val cardPrice = if (amount.name.equals("Tip", ignoreCase = true)) {
                        amount.value
                    } else if (amount.name.equals("Total", ignoreCase = true)) {
                        val fee = (4.0 / 100) * amount.value
                        amount.value + fee + customerTip
                    } else {
                        val fee = (4.0 / 100) * amount.value
                        amount.value + fee
                    }

                    JSONObject().apply {
                        put("Name", amount.name)
                        put("Value", formatToTwoDecimalPlaces(cardPrice))
                    }
                })
                val cashAmountsArray = JSONArray(cart.amounts.map { amount ->
                    val cashPrice = if (amount.name.equals("Total", ignoreCase = true)) {
                        amount.value+customerTip
                    }else{
                        amount.value
                    }
                    JSONObject().apply {
                        put("Name", amount.name)
                        put("Value", formatToTwoDecimalPlaces(cashPrice))
                    }
                })
                Log.d("CartActivity", "CashPrices array created with ${cart.amounts.size} entries")

                cartObject.put("Items", itemsArray)
                cartObject.put("Amounts", cardAmountsArray)
                cartObject.put("CashPrices", cashAmountsArray)

                if (enableLineItems) {
                    jsonRequest.put("Cart", cartObject)
                    Log.d("CartActivity", "Line items enabled; cart object added to payload")
                }
                Log.d("CartActivity", "Final JSON Object: ${spinRequest}")
                Log.d("CartActivity", "Processing sale transaction...")
                val editedJsonString = data?.getStringExtra("editedJson")
                if (!editedJsonString.isNullOrEmpty()) {
                    val finalJson = JSONObject(editedJsonString)
                    Log.d("CartActivity", "Confirmed JSON: $finalJson")
                    processSaleTxn(intentApplication, activityResultLauncher, finalJson)
                }
                if(showJsonPreview){
                    val intent = Intent(this, JsonPreviewActivity::class.java)
                    intent.putExtra("jsonPayload", jsonRequest.toString(2))
                    startActivityForResult(intent, 456)
                }else{
                    processSaleTxn(intentApplication, activityResultLauncher, jsonRequest)
                }
                customerTip = 0.00
            }
            if (requestCode == 456 && resultCode == Activity.RESULT_OK) {
                val editedJsonString = data?.getStringExtra("editedJson")
                if (!editedJsonString.isNullOrEmpty()) {
                    val finalJson = JSONObject(editedJsonString)
                    Log.d("CartActivity", "Confirmed JSON: $finalJson")
                    processSaleTxn(intentApplication, activityResultLauncher, finalJson)
                }
            } else {
                Log.d("CartActivity", "Request code or result code did not match expected values")
            }
            }
    }


    private fun processSaleTxn(
        intentApplication: IntentApplication,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        jsonRequest: JSONObject
    ) {
        LoggerManager
        intentApplication.setTransactionListener(object :
            TransactionListener {
            override fun onApplicationLaunched(result: JSONObject?) {
            }

            override fun onApplicationLaunchFailed(errorResult: JSONObject) {
                clearCart()
                val errorMessage = errorResult.get("error_message")
                val txnCompletePopUp = TxnCompletePopUp(activity)
                txnCompletePopUp.showPopUpDialog(false, LoadItems.TRANSACTION,errorMessage.toString())
            }

            override fun onTransactionSuccess(transactionResult: JSONObject?) {
                Log.e(
                    "CartActivity",
                    "transactionResult.toString() - ${transactionResult.toString()}"
                )
                clearCart()
                val txnCompletePopUp = TxnCompletePopUp(activity)
                txnCompletePopUp.transactionResult = transactionResult
                txnCompletePopUp.showPopUpDialog(true, LoadItems.TRANSACTION,"")
            }

            override fun onTransactionFailed(errorResult: JSONObject) {
                Log.e("CartActivity", "errorResult.toString() - ${errorResult.toString()}")
                clearCart()
                val errorMessage = errorResult.get("error_message")
                val txnCompletePopUp = TxnCompletePopUp(activity)
                txnCompletePopUp.showPopUpDialog(false,LoadItems.TRANSACTION, errorMessage.toString())
            }
        })
        intentApplication.performTransaction(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun processSettlement(intentApplication: IntentApplication, activityResultLauncher: ActivityResultLauncher<Intent>) {
        val jsonRequest = JSONObject()
        jsonRequest.put("type", "SETTLE")
        jsonRequest.put("applicationType", "DVPAYLITE")

        intentApplication.setSettlementListener(object :
            SettlementListener {

            override fun onSettlementSuccess(p0: JSONObject?) {
                Log.e("DVPAYLITE", "onSettlementSuccess-- ${p0.toString()}")
                val txnCompletePopUp = TxnCompletePopUp(activity)
                txnCompletePopUp.showPopUpDialog(true, LoadItems.SETTLEMENT,"")
            }

            override fun onSettlementFailed(p0: JSONObject?) {
                val txnCompletePopUp = TxnCompletePopUp(activity)
                val errorMessage = p0?.optString("error_message")
                txnCompletePopUp.showPopUpDialog(false, LoadItems.SETTLEMENT,errorMessage.toString())
                Log.e("DVPAYLITE", "onSettlementFailed-- ${p0.toString()}")
            }
        })
        intentApplication.settleBatch(
            jsonRequest,
            activityResultLauncher
        )
    }

    private fun updateTotalAmount(totalAmount: Double) {
        if (totalAmount == 0.0) {
            cardViewAmountSection.visibility = View.GONE
        } else {
            cardViewAmountSection.visibility = View.VISIBLE
        }
        Log.d("CartActivity","pragada TT"+totalAmount.toString())
        Log.d("CartActivity","pragada"+customerTip.toString())
        val formattedTotal = String.format("%.2f", totalAmount).toDouble()
        cart.amounts.find { it.name == "Subtotal" }?.value = (formattedTotal)
        cart.amounts.find { it.name == "Tip" }?.value = customerTip
        cart.amounts.find { it.name == "Total" }?.value = (formattedTotal)
        updateAmounts()
    }

    private fun updateAmounts() {
        amountsContainer.removeAllViews()

        cart.amounts.forEachIndexed { index, amount ->
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)

            }

            val nameEditText = TextView(this).apply {
                text = amount.name + ":"
                hint = "Name"
                setTextColor(ContextCompat.getColor(this@CartActivity, R.color.black))
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                addTextChangedListener { text ->
                    amount.name = text.toString()
                }
                (layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 0, 0)
            }

            val valueEditText = TextView(this).apply {
                text = "$${formatToTwoDecimalPlaces(amount.value)}"
                hint = "Value"
                setTextColor(ContextCompat.getColor(this@CartActivity, R.color.black))
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        setMargins(0, 0, convertDpToPx(10), 0)
                    }
                addTextChangedListener { text ->
                    amount.value = text.toString().toDoubleOrNull() ?: 0.00
                }
            }

            layout.addView(nameEditText)
            layout.addView(valueEditText)
            if (amount.value > 0) {
                amountsContainer.addView(layout)
            }
        }
    }


    private fun convertDpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

    private fun formatToTwoDecimalPlaces(value: Double): String {
        return if (value==DEFAULT_VALUE) "0.00" else (String.format("%.2f", value))
    }

    private fun clearCart() {
        val adapter = itemsRecyclerView.adapter as CartAdapter
        adapter.removeAllItems()
        updateTotalAmount(0.0)
    }

    private fun showProductsListLayout() {
        if (itemsRecyclerView.visibility == View.GONE) {
            itemsRecyclerView.visibility = View.VISIBLE
            referenceIDLinear.visibility = View.GONE
        }
    }

    private fun showReferenceIDLayout(showRRNLinear:Boolean) {
        clearCart()
        referenceIDLinear.visibility = View.VISIBLE
        externalRRNLinear.visibility = if (showRRNLinear) View.VISIBLE else View.GONE
        amountEditText.visibility = if(selectedTransactionType == LoadItems.TICKET)  View.VISIBLE else View.GONE
        itemsRecyclerView.visibility = View.GONE
    }

    private fun getPayloadJSON(referenceId:String,totalAmount:Double, items: List<Item>,ticketAmount:Double):JSONObject{
        val totalAmt = formatToTwoDecimalPlaces(totalAmount)
        txnTotalAmount = totalAmount
        return JSONObject().apply {
            put("type", selectedTransactionType)
            put("paymentType", "Credit")
            if(selectedTransactionType.equals(LoadItems.TICKET)){
                put("amount", ticketAmount)
            }else{
                put("amount", totalAmt)
            }
            put("tip",  String.format("%.2f", customerTip))
            put("applicationType", "DVPAYLITE")
            put("refId", referenceId)
            put("receiptType", "receiptType")
            put("isTxnStatusScreenRequired", if (showApproval) "Yes" else "No")
            put("showBreakupScreen", if (showBreakup) "Yes" else "No")
            put("showDualPriceScreen", if (showDual) "Yes" else "No")
            put("showTipScreen", if (showTipScreen) "Yes" else "No")
            if (enableL2L3Items) {
                val l2l3Data = buildL2L3Data(items)
                for (key in l2l3Data.keys()) {
                    if (key != "Level3LineItems") {
                        put(key, l2l3Data.get(key))
                    }
                }
                put("Level3LineItems", l2l3Data.getJSONObject("Level3LineItems"))
            }
        }
    }

    private fun getPayloadSpinXML(
        referenceId: String,
        totalAmount: Double,
        items: List<Item>,
        ticketAmount: Double
    ): String {
        items.forEach { item ->
            Log.i("my_tag", " cart Item: $item")
        }
        val totalAmt = formatToTwoDecimalPlaces(totalAmount)
        val discounts = 0.00
        val taxes = 0.00
        val xmlBuilder = StringBuilder()
        xmlBuilder.append("<request>")
        if (selectedTransactionType != LoadItems.SETTLEMENT) {
            xmlBuilder.append("<PaymentType>Credit</PaymentType>")
        }
        xmlBuilder.append("<TransType>${getTransactionType(selectedTransactionType)}</TransType>")
        if (selectedTransactionType != LoadItems.SETTLEMENT && selectedTransactionType != "STATUS") {
            if (selectedTransactionType == LoadItems.TICKET) {
                xmlBuilder.append("<Amount>$ticketAmount</Amount>")
            } else {
                    xmlBuilder.append("<Amount>$totalAmt</Amount>")
            }
            xmlBuilder.append("<Tip>${String.format("%.2f", customerTip)}</Tip>")
            xmlBuilder.append("<CashbackAmount>0.00</CashbackAmount>")
            xmlBuilder.append("<Frequency>OneTime</Frequency>")
            xmlBuilder.append("<CustomFee>0.00</CustomFee>")
        }
        xmlBuilder.append("<RefId>${referenceId}</RefId>")

        if (selectedTransactionType == LoadItems.SETTLEMENT) {
            xmlBuilder.append("<Param>Close</Param>")
        }
        xmlBuilder.append("<RegisterId>${registerId}</RegisterId>")
        if (transactionMode == "USB") {
            xmlBuilder.append("<AuthKey>vPXjq5X8fn</AuthKey>")
        } else {
            xmlBuilder.append("<AuthKey>${authKey}</AuthKey>")
        }
        xmlBuilder.append("<PrintReceipt>No</PrintReceipt>")
        xmlBuilder.append("<SigCapture>No</SigCapture>")

        Log.i("my_tag", " settlement enabled  $selectedTransactionType")
        Log.i("my_tag", " settlement enabled  ${selectedTransactionType != LoadItems.SETTLEMENT}")
        Log.i("my_tag", " settlement enabled  ${ selectedTransactionType != "STATUS"}")
        if (selectedTransactionType != LoadItems.SETTLEMENT && selectedTransactionType != "STATUS") {
            // Cart Section
            xmlBuilder.append("<Cart>")
            xmlBuilder.append("<Amounts>")
            xmlBuilder.append("<Amount><Name>Discounts</Name><Value>${(discounts).toInt()}</Value></Amount>")
            xmlBuilder.append("<Amount><Name>Subtotal</Name><Value>${(totalAmount).toInt()}</Value></Amount>")
            xmlBuilder.append("<Amount><Name>Taxes</Name><Value>${(taxes).toInt()}</Value></Amount>")
            xmlBuilder.append("<Amount><Name>Total</Name><Value>${(totalAmount).toInt()}</Value><Total/></Amount>")
            xmlBuilder.append("</Amounts>")
            xmlBuilder.append("<Items>")

            for (item in items) {
                val itemTotalValue = item.price * item.quantity
                xmlBuilder.append("<Item>")
                xmlBuilder.append("<Name>${item.name}</Name>")
                xmlBuilder.append("<Price>${item.price}</Price>")
                xmlBuilder.append("<UnitPrice></UnitPrice>")
                xmlBuilder.append("<Quantity>${item.quantity}</Quantity>")

                // NEW: CustomInfos Block per Item
                xmlBuilder.append("<CustomInfos>")
                xmlBuilder.append("<CustomInfo><Name>Total</Name><Value>${itemTotalValue}</Value></CustomInfo>")
                xmlBuilder.append("<CustomInfo><Name>Total</Name><Value>${itemTotalValue}</Value></CustomInfo>")
                xmlBuilder.append("</CustomInfos>")
                xmlBuilder.append("<AdditionalInfo>")
                xmlBuilder.append("</AdditionalInfo>")
                xmlBuilder.append("</Item>")
            }
            xmlBuilder.append("</Items>")
            xmlBuilder.append("</Cart>")
        }
        xmlBuilder.append("</request>")

        return xmlBuilder.toString()
    }


    private fun hideSoftKeyboard(){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(referenceIDEditText.windowToken, 0)
    }

    private fun buildL2L3Data(selectedItems: List<Item>): JSONObject {

        var totalDiscountAmt = 0.0
        var dutyAmount = 0.0
        var totalStateTax = 0.0
        var totalLocalTax = 0.0
        var totalAltTaxAmount = 0.0
        var totalTaxAmount = 0.0
        var shippingAmount = 0.0
        var freightAmount = 0.0
        var taxRate = 0.0
        val groupArray = JSONArray().apply {
            selectedItems.forEach { item ->

                val basePrice = item.price
                val quantity = item.quantity
                val itemBaseAmount = basePrice * quantity
                var altTaxAmount = 0.0

                // Example tax values; replace with actual logic if available in your Item
                val discountRate = item.discountRate
                val localTaxRate = item.localTaxRate
                val stateTaxRate = item.stateTaxRate

                    val discountAmt =  itemBaseAmount * 0.5
                val localTaxAmt =  itemBaseAmount * 0.20
                val stateTaxAmt =  itemBaseAmount * 0.10
                val totalTaxAmt = localTaxAmt + stateTaxAmt
                val totalTaxRate = stateTaxRate + localTaxRate
                val itemTotalAmount = itemBaseAmount - discountAmt + totalTaxAmt
                totalTaxAmount += totalTaxAmt
                totalStateTax += stateTaxAmt
                totalLocalTax += localTaxAmt
                totalDiscountAmt += discountAmt
                taxRate = totalTaxRate

                val description = item.name.replace(Regex("[^A-Za-z0-9]"), "")
                put(JSONObject().apply {
                    put("CommodityCode", "10")
                    put("Description", description)
                    put("ProductCode", "2012")
                    put("Quantity", item.quantity.toString())
                    put("UnitOfMeasure", "ITM")
                    put("UnitCost",formatToTwoDecimalPlaces(basePrice))
                    put("TaxRate",formatToTwoDecimalPlaces(localTaxRate))
                    put("TaxAmount", formatToTwoDecimalPlaces(localTaxAmt))
                    put("DiscountAmount", "0.00")
                    put("DiscountRate", "0.00")
                    put("DiscountIndicator", "N")
                    put("NetGrossIndicator", "N")
                    put("DebitCreditIndicator", "D")
                    put("ExtLineAmount",formatToTwoDecimalPlaces(itemTotalAmount))
                    put("AltTaxID", "0")
                    put("TaxTypeApplied", "")
                    put("NationalTaxAmount",formatToTwoDecimalPlaces(stateTaxAmt))
                    put("NationalTaxRate",formatToTwoDecimalPlaces((stateTaxRate)))
                    put("TaxIndicator", "1")
                })
            }
        }
        val level3LineItems = JSONObject().apply {
            put("group", groupArray)
        }
        return JSONObject().apply {
            put("IsvId", "23454")
            put("cardAcceptanceTime", "")
            put("TaxAmount", Utils.removeDouble(totalTaxAmount))
            put("LocalTaxFlag", "1")
            put("NationalTaxAmount", formatToTwoDecimalPlaces(totalStateTax))
//            put("LocalTaxAmount", formatToTwoDecimalPlaces(totalLocalTax))
            put("DestZipCode", "840")
            put("SummaryCommodityCode", "0987")
            put("TaxRateApplied", "")
            put("TotalDiscountAmount", formatToTwoDecimalPlaces(totalDiscountAmt))
            put("PoNumber", Utils.generateRandom(6).toString())
            put("FreightAmount", formatToTwoDecimalPlaces(freightAmount))
            put("DutyAmount", formatToTwoDecimalPlaces(dutyAmount))
            put("ShipfromZipCode", "90")
            put("DestCountryCode", "840")
            put("LineItemCount", selectedItems.size.toString())
            put("AltTaxAmount", "0")
            put("PurchaseIdentifier", Utils.generateRandom(9).toString())
            put("PurchaseIdFormatCode","")
            put("MerchantTaxId", "0")
//            put("VatInvNum", "98989")
//            put("totalLTaxAmount", formatToTwoDecimalPlaces(totalLocalTax))
            put("AltTaxIndicator","")
            put("OrderDate",Utils.getCurrentDateYYMMDD())
            put("Level3LineItems", level3LineItems)
        }
    }



    fun usbRequest(request: String) {

     Log.i("usbrequest","usbRequest Main request : ${request.toString()}")
     Log.i("usbrequest","usbRequest Main request : ${usbPosManager?.isConnected()}")
        val progressDialog = android.app.AlertDialog.Builder(this)
            .setTitle("Please wait")
            .setMessage("Processing transaction...")
            .setCancelable(false) // cannot dismiss by tapping outside
            .create()
        progressDialog.show()
        LoggerManager.log(this@CartActivity, "Processing Dialog open")
        LoggerManager.log(this, "Processing USB Request : ${request}")
        usbPosManager?.sendAndReceive(request, object : UsbPosCallback {

            override fun onResult(response: String?, totalBytes: Int) {
                runOnUiThread {

                    if (progressDialog.isShowing) {
                        LoggerManager.log(this@CartActivity, "Processing Dialog Close")
                        progressDialog.dismiss()
                    }

                    if (response != null) {
                        Log.i("USB", response)
                        LoggerManager.log(this@CartActivity, "USB Result  : ${response}")
                        Toast.makeText(
                            this@CartActivity,
                            response,
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        LoggerManager.log(this@CartActivity, "USB Result  : No POS response")
                        Toast.makeText(this@CartActivity,
                            "No POS response",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun updateMenuVisibility() {
        val menu = navigationView.menu
        val currentMode = PrefsHelper.getMode(this)

        // Show configure only for DeepLink
        menu.findItem(R.id.nav_configure).isVisible = currentMode == "DEEPLINK"
        menu.findItem(R.id.nav_statusCheck).isVisible = currentMode != "DEEPLINK"

        if(PrefsHelper.getMode(context) == "DEEPLINK"){
            imageViewMore.visibility = View.VISIBLE
        }else{
            imageViewMore.visibility = View.GONE
        }

    }

    private fun getTransactionType(txnType:String): String {
        when (txnType) {
            "SALE" -> {
                return "Sale"
            }
            "REFUND" -> {
                return "Return"
            }
            "VOID" -> {
                return "Void"
            }
            "TICKET" -> {
                return "Ticket"
            }
            "TIPADJUST" -> {
                return "TipAdjust"
            }
            "SETTLEMENT" -> {
                return  "Settle"
            }
            "PRE_AUTH" -> {
                return "Auth"
            }
            "STATUS"  -> {
                return "Status"
            }
            else -> return "Sale"
        }
    }

    fun cloudRequest(xmlRequest: String) {

        val progressDialog = android.app.AlertDialog.Builder(this)
            .setTitle("Please wait")
            .setMessage("Processing transaction...")
            .setCancelable(false) // cannot dismiss by tapping outside
            .create()
        progressDialog.show()
        LoggerManager.log(this@CartActivity, "Processing Dialog open")
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("test.spinpos.net")
            .addPathSegments("spin/cgi.html")
            .addQueryParameter("TerminalTransaction", xmlRequest)
            .build()
        Log.d("SPIN_REQUEST", url.toString())
        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("User-Agent", "Mozilla/5.0")
            .addHeader("Connection", "close")
            .build()
        LoggerManager.log(this@CartActivity, "Processing CLOUD Request  : ${request}")
        Thread {
            try {
                val response = client.newCall(request).execute()
                val result = response.body?.string() ?: "No Response"

                Log.d("SPIN_RESPONSE", result)
                LoggerManager.log(this@CartActivity, " CLOUD Response  : ${result}")

                //  Switch to Main Thread to show Toast
                Handler(Looper.getMainLooper()).post {
                    if (progressDialog.isShowing) {
                        LoggerManager.log(this@CartActivity, "Processing Dialog Close")
                        progressDialog.dismiss()
                    }
                    Toast.makeText(
                        this@CartActivity,
                        result,
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Handler(Looper.getMainLooper()).post {
                    if (progressDialog.isShowing) {
                        LoggerManager.log(this@CartActivity, "Processing Dialog Close")
                        progressDialog.dismiss()
                    }
                    Toast.makeText(
                        this@CartActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                LoggerManager.log(this@CartActivity, "Request failed : ")
                Log.e("SPIN_ERROR", "Request failed", e)
            }

        }.start()
    }

    fun localRequest(xmlRequest: String) {
        val progressDialog = android.app.AlertDialog.Builder(this)
            .setTitle("Please wait")
            .setMessage("Processing transaction...")
            .setCancelable(false) // cannot dismiss by tapping outside
            .create()
        progressDialog.show()
        try {
            LoggerManager.log(this@CartActivity, "Processing Dialog open")
            val url = HttpUrl.Builder()
                .scheme("http")
                .host(ipAddress)          // make sure ipAddress is correct
                .port(9000)
                .addPathSegments("spin/cgi.html")
                .addQueryParameter("TerminalTransaction", xmlRequest)
                .build()

            Log.i("LOCAL_URL", url.toString())

            val client = OkHttpClient.Builder()
                .protocols(listOf(Protocol.HTTP_1_1))
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Connection", "close")
                .build()
            LoggerManager.log(this@CartActivity, " Processing Local Request  : ${request}")

            Thread {
                try {

                    val response = client.newCall(request).execute()
                    val result = response.body?.string() ?: "No Response"

                    Log.d("LOCAL_RESPONSE", result)
                    LoggerManager.log(this@CartActivity, "  Local Response  : ${result}")
                    //  Show Toast on Main Thread
                    Handler(Looper.getMainLooper()).post {
                        if (progressDialog.isShowing) {
                            LoggerManager.log(this@CartActivity, "Processing Dialog Close")
                            progressDialog.dismiss()
                        }
                        Toast.makeText(
                            this@CartActivity,   // change if in different activity
                            result,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {
                    if (progressDialog.isShowing) {
                        LoggerManager.log(this@CartActivity, "Processing Dialog Close")
                        progressDialog.dismiss()
                    }

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            this@CartActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    LoggerManager.log(this@CartActivity, "Request failed")
                    Log.e("LOCAL_ERROR", "Request failed", e)
                }
            }.start()

        } catch (e: Exception) {
            if (progressDialog.isShowing) {
                LoggerManager.log(this@CartActivity, "Processing Dialog Close")
                progressDialog.dismiss()
            }
            Log.e("LOCAL_ERROR", "URL build failed", e)
            LoggerManager.log(this@CartActivity, "URL build failed")
        }
    }

}