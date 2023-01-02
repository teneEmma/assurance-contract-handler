package com.kod.assurancecontracthandler.repository

import android.content.SharedPreferences

class DataStoreRepository(private val sharedPrefs: SharedPreferences){
    companion object{
        private const val SHARED_PREFERENCE_MESSAGE = "predefined_message"
    }

    fun readPredefinedMessage(): String = sharedPrefs.getString(SHARED_PREFERENCE_MESSAGE, "VIDE")!!

    fun savePredefinedMessage(msg: String) = sharedPrefs.edit().putString(SHARED_PREFERENCE_MESSAGE, msg).apply()

}