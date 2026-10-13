package com.app.dvpaylitedeeplink.cart

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicLong

/**
 * Mirrors SPIN / USB log lines for the Response Check screen only.
 * Does not participate in sending or parsing transactions.
 */
object TransactionResponseTracker {

    data class Snapshot(
        val rawRequest: String?,
        val rawResponse: String?,
        val waitingForResponse: Boolean
    )

    fun interface Listener {
        fun onTransactionUpdated(snapshot: Snapshot)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = CopyOnWriteArraySet<Listener>()
    private val sessionCounter = AtomicLong(0)

    @Volatile
    private var activeSessionId: Long = 0

    @Volatile
    private var rawRequest: String? = null

    @Volatile
    private var rawResponse: String? = null

    fun addListener(listener: Listener) {
        listeners.add(listener)
        mainHandler.post { listener.onTransactionUpdated(currentSnapshot()) }
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun currentSnapshot(): Snapshot {
        val request = rawRequest
        val response = rawResponse
        return Snapshot(
            rawRequest = request,
            rawResponse = response,
            waitingForResponse = request != null && response == null
        )
    }

    /** Same content as SPIN_REQUEST / Processing USB Request / LOCAL_URL log lines. */
    fun recordSpinRequest(logRequest: String) {
        val sessionId = sessionCounter.incrementAndGet()
        synchronized(this) {
            activeSessionId = sessionId
            rawRequest = logRequest
            rawResponse = null
        }
        notifyListeners()
    }

    /** Same content as SPIN_RESPONSE / USB Result / LOCAL_RESPONSE log lines. */
    fun recordSpinResponse(logResponse: String) {
        synchronized(this) {
            if (activeSessionId == 0L) return
            rawResponse = logResponse
        }
        notifyListeners()
    }

    private fun notifyListeners() {
        val snapshot = currentSnapshot()
        mainHandler.post {
            for (listener in listeners) {
                listener.onTransactionUpdated(snapshot)
            }
        }
    }
}
