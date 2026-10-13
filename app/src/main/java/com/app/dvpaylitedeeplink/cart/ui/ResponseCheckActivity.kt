package com.app.dvpaylitedeeplink.cart.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.cart.TransactionResponseTracker
import com.app.dvpaylitedeeplink.cart.util.PayloadFormatter
import com.app.dvpaylitedeeplink.logger.LoggerManager

class ResponseCheckActivity : AppCompatActivity() {

    private lateinit var tvEmptyState: AppCompatTextView
    private lateinit var scrollRequest: View
    private lateinit var scrollResponse: View
    private lateinit var tvRequestLabel: AppCompatTextView
    private lateinit var tvResponseLabel: AppCompatTextView
    private lateinit var tvRequestBody: AppCompatTextView
    private lateinit var tvResponseBody: AppCompatTextView

    private val trackerListener = TransactionResponseTracker.Listener { snapshot ->
        renderSnapshot(snapshot)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_response_check)
        LoggerManager.log(this, "Open ResponseCheckActivity")

        findViewById<AppCompatImageView>(R.id.iv_back).setOnClickListener { finish() }

        tvEmptyState = findViewById(R.id.tv_empty_state)
        scrollRequest = findViewById(R.id.scroll_request)
        scrollResponse = findViewById(R.id.scroll_response)
        tvRequestLabel = findViewById(R.id.tv_request_label)
        tvResponseLabel = findViewById(R.id.tv_response_label)
        tvRequestBody = findViewById(R.id.tv_request_body)
        tvResponseBody = findViewById(R.id.tv_response_body)

        renderSnapshot(TransactionResponseTracker.currentSnapshot())
    }

    override fun onStart() {
        super.onStart()
        TransactionResponseTracker.addListener(trackerListener)
    }

    override fun onStop() {
        TransactionResponseTracker.removeListener(trackerListener)
        super.onStop()
    }

    private fun renderSnapshot(snapshot: TransactionResponseTracker.Snapshot) {
        val hasRequest = !snapshot.rawRequest.isNullOrBlank()
        if (!hasRequest) {
            tvEmptyState.visibility = View.VISIBLE
            tvRequestLabel.visibility = View.GONE
            scrollRequest.visibility = View.GONE
            tvResponseLabel.visibility = View.GONE
            scrollResponse.visibility = View.GONE
            tvRequestBody.text = ""
            tvResponseBody.text = ""
            return
        }

        tvEmptyState.visibility = View.GONE
        tvRequestLabel.visibility = View.VISIBLE
        scrollRequest.visibility = View.VISIBLE
        tvResponseLabel.visibility = View.VISIBLE
        scrollResponse.visibility = View.VISIBLE

        tvRequestBody.text = PayloadFormatter.formatForDisplay(snapshot.rawRequest)

        if (snapshot.waitingForResponse) {
            tvResponseBody.text = getString(R.string.waiting_for_response)
        } else if (snapshot.rawResponse.isNullOrBlank()) {
            tvResponseBody.text = getString(R.string.response_check_no_response)
        } else {
            tvResponseBody.text = PayloadFormatter.formatForDisplay(snapshot.rawResponse)
        }
    }
}
