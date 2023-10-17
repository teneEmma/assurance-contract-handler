package com.kod.assurancecontracthandler.common.workmanager

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkerParameters
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables

class FirstUsageWorker(context: Context, params: WorkerParameters) : ExpirationWorker(context, params) {
    override suspend fun doWork(): Result {
        setupNotification()
        return Result.success()
    }

    override fun setupNotification() {
        val builder = NotificationCompat.Builder(context, ConstantsVariables.FIRST_USAGE_CHANNEL_ID_STRING)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(ConstantsVariables.WELCOME_NOTIFICATION_TITLE)
            .setContentText(context.getString(R.string.welcome_notification_message))
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setChannelId(ConstantsVariables.FIRST_USAGE_CHANNEL_ID_STRING)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        NotificationManagerCompat.from(context).apply{
            notify(ConstantsVariables.FIRST_USAGE_CHANNEL_ID_STRING, ConstantsVariables.firstTimeChannelID_int, builder.build())
        }
    }
}