package com.kod.assurancecontracthandler.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.*
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.DataStoreRepository
import com.kod.assurancecontracthandler.common.workmanager.ExpirationWorker
import com.kod.assurancecontracthandler.common.workmanager.FirstUsageWorker
import java.util.concurrent.TimeUnit

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val dataStore = DataStoreRepository(
            sharedPrefs,
            resources.getString(R.string.predefined_message_key),
            resources.getString(R.string.first_install_key),
        )
        if (dataStore.isFirstTime()) {
            dataStore.setFirstTimeNot()
            setupAndSendFirstTimeNotification()
            firstTimeWorkManager()
        }

        setupExpiryNotification()
        creatingWorkManager()
    }

    private fun setupExpiryNotification() {
        val channel = NotificationChannel(
            ConstantsVariables.EXPIRY_CHANNEL_ID_STRING,
            getString(R.string.expiry_channel), NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                description = getString(R.string.expiry_channel_description)
            }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setupAndSendFirstTimeNotification() {
        val channel = NotificationChannel(
            ConstantsVariables.FIRST_USAGE_CHANNEL_ID_STRING,
            getString(R.string.first_time_channel), NotificationManager.IMPORTANCE_HIGH
        )
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
        workManager.enqueueUniqueWork(ConstantsVariables.WELCOME_WORK_NAME, ExistingWorkPolicy.KEEP, workRequest)
    }

    private fun creatingWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(false)
            .setRequiresBatteryNotLow(false)
            .build()
        val workManager = WorkManager.getInstance(this)
        val workRequest = PeriodicWorkRequestBuilder<ExpirationWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            ConstantsVariables.CHECKING_EXPIRY_CONTRACTS_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}