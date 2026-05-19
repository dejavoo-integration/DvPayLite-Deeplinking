package com.app.dvpaylitedeeplink.cart

import android.content.Context
import com.app.dvpaylitedeeplink.cart.ui.RegistrationActivity.RequestFormat

object PrefsHelper {
    private const val PREF_NAME = "app_settings"
    private const val KEY_APPROVAL = "approval_screen"
    private const val KEY_BREAKUP = "breakup_screen"
    private const val KEY_DUAL = "dual_screen"
    private const val KEY_TIP_SCREEN = "tip_screen"
    private const val KEY_LINE_ITEM = "line_items"
    private const val KEY_L2L3_LINE_ITEM = "l2l3_line_items"
    private const val KEY_JSON_PREVIEW = "json_preview"
    private const val KEY_MODE = "transaction_mode"
    private const val KEY_REGISTER_ID = "register_id"
    private const val KEY_TPN_NUMBER = "TPN_NUMBER"
    private const val KEY_AUTH_ID = "auth_id"
    private const val KEY_IP_ADDRESS = "ip_address"
    private const val KEY_TPN = "tpn_value"
    private const val KEY_SELECTED_MODE = "selected_mode"
    private const val KEY_PRIMARY_COLOR = "primary_color"
    private const val KEY_SECONDARY_COLOR = "secondary_color"
    private const val KEY_NEGATIVE_COLOR = "negative_color"
    private const val KEY_FONT = "selected_font"
    private const val KEY_SELECTED_AVS = "selected_avs"
    private const val KEY_SELECTED_LOGO = "selected_logo"
    private const val KEY_REQUEST_FORMAT = "request_format"
    private const val KEY_USB_BULK_ENABLED = "usb_bulk_enabled"
    private const val KEY_USB_BULK_COUNT = "usb_bulk_count"

    fun saveSettings(context: Context, approval: Boolean, breakup: Boolean, tipScreen: Boolean, dual: Boolean , showLineItems:Boolean, sendL2L3: Boolean,showJsonPreview :Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_APPROVAL, approval)
            putBoolean(KEY_BREAKUP, breakup)
            putBoolean(KEY_TIP_SCREEN, tipScreen)
            putBoolean(KEY_DUAL, dual)
            putBoolean(KEY_LINE_ITEM, showLineItems)
            putBoolean(KEY_L2L3_LINE_ITEM, sendL2L3)
            putBoolean(KEY_JSON_PREVIEW, showJsonPreview)
            apply()
        }
    }

    fun saveCloud(context: Context, registerId: String, authId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_MODE, "CLOUD")
            putString(KEY_REGISTER_ID, registerId)
            putString(KEY_AUTH_ID, authId)
            remove(KEY_TPN_NUMBER)
            remove(KEY_TPN) // clear other mode data
            apply()
        }
    }

    fun saveLocal(context: Context, registerId: String, ipAddress: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_MODE, "LOCAL")
            putString(KEY_REGISTER_ID, registerId)
            putString(KEY_IP_ADDRESS, ipAddress)
            remove(KEY_TPN_NUMBER)
            remove(KEY_TPN) // clear other mode data
            apply()
        }
    }

    fun saveDeepLink(context: Context, tpn: String, primaryColor : String, secondaryColor: String, negativeColor:String,
                     selectedAvs: String, selectedLogo:String, font : String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_MODE, "DEEPLINK")
            putString(KEY_TPN, tpn)
            putString(KEY_PRIMARY_COLOR, primaryColor)
            putString(KEY_SECONDARY_COLOR, secondaryColor)
            putString(KEY_NEGATIVE_COLOR, negativeColor)
            putString(KEY_SELECTED_AVS, selectedAvs)
            putString(KEY_SELECTED_LOGO, selectedLogo)
            putString(KEY_FONT, font)
            remove(KEY_REGISTER_ID)
            remove(KEY_IP_ADDRESS)
            apply()
        }
    }

    fun saveUsb(context: Context, registerId: String, tpnNumber: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_MODE, "USB")
            putString(KEY_REGISTER_ID, registerId)
            putString(KEY_TPN_NUMBER, tpnNumber)
            remove(KEY_IP_ADDRESS)
            remove(KEY_TPN)
            putBoolean(KEY_USB_BULK_ENABLED, false)
            putInt(KEY_USB_BULK_COUNT, 1)
            apply()
        }
    }

    fun saveUsbBulkConfig(context: Context, enabled: Boolean, count: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_USB_BULK_ENABLED, enabled)
            putInt(KEY_USB_BULK_COUNT, count)
            apply()
        }
    }

    fun saveMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_MODE, mode).apply()
    }

    fun getMode(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MODE, "")

    fun getRegisterId(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REGISTER_ID, "")

    fun getTPNNumber(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TPN_NUMBER, "")

    fun getAuthId(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_AUTH_ID, "")

    fun getIpAddress(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_IP_ADDRESS, "")

    fun getTpn(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TPN, "")
    fun getPrimaryColor(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PRIMARY_COLOR, "")
    fun getSecondaryColor(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SECONDARY_COLOR, "")
    fun getNegativeColor(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NEGATIVE_COLOR, "")
    fun getSelectedAvs(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_AVS, "")
    fun getSelectedLogo(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_LOGO, "")
    fun getFont(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FONT, "")

    fun clearModeData(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            remove(KEY_MODE)
            remove(KEY_REGISTER_ID)
            remove(KEY_AUTH_ID)
            remove(KEY_TPN)
            apply()
        }
    }

    fun isUsbBulkEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_USB_BULK_ENABLED, false)
    }

    fun getUsbBulkCount(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_USB_BULK_COUNT, 1)
    }

    fun getSelectMode(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_MODE, "DEEPLINK") ?: "DEEPLINK"
    }

    fun saveRequestFormat(context: Context, format: RequestFormat) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_REQUEST_FORMAT, format.name).apply()
    }

    fun getRequestFormat(context: Context): RequestFormat {
        val value = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REQUEST_FORMAT, RequestFormat.XML.name)

        return try {
            RequestFormat.valueOf(value!!)
        } catch (e: Exception) {
            RequestFormat.XML
        }
    }

    fun getApproval(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_APPROVAL, false)

    fun getBreakup(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_BREAKUP, false)

    fun getDual(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_DUAL, false)

    fun getTipScreenStatus(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TIP_SCREEN, false)

    fun getLineItems(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_LINE_ITEM, false)

    fun getL2L3LineItems(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_L2L3_LINE_ITEM, false)

    fun getJsonPreviewStatus(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_JSON_PREVIEW, false)
}
