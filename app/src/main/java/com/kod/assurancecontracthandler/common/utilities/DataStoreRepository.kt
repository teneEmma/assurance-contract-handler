package com.kod.assurancecontracthandler.common.utilities

import android.content.SharedPreferences

class DataStoreRepository(private val sharedPrefs: SharedPreferences){
    companion object{
        private const val SHARED_PREFERENCE_MESSAGE = "predefined_message"
        private const val SHARED_PREFERENCE_NEWLY_INSTALLED = "newly_installed"
    }

    fun readPredefinedMessage(): String = sharedPrefs.getString(SHARED_PREFERENCE_MESSAGE, "VIDE")!!

    fun savePredefinedMessage(msg: String) = sharedPrefs.edit().putString(SHARED_PREFERENCE_MESSAGE, msg).apply()

    fun isFirstTime(): Boolean = sharedPrefs.getBoolean(
        SHARED_PREFERENCE_NEWLY_INSTALLED,
        true)

    fun setFirstTimeNot() = sharedPrefs.edit().putBoolean(
        SHARED_PREFERENCE_NEWLY_INSTALLED, false).apply()
}