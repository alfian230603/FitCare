package com.app.fitcare.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.app.fitcare.models.User
import com.google.gson.Gson

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveUser(userId: String, user: User) {
        val userJson = Gson().toJson(user)
        prefs.edit()
            .putString(KEY_USER, userJson)
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun getUser(): Pair<String, User>? {
        val userJson = prefs.getString(KEY_USER, null)
        val userId = prefs.getString(KEY_USER_ID, null)
        return if (userJson != null && userId != null) {
            val user = Gson().fromJson(userJson, User::class.java)
            userId to user
        } else {
            null
        }
    }

    fun saveDailySteps(date: String, steps: Int) {
        Log.d("SessionManager", "Langkah disimpan pada $date: $steps")
        prefs.edit()
            .putInt("$KEY_DAILY_STEPS$date", steps)
            .apply()
    }

    fun getDailySteps(date: String): Int {
        val steps = prefs.getInt("$KEY_DAILY_STEPS$date", 0)
        Log.d("SessionManager", "Langkah pada $date adalah $steps")
        return steps
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "FitCareSession"
        private const val KEY_USER = "User"
        private const val KEY_USER_ID = "UserId"
        private const val KEY_DAILY_STEPS = "DailySteps"
    }
}
