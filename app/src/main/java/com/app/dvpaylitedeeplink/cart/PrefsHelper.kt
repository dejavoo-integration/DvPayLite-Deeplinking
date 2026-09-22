package com.app.dvpaylitedeeplink.cart

import android.content.Context

object PrefsHelper {
    private const val PREF_NAME = "app_settings"
    private const val KEY_APPROVAL = "approval_screen"
    private const val KEY_BREAKUP = "breakup_screen"
    private const val KEY_DUAL = "dual_screen"
    private const val KEY_TIP_SCREEN = "tip_screen"
    private const val KEY_LINE_ITEM = "line_items"

    fun saveSettings(context: Context, approval: Boolean, breakup: Boolean, tipScreen: Boolean, dual: Boolean , showLineItems:Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_APPROVAL, approval)
            putBoolean(KEY_BREAKUP, breakup)
            putBoolean(KEY_TIP_SCREEN, tipScreen)
            putBoolean(KEY_DUAL, dual)
            putBoolean(KEY_LINE_ITEM, showLineItems)
            apply()
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
}
