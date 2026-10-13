package com.app.dvpaylitedeeplink.cart.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object PayloadFormatter {

    enum class PayloadKind {
        JSON, XML, UNKNOWN
    }

    fun detectKind(payload: String): PayloadKind {
        val trimmed = payload.trim()
        return when {
            trimmed.isEmpty() -> PayloadKind.UNKNOWN
            trimmed.startsWith("{") || trimmed.startsWith("[") -> PayloadKind.JSON
            trimmed.startsWith("<") -> PayloadKind.XML
            else -> PayloadKind.UNKNOWN
        }
    }

    /**
     * Formats payload for display only. Returns the original string when formatting fails.
     */
    fun formatForDisplay(payload: String?): String {
        if (payload.isNullOrEmpty()) return ""
        return when (detectKind(payload)) {
            PayloadKind.JSON -> prettyPrintJson(payload)
            PayloadKind.XML -> prettyPrintXml(payload)
            PayloadKind.UNKNOWN -> payload
        }
    }

    fun prettyPrintJson(jsonString: String): String {
        return try {
            val trimmed = jsonString.trim()
            when {
                trimmed.startsWith("{") -> JSONObject(trimmed).toString(4)
                trimmed.startsWith("[") -> JSONArray(trimmed).toString(4)
                else -> jsonString
            }
        } catch (_: JSONException) {
            jsonString
        }
    }

    fun prettyPrintXml(xmlString: String): String {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = false
            val document = factory.newDocumentBuilder()
                .parse(InputSource(StringReader(xmlString.trim())))
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            val writer = StringWriter()
            transformer.transform(DOMSource(document), StreamResult(writer))
            writer.toString().trim()
        } catch (_: Exception) {
            xmlString
        }
    }

    fun extractRefId(payload: String?): String? {
        if (payload.isNullOrBlank()) return null
        val trimmed = payload.trim()
        if (trimmed.startsWith("{")) {
            return try {
                val json = JSONObject(trimmed)
                json.optString("refId").takeIf { it.isNotEmpty() }
                    ?: json.optString("RefId").takeIf { it.isNotEmpty() }
            } catch (_: JSONException) {
                null
            }
        }
        if (trimmed.startsWith("<")) {
            return Regex("(?i)<RefId>([^<]*)</RefId>").find(trimmed)?.groupValues?.get(1)
                ?.takeIf { it.isNotEmpty() }
        }
        return null
    }
}
