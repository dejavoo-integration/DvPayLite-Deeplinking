package com.app.dvpaylitedeeplink.logger

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

object LoggerManager {

    private val lock = Any()
    private val writeExecutor = Executors.newSingleThreadExecutor()

    private fun getTodayFile(context: Context): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = dateFormat.format(Date())
        val fileName = "log_$todayDate.txt"
        return File(context.getExternalFilesDir(null), fileName)
    }

    fun log(context: Context, message: String) {
        val appContext = context.applicationContext
        writeExecutor.execute {
            try {
                synchronized(lock) {
                    val file = getTodayFile(appContext)
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val time = timeFormat.format(Date())
                    val logMessage = "$time -> $message\n"
                    FileWriter(file, true).use { writer ->
                        writer.append(logMessage)
                        writer.flush()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
