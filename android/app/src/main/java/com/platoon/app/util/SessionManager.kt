package com.platoon.app.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("platoon_prefs", Context.MODE_PRIVATE)

    var platoonId: String
        get() = prefs.getString(KEY_PLATOON_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PLATOON_ID, value).apply()

    var platoonName: String
        get() = prefs.getString(KEY_PLATOON_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PLATOON_NAME, value).apply()

    var customLogoData: String
        get() = prefs.getString(KEY_LOGO, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LOGO, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    fun isLoggedIn() = platoonId.isNotEmpty()

    fun logout() {
        prefs.edit()
            .remove(KEY_PLATOON_ID)
            .remove(KEY_PLATOON_NAME)
            .remove(KEY_LOGO)
            .apply()
    }

    companion object {
        private const val KEY_PLATOON_ID = "platoonId"
        private const val KEY_PLATOON_NAME = "platoonName"
        private const val KEY_LOGO = "customLogoData"
        private const val KEY_DARK_MODE = "darkMode"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
