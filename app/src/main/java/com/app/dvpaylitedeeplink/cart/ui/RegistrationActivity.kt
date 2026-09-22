package com.app.dvpaylitedeeplink.cart.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
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
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.cart.PrefsHelper
import com.denovo.app.invokeiposgo.interfaces.TerminalAddListener
import com.denovo.app.invokeiposgo.launcher.IntentApplication
import org.json.JSONObject


class RegistrationActivity : AppCompatActivity() {

    private lateinit var btnConfirm: AppCompatButton
    private lateinit var ivBack: AppCompatImageView
    private lateinit var edtTpn: AppCompatEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)


        btnConfirm = findViewById(R.id.btnConfirm)
        ivBack = findViewById<AppCompatImageView>(R.id.iv_back)
        edtTpn = findViewById<AppCompatEditText>(R.id.edtTPN)
        val intentApplication = IntentApplication(applicationContext)

        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            intentApplication.handleResultCallBack(result)
        }

        btnConfirm.setOnClickListener {
            try {
                registerApp(intentApplication, activityResultLauncher)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@RegistrationActivity,
                    "Unable to Register ",
                    Toast.LENGTH_LONG
                ).show()
            }        }

        ivBack.setOnClickListener {
            onBackPressed()
        }

    }

    private fun registerApp(
        intentApplication: IntentApplication,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        //   val jsonRequest:JSONObject = {“tpn”:”123456789012”, “applicationType”:”DVPAYLITE”}

        if(edtTpn.text.toString().trim().isEmpty()){
            throw Exception()
        }

        val jsonRequest = JSONObject()
        jsonRequest.put("tpn", edtTpn.text.toString().trim())
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
}
