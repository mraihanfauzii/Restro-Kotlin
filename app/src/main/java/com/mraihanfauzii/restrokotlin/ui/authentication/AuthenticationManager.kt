package com.mraihanfauzii.restrokotlin.ui.authentication

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.mraihanfauzii.restrokotlin.model.LoginResponse
import com.mraihanfauzii.restrokotlin.model.PatientProfileResponse

class AuthenticationManager(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val edit = preferences.edit()

    fun saveAccess(key: String, value: String) {
        edit.putString(key, value)
        edit.apply()
    }

    fun saveAccessInt(key: String, value: Int) {
        edit.putInt(key, value)
        edit.apply()
    }

    fun setSession(key: String, value: Boolean) {
        edit.putBoolean(key, value)
        edit.apply()
    }

    fun checkSession(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }

    fun getAccess(key: String): String? {
        return preferences.getString(key, null)
    }

    fun getAccessInt(key: String): Int {
        return preferences.getInt(key, 0)
    }

    fun saveLoginResponseData(response: LoginResponse) {
        edit.apply {
            setSession(SESSION, true)
            putString(TOKEN, response.accessToken)
            putInt(ID, response.user.id)
            putString(EMAIL, response.user.email)
            putString(USERNAME, response.user.username)
            putString(NAMALENGKAP, response.user.namaLengkap)
            putString(ROLE, response.user.role)
            putInt(TOTAL_POINTS, response.user.totalPoints)
            putString(FIREBASE_UID, response.user.id.toString())
            putString(PROFILE_PICTURE, null)
        }.apply()
    }

    fun updateProfileData(profile: PatientProfileResponse) {
        edit.apply {
            putString(USERNAME, profile.username)
            putString(EMAIL, profile.email)
            putString(PHONE_NUMBER, profile.phoneNumber)
            putString(NAMALENGKAP, profile.fullName)
            putString(PROFILE_PICTURE, profile.profilePictureUrl)
        }.apply()
    }

    fun getCachedPatientProfile(): PatientProfileResponse? {
        val id = preferences.getInt(ID, -1)
        if (id == -1) return null

        return PatientProfileResponse(
            id = id,
            username = preferences.getString(USERNAME, null),
            email = preferences.getString(EMAIL, null),
            phoneNumber = preferences.getString(PHONE_NUMBER, null),
            fullName = preferences.getString(NAMALENGKAP, null),
            gender = null,
            dateOfBirth = null,
            placeOfBirth = null,
            address = null,
            companionName = null,
            height = null,
            weight = null,
            bloodType = null,
            medicalHistory = null,
            allergyHistory = null,
            profilePictureUrl = preferences.getString(PROFILE_PICTURE, null),
            profileDescription = null
        )
    }

    fun setCameraPermissionToastShown(shown: Boolean) {
        edit.putBoolean(CAMERA_PERMISSION_TOAST_SHOWN, shown).apply()
    }

    fun isCameraPermissionToastShown(): Boolean {
        return preferences.getBoolean(CAMERA_PERMISSION_TOAST_SHOWN, false)
    }


    fun logOut() {
        // Hapus semua data dari SharedPreferences
        edit.clear()
        edit.apply()
        // Lakukan signOut dari Firebase Auth
        FirebaseAuth.getInstance().signOut()
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
        const val TOTAL_POINTS = "total_points"
        const val ROLE = "role"
        const val FIREBASE_UID = "firebase_uid"
        const val CAMERA_PERMISSION_TOAST_SHOWN = "camera_permission_toast_shown"
    }
}