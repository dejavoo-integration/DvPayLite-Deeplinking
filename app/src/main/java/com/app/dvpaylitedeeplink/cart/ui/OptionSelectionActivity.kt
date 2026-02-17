package com.app.dvpaylitedeeplink.cart.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
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
import com.app.dvpaylitedeeplink.logger.LoggerManager


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
    private lateinit var linearShowBreakup: LinearLayout
    private lateinit var sendL2L3Data: SwitchCompat
    private lateinit var showJsonPreview: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option_selection)
        LoggerManager.log(this, "OpenOptionSelectorActivity")
        linearTipFee = findViewById(R.id.linearTipFee)
        linearShowBreakup = findViewById(R.id.linearShowBreakup)
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
        sendL2L3Data = findViewById(R.id.set_l2l3_data)
        showJsonPreview = findViewById(R.id.switchJsonPreview)

        linearTipFee.visibility = View.GONE
        linearShowBreakup.visibility = if (Build.MODEL.equals("P18", ignoreCase = true)) View.GONE else View.VISIBLE //As of now, this feature not handled in DVAmphi

        switchApproval.isChecked = PrefsHelper.getApproval(this)
        switchBreakup.isChecked = PrefsHelper.getBreakup(this)
        switchTip.isChecked = PrefsHelper.getTipScreenStatus(this)
        switchDual.isChecked = PrefsHelper.getDual(this)
        switchLineItems.isChecked = PrefsHelper.getLineItems(this)
        sendL2L3Data.isChecked = PrefsHelper.getL2L3LineItems(this)
        showJsonPreview.isChecked = PrefsHelper.getJsonPreviewStatus(this)

        switchApproval.setOnCheckedChangeListener { _, isChecked ->
            LoggerManager.log(this, "switchApproval Clicked")
            PrefsHelper.saveSettings(
                this,
                isChecked,
                switchBreakup.isChecked,
                switchTip.isChecked,
                switchDual.isChecked,
                switchLineItems.isChecked,
                sendL2L3Data.isChecked,
                showJsonPreview.isChecked,

            )
        }

        switchBreakup.setOnCheckedChangeListener { _, isChecked ->
            LoggerManager.log(this, "switchBreakup Clicked")
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                isChecked,
                switchTip.isChecked,
                switchDual.isChecked,
                switchLineItems.isChecked,
                sendL2L3Data.isChecked,
                showJsonPreview.isChecked,
            )
        }
        switchTip.setOnCheckedChangeListener { _, isChecked ->
            LoggerManager.log(this, "switchTip Clicked")
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                switchBreakup.isChecked,
                isChecked,
                switchDual.isChecked,
                switchLineItems.isChecked,
                sendL2L3Data.isChecked,
                showJsonPreview.isChecked,
            )
        }
        switchDual.setOnCheckedChangeListener { _, isChecked ->
            LoggerManager.log(this, "switchDual Clicked")
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                switchBreakup.isChecked,
                switchTip.isChecked,
                isChecked,
                switchLineItems.isChecked,
                sendL2L3Data.isChecked,
                showJsonPreview.isChecked,
            )
        }
        switchLineItems.setOnCheckedChangeListener { _, isChecked ->
            LoggerManager.log(this, "switchLineItems Clicked")
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                switchBreakup.isChecked,
                switchTip.isChecked,
                switchDual.isChecked,
                isChecked,
                sendL2L3Data.isChecked,
                showJsonPreview.isChecked,
            )
        }
        btnConfirm.setOnClickListener {
            LoggerManager.log(this, "Button confirm Clicked")
            onBackPressed()
        }

        ivBack.setOnClickListener {
            LoggerManager.log(this, "back button Clicked")
            onBackPressed()
        }

        sendL2L3Data.setOnCheckedChangeListener { _, isChecked ->
            LoggerManager.log(this, "sendL2L3Data Clicked")
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                switchBreakup.isChecked,
                switchTip.isChecked,
                switchDual.isChecked,
                switchLineItems.isChecked,
                isChecked,
                showJsonPreview.isChecked,
            )
        }
        showJsonPreview.setOnCheckedChangeListener { _, isChecked ->
            LoggerManager.log(this, "showJsonPreview Clicked")
            PrefsHelper.saveSettings(
                this,
                switchApproval.isChecked,
                switchBreakup.isChecked,
                switchTip.isChecked,
                switchDual.isChecked,
                switchLineItems.isChecked,
                sendL2L3Data.isChecked,
                isChecked,
            )
        }

    }

}
