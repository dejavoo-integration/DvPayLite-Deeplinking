package com.app.dvpaylitedeeplink.cart.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.cart.PrefsHelper


class OptionSelectionActivity : AppCompatActivity() {

    private lateinit var switchApproval: SwitchCompat
    private lateinit var switchBreakup: SwitchCompat
    private lateinit var switchDual: SwitchCompat
    private lateinit var switchTip: SwitchCompat
    private lateinit var switchLineItems: SwitchCompat
    private lateinit var btnConfirm: AppCompatButton
    private lateinit var ivBack: AppCompatImageView
    private lateinit var tvTotalAmount: AppCompatTextView
    private lateinit var edtTipAmount: AppCompatEditText
    private lateinit var edtCustomFee: AppCompatEditText
    private lateinit var linearTipFee: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option_selection)

        linearTipFee = findViewById(R.id.linearTipFee)
        switchApproval = findViewById(R.id.switchApproval)
        switchBreakup = findViewById(R.id.switchBreakup)
        switchDual = findViewById(R.id.switchDual)
        switchTip = findViewById(R.id.switchTip)
        switchLineItems = findViewById(R.id.switchLineItems)
        btnConfirm = findViewById(R.id.btnConfirm)
        ivBack = findViewById<AppCompatImageView>(R.id.iv_back)
        tvTotalAmount = findViewById<AppCompatTextView>(R.id.tvTotalAmount)
        edtTipAmount = findViewById<AppCompatEditText>(R.id.edtTipAmount)
        edtCustomFee = findViewById<AppCompatEditText>(R.id.edtFee)

        linearTipFee.visibility = View.GONE
        switchApproval.isChecked = PrefsHelper.getApproval(this)
        switchBreakup.isChecked = PrefsHelper.getBreakup(this)
        switchTip.isChecked = PrefsHelper.getTipScreenStatus(this)
        switchDual.isChecked = PrefsHelper.getDual(this)
        switchLineItems.isChecked = PrefsHelper.getLineItems(this)

        switchApproval.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.saveSettings(
                this,
                isChecked,
                switchBreakup.isChecked,
                switchTip.isChecked,
                switchDual.isChecked,
                switchLineItems.isChecked
            )
        }

        switchBreakup.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                isChecked,
                switchTip.isChecked,
                switchDual.isChecked,
                switchLineItems.isChecked
            )
        }
        switchTip.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                switchBreakup.isChecked,
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
                switchTip.isChecked,
                isChecked,
                switchLineItems.isChecked
            )
        }
        switchLineItems.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                switchBreakup.isChecked,
                switchTip.isChecked,
                switchDual.isChecked,
                isChecked
            )
        }
        btnConfirm.setOnClickListener {
            onBackPressed()
        }

        ivBack.setOnClickListener {
            onBackPressed()
        }

    }

}
