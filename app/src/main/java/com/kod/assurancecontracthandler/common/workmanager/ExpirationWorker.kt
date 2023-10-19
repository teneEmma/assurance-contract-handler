package com.kod.assurancecontracthandler.common.workmanager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.selectfileviewmodel.ExpirationWorkerViewModel
import com.kod.assurancecontracthandler.views.expiringactivity.ExpiringContractsActivity
import java.time.LocalDateTime
import java.time.ZoneOffset

open class ExpirationWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val isExpiringNotificationEnabled =
            sharedPrefs.getBoolean(context.resources.getString(R.string.expiring_contracts_notifications_key), false)
        if (isExpiringNotificationEnabled) {
            checkExpiringContracts()
        }
        return Result.success()
    }

    private fun checkExpiringContracts() {
        val contractRepository = ContractRepository(ContractDatabase.getDatabase(context).contractDao())
        val contractViewModel = ExpirationWorkerViewModel(contractRepository)


        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val timeUnit =
            sharedPrefs.getString(context.resources.getString(R.string.expiring_notifications_periodicity_key), "1")
        Log.e("periodicity", "--> $timeUnit <--")
        val todayInLong = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000
        val todayString = TimeConverters.formatLongToLocaleDate(todayInLong)
        val maxDateInLong = LocalDateTime.now().plusDays(timeUnit?.toLong() ?: 1L)
            .toEpochSecond(ZoneOffset.UTC) * 1000
        val maxDateString = TimeConverters.formatLongToLocaleDate(maxDateInLong)

        if (todayString == null || maxDateString == null) {
            return
        }

        contractViewModel.executeFunctionWithoutAnimation {
            if (contractViewModel.isContractsExpiring(todayString, maxDateString)) {
                setupNotification()
            }
        }
    }

    open fun setupNotification() {

        val intent = Intent(context, ExpiringContractsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0,
            intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, ConstantsVariables.EXPIRY_CHANNEL_ID_STRING)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(context.resources.getString(R.string.expiring_notification_title))
            .setContentText(context.getString(R.string.expiration_notification_message))
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setChannelId(ConstantsVariables.EXPIRY_CHANNEL_ID_STRING)
            .addAction(R.drawable.ic_eye, context.getString(R.string.see_text), pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        NotificationManagerCompat.from(context).apply {
            notify(ConstantsVariables.EXPIRY_CHANNEL_ID_STRING, ConstantsVariables.expiryChannelID_int, builder.build())
        }
    }
}