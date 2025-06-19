package com.mraihanfauzii.restrokotlin.ui.authentication

import android.content.Context

class AuthenticationManager(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val edit = preferences.edit()

    fun login(key: String, value: String) {
        edit.putString(key, value)
        edit.apply()
    }

    fun loginInt(key: String, value: Int) {
        edit.putInt(key, value)
        edit.apply()
    }

    fun setSession(key: String, value: Boolean) {
        edit.putBoolean(key, value)
        edit.apply()
    }

    fun checkSession(key: String): Boolean? {
        return preferences.getBoolean(key, false)
    }

    fun getAccess(key: String): String? {
        return preferences.getString(key, null)
    }

    fun getAccessInt(key: String): Int? {
        return preferences.getInt(key, 0)
    }

    fun logOut() {
        edit.clear()
        edit.apply()
    }

    companion object {
        // Data User
        const val PREFS = "prefs"
        const val ID = "id"
        const val SESSION = "session"
        const val TOKEN = "token"
        const val EMAIL = "email"
        const val USERNAME = "user_name"
        const val NAMALENGKAP = "nama_lengkap"
        const val PHONE_NUMBER = "phone_number"
        const val PROFILE_PICTURE = "profile_picture"
        const val PROFILE_DESCRIPTION = "profile_description"
        const val ROLE = "role"
    }
}