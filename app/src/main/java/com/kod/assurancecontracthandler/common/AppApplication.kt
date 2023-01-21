package com.kod.assurancecontracthandler.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.*
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables.CHECKING_EXPIRY_CONTRACTS_WORKNAME
import com.kod.assurancecontracthandler.common.workmanager.ExpirationWorker
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import java.util.concurrent.TimeUnit

class AppApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            ConstantsVariables.expiryChannelID_str,
            getString(R.string.expiry_channel), NotificationManager.IMPORTANCE_HIGH)
            .apply {
                description = getString(R.string.expiry_channel_description)
            }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        creatingWorkManager()
    }

    private fun creatingWorkManager(){
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(false)
            .setRequiresBatteryNotLow(false)
            .build()
        val workManager = WorkManager.getInstance(this)
        val workRequest = PeriodicWorkRequestBuilder<ExpirationWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(CHECKING_EXPIRY_CONTRACTS_WORKNAME, ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }
}