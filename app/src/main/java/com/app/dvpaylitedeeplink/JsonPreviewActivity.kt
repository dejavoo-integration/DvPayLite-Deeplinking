package com.app.dvpaylitedeeplink

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class JsonPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_json_preview)

        val jsonEditText = findViewById<AppCompatEditText>(R.id.jsonEditText)
        val confirmButton = findViewById<AppCompatButton>(R.id.confirmButton)

        // Get JSON from Intent
        val jsonPayload = intent.getStringExtra("jsonPayload") ?: "{}"

        // Try pretty-printing it for readability
        jsonEditText.setText(prettyPrintJson(jsonPayload))

        confirmButton.setOnClickListener {
            val userInput = jsonEditText.text.toString().trim()

            val (isValid, errorMsg) = isValidJson(userInput)
            if (isValid) {
                val resultIntent = intent
                resultIntent.putExtra("editedJson", userInput)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Invalid JSON: $errorMsg",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Validate whether a string is valid JSON.
     * Returns a Pair<isValid, errorMessage>
     */
    private fun isValidJson(input: String): Pair<Boolean, String?> {
        if (input.isEmpty()) return false to "JSON cannot be empty"

        return try {
            when {
                input.trim().startsWith("{") -> JSONObject(input)
                input.trim().startsWith("[") -> JSONArray(input)
                else -> return false to "JSON must start with { or ["
            }
            true to null
        } catch (e: JSONException) {
            false to e.message
        }
    }

    /**
     * Pretty print JSON if possible, otherwise return original string.
     */
    private fun prettyPrintJson(jsonString: String): String {
        return try {
            val trimmed = jsonString.trim()
            when {
                trimmed.startsWith("{") -> JSONObject(trimmed).toString(4)
                trimmed.startsWith("[") -> JSONArray(trimmed).toString(4)
                else -> jsonString
            }
        } catch (e: JSONException) {
            jsonString // fallback to raw text if not valid JSON
        }
    }
}
