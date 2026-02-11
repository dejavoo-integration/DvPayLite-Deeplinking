package com.app.dvpaylitedeeplink.cart

import android.content.Context

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
    private const val KEY_AUTH_ID = "auth_id"
    private const val KEY_IP_ADDRESS = "ip_address"
    private const val KEY_TPN = "tpn_value"
    private const val KEY_SELECTED_MODE = "selected_mode"

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
            remove(KEY_TPN) // clear other mode data
            apply()
        }
    }

    fun saveDeepLink(context: Context, tpn: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_MODE, "DEEPLINK")
            putString(KEY_TPN, tpn)
            remove(KEY_REGISTER_ID)
            remove(KEY_AUTH_ID)
            remove(KEY_IP_ADDRESS)
            apply()
        }
    }

    fun saveUsb(context: Context, registerId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_MODE, "USB")
            putString(KEY_REGISTER_ID, registerId)
            remove(KEY_IP_ADDRESS)
            remove(KEY_AUTH_ID)
            remove(KEY_TPN)
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

    fun getAuthId(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_AUTH_ID, "")

    fun getIpAddress(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_IP_ADDRESS, "")

    fun getTpn(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TPN, "")

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

    fun getSelectMode(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_MODE, "DEEPLINK") ?: "DEEPLINK"
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
