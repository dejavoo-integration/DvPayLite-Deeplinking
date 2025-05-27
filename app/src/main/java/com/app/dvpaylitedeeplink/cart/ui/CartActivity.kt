package com.app.dvpaylitedeeplink.cart.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import com.app.dvpaylitedeeplink.MainActivity
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.Utils
import com.app.dvpaylitedeeplink.cart.PrefsHelper
import com.app.dvpaylitedeeplink.cart.adapters.CartAdapter
import com.app.dvpaylitedeeplink.cart.interfaces.TypeSelectionInterface
import com.app.dvpaylitedeeplink.cart.models.LoadItems
import com.app.dvpaylitedeeplink.dialogs.TxnCompletePopUp
import com.denovo.app.invokeiposgo.interfaces.SettlementListener
import com.denovo.app.invokeiposgo.interfaces.TransactionListener
import com.denovo.app.invokeiposgo.launcher.IntentApplication
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import org.json.JSONObject

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
    private lateinit var proceedButton: AppCompatButton
    private lateinit var externalRRNLinear: LinearLayout
    private var externalRRN: String? = null
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
    private var txnTotalAmount: Double =0.0
    private var customerTip: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        activity = this
        context = this as Context

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
        navigationView.getHeaderView(0)
            .findViewById<TextView>(R.id.tv_version)
            .text = "Version: ${BuildConfig.VERSION_NAME}"
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_registration -> {
                    val intent = Intent(this, RegistrationActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                }
                R.id.nav_configure -> {
                    val intent = Intent(this, OptionSelectionActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                }
                R.id.nav_sale -> {
                    selectedTransactionType =LoadItems.SALE
                    hideSoftKeyboard()
                    showProductsListLayout()
                }
                R.id.nav_refund -> {
                    selectedTransactionType =LoadItems.REFUND
                    hideSoftKeyboard()
                    showProductsListLayout()
                }
                R.id.nav_preAuth -> {
                    selectedTransactionType =LoadItems.PRE_AUTH
                    hideSoftKeyboard()
                    showProductsListLayout()
                }
                R.id.nav_void -> {
                    selectedTransactionType =LoadItems.VOID
                    if (externalRRN != null) {
                        referenceIDEditText.setText(externalRRN)
                    }
                    showReferenceIDLayout(true)
                }
                R.id.nav_ticket -> {
                    selectedTransactionType =LoadItems.TICKET
                        if (externalRRN != null) {
                        referenceIDEditText.setText(externalRRN)
                    }
                    showReferenceIDLayout(true)
                }
                R.id.nav_settlement -> {
                    selectedTransactionType = LoadItems.SETTLEMENT
                    showReferenceIDLayout(false)
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

        val intentApplication = IntentApplication(applicationContext)
        val activityResultLauncher =
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
            },
            { totalAmount ->
                // Update cart total dynamically
                updateTotalAmount(totalAmount)
            }
        )

        updateAmounts()

        proceedButton.setOnClickListener{
            when (selectedTransactionType) {
                LoadItems.VOID,
                LoadItems.TICKET -> {
                    val refIdFromEditText = referenceIDEditText.text.toString()
                    if (refIdFromEditText.isNotEmpty()) {
                        val externalRRN = EXTERNAL_RRN_PREFIX+refIdFromEditText
                        val jsonRequest = getPayloadJSON(externalRRN,DEFAULT_VALUE)
                        processSaleTxn(intentApplication, activityResultLauncher, jsonRequest)
                    }else{
                        Toast.makeText(this, "Please enter External RRN", Toast.LENGTH_SHORT).show()
                    }
                }
                LoadItems.SETTLEMENT ->{
                    processSettlement(intentApplication, activityResultLauncher)
                }
            }
        }

        checkoutButton.setOnClickListener {

            val adapter = itemsRecyclerView.adapter as? CartAdapter
            val selectedItems = adapter?.getSelectedItems().orEmpty()

            if (selectedItems.isEmpty()) {
                Log.e("CartActivity", "No items selected!")
                Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            txnTotalAmount = selectedItems.sumOf { it.price * it.quantity }

            Log.e("CartActivity", "txnTotalAmount before open user selection:::$txnTotalAmount")

            val intent = Intent(this, TipAndFeeActivity::class.java).apply {
                putExtra("txnAmount", txnTotalAmount)
            }
            startActivityForResult(intent, 123)
        }
        cancelButton.setOnClickListener {
            clearCart()
        }

        imageViewMore.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getUserConfig()
        Log.i("CartActivity",
            "Show Approval Screen: $showApproval------ Show Breakup Screen: $showBreakup---- Show Dual Screen: $showDual----- Enable Line Items $enableLineItems----- Show Tip Screen: $showTipScreen")
    }

    private fun getUserConfig() {
        showApproval = PrefsHelper.getApproval(this)
        showBreakup = PrefsHelper.getBreakup(this)
        showDual = PrefsHelper.getDual(this)
        showTipScreen = PrefsHelper.getTipScreenStatus(this)
        enableLineItems = PrefsHelper.getLineItems(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("CartActivity", "onActivityResult called with requestCode: $requestCode, resultCode: $resultCode")

        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            Log.d("CartActivity", "Request code matched and result OK")

            getUserConfig()
            Log.d("CartActivity", "User config loaded")

            val intentApplication = IntentApplication(applicationContext)
            val activityResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    Log.d("CartActivity", "Nested activity result received")
                    intentApplication.handleResultCallBack(result)
                }

            if (data != null) {
                customerTip = data.getDoubleExtra("tip", 0.0)
                Log.d("CartActivity", "Customer tip received: $customerTip")
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
            Log.d("CartActivity", "Generated external RRN: $externalRRN")

            val jsonRequest = getPayloadJSON(EXTERNAL_RRN_PREFIX + externalRRN, totalAmount)
            Log.d("CartActivity", "Initialized JSON payload")

            val cartObject = JSONObject()

            val itemsArray = JSONArray(selectedItems.map { item ->
                JSONObject().apply {
                    put("Name", item.name)
                    put("Price", formatToTwoDecimalPlaces(item.price))
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
                JSONObject().apply {
                    put("Name", amount.name)
                    put("Value", formatToTwoDecimalPlaces(amount.value))
                }
            })
            val cashAmountsArray = JSONArray(cart.amounts.map { amount ->
                val fee = (4.0 / 100) * (amount.value)
                val cashPrice = ((amount.value) - fee)
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
                Log.d("CartActivity", "Final JSON Object: $jsonRequest")
            }

            Log.d("CartActivity", "Processing sale transaction...")
            processSaleTxn(intentApplication, activityResultLauncher, jsonRequest)
        } else {
            Log.d("CartActivity", "Request code or result code did not match expected values")
        }
    }


    private fun processSaleTxn(
        intentApplication: IntentApplication,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        jsonRequest: JSONObject
    ) {
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
        val formattedTotal = String.format("%.2f", totalAmount).toDouble()
        cart.amounts.find { it.name == "Subtotal" }?.value = formattedTotal
        cart.amounts.find { it.name == "Total" }?.value = formattedTotal
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
            amountsContainer.addView(layout)
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
        itemsRecyclerView.visibility = View.GONE
    }

    private fun getPayloadJSON(referenceId:String,totalAmount:Double):JSONObject{
        val totalAmt = formatToTwoDecimalPlaces(totalAmount)
        txnTotalAmount = totalAmount
        return JSONObject().apply {
            put("type", selectedTransactionType)
            put("paymentType", "Credit")
            put("amount", totalAmt)
            put("tip", customerTip)
            put("applicationType", "DVPAYLITE")
            put("refId", referenceId)
            put("receiptType", "receiptType")
            put("isTxnStatusScreenRequired", if (showApproval) "Yes" else "No")
            put("showBreakupScreen", if (showBreakup) "Yes" else "No")
            put("showDualPriceScreen", if (showDual) "Yes" else "No")
            put("showTipScreen", if (showTipScreen) "Yes" else "No")
        }
    }

    private fun hideSoftKeyboard(){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(referenceIDEditText.windowToken, 0)
    }


}