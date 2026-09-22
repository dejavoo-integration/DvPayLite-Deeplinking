package com.app.dvpaylitedeeplink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.denovo.app.invokeiposgo.enums.ApplicationType
import com.denovo.app.invokeiposgo.interfaces.GetTPNListener
import com.denovo.app.invokeiposgo.launcher.IntentApplication
import com.denovo.app.invokeiposgo.models.InvokeApp
import org.json.JSONObject

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        getTpn()
    }
    private fun getTpn() {
        InvokeApp.intentApplication = IntentApplication(this)
        val register: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(
                "onActivityResult",
                "resultCode=${result?.resultCode}, ${result?.data.toString()}"
            )
            Log.d("onActivityResult", "resultCode=${result?.resultCode}, ${result?.data?.toUri(0)}")

            InvokeApp.intentApplication.handleResultCallBack(result)
        }


        InvokeApp.intentApplication.setGetTPNListener(object : GetTPNListener {
            override fun onApplicationLaunched(response: JSONObject?) {
                Log.i("getTpn", "onApplicationLaunched, ${response}")
            }

            override fun onApplicationLaunchFailed(response: JSONObject?) {
                Log.i("getTpn", "onApplicationLaunchFailed, ${response}")

            }

            override fun onGetTPN(response: JSONObject?) {
                Log.i("getTpn", "onGetTPN: $response")

            }

            override fun onTPNFailed(response: JSONObject?) {
                Log.i("getTpn", "onTPNFailed")
            }
        })
        InvokeApp.intentApplication.getTPN(getTPNRequest1(), register)
    }

    private fun getTPNRequest1(): JSONObject {
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

}