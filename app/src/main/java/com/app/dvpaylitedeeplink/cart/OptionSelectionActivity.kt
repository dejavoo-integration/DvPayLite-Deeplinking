package com.app.dvpaylitedeeplink.cart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.cart.CartActivity
import com.app.dvpaylitedeeplink.cart.PrefsHelper


class OptionSelectionActivity : AppCompatActivity() {

    private lateinit var switchApproval: SwitchCompat
    private lateinit var switchBreakup: SwitchCompat
    private lateinit var switchDual: SwitchCompat
    private lateinit var switchLineItems: SwitchCompat
    private lateinit var btnConfirm: AppCompatButton
    private lateinit var ivBack: AppCompatImageView
    private lateinit var tvTotalAmount: AppCompatTextView
    private lateinit var edtTipAmount: AppCompatEditText
    private lateinit var edtCustomFee: AppCompatEditText
    private var txnAmount: Double = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option_selection)

        switchApproval = findViewById(R.id.switchApproval)
        switchBreakup = findViewById(R.id.switchBreakup)
        switchDual = findViewById(R.id.switchDual)
        switchLineItems = findViewById(R.id.switchLineItems)
        btnConfirm = findViewById(R.id.btnConfirm)
        ivBack = findViewById<AppCompatImageView>(R.id.iv_back)
        tvTotalAmount = findViewById<AppCompatTextView>(R.id.tvTotalAmount)
        edtTipAmount = findViewById<AppCompatEditText>(R.id.edtTipAmount)
        edtCustomFee = findViewById<AppCompatEditText>(R.id.edtFee)

        getIntentValues()
        switchApproval.isChecked = PrefsHelper.getApproval(this)
        switchBreakup.isChecked = PrefsHelper.getBreakup(this)
        switchDual.isChecked = PrefsHelper.getDual(this)
        switchLineItems.isChecked = PrefsHelper.getLineItems(this)

        switchApproval.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.saveSettings(
                this,
                isChecked,
                switchBreakup.isChecked,
                switchDual.isChecked,
                switchLineItems.isChecked
            )
        }

        switchBreakup.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                isChecked,
                switchDual.isChecked,
                switchLineItems.isChecked
            )
        }

        switchDual.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                switchBreakup.isChecked,
                isChecked,
                switchLineItems.isChecked
            )
        }
        switchLineItems.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                switchBreakup.isChecked,
                switchDual.isChecked,
                isChecked
            )
        }
        btnConfirm.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("tip", edtTipAmount.text.toString().toDoubleOrNull() ?: 0.0)
            resultIntent.putExtra("fee", edtCustomFee.text.toString().toDoubleOrNull() ?: 0.0)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        ivBack.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val resultIntent = Intent()
        resultIntent.putExtra("tip", "")
        resultIntent.putExtra("fee", "")
        setResult(Activity.RESULT_CANCELED, resultIntent)
        finish()
    }

    private fun getIntentValues() {
        if (intent.hasExtra("txnAmount")){
           txnAmount= intent.getDoubleExtra("txnAmount",0.0)
            if (txnAmount > 0) {
                tvTotalAmount.visibility = View.VISIBLE
                tvTotalAmount.text = String.format("$%.2f", txnAmount)
            } else {
                tvTotalAmount.visibility = View.GONE
            }
        }
    }
}
