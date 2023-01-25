package com.kod.assurancecontracthandler.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import androidx.work.*
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables.CHECKING_EXPIRY_CONTRACTS_WORKNAME
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables.WELCOME_WORKNAME
import com.kod.assurancecontracthandler.common.utilities.DataStoreRepository
import com.kod.assurancecontracthandler.common.workmanager.ExpirationWorker
import com.kod.assurancecontracthandler.common.workmanager.FirstUsageWorker
import java.util.concurrent.TimeUnit

class AppApplication: Application() {
    private val sharedPrefs: SharedPreferences by lazy {
        getSharedPreferences(ConstantsVariables.APP_USAGE_STATE, Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        val dataStore = DataStoreRepository(sharedPrefs)
        if(dataStore.isFirstTime()){
            dataStore.setFirstTimeNot()
            setupFirstTimeNotification()
            firstTimeWorkManager()
        }

        setupExpiryNotification()
        creatingWorkManager()
    }

    private fun setupExpiryNotification() {
        val channel = NotificationChannel(
            ConstantsVariables.expiryChannelID_str,
            getString(R.string.expiry_channel), NotificationManager.IMPORTANCE_HIGH)
            .apply {
                description = getString(R.string.expiry_channel_description)
            }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setupFirstTimeNotification() {
        val channel = NotificationChannel(
            ConstantsVariables.FIRST_USAGE_CHANNEL_ID_STRING,
            getString(R.string.first_time_channel), NotificationManager.IMPORTANCE_HIGH)
            .apply {
                description = getString(R.string.first_time_channel_description)
            }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun firstTimeWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(false)
            .setRequiresBatteryNotLow(false)
            .build()
        val workManager = WorkManager.getInstance(this)
        val workRequest = OneTimeWorkRequestBuilder<FirstUsageWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniqueWork(WELCOME_WORKNAME, ExistingWorkPolicy.KEEP, workRequest)
    }

    private fun creatingWorkManager(){
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(false)
            .setRequiresBatteryNotLow(false)
            .build()
        val workManager = WorkManager.getInstance(this)
        val workRequest = PeriodicWorkRequestBuilder<ExpirationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(CHECKING_EXPIRY_CONTRACTS_WORKNAME, ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }
}