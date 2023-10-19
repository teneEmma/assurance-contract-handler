package com.kod.assurancecontracthandler.common.utilities

import android.content.SharedPreferences

class DataStoreRepository(
    private val sharedPrefs: SharedPreferences,
    private val predefinedMessageKey: String,
    private val newlyInstalledKey: String,
) {

    fun readPredefinedMessage(): String? = sharedPrefs.getString(predefinedMessageKey, "")

    fun isFirstTime(): Boolean = sharedPrefs.getBoolean(
        newlyInstalledKey,
        true
    )

    fun setFirstTimeNot() = sharedPrefs.edit().putBoolean(
        newlyInstalledKey, false
    ).apply()
}