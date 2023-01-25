package com.kod.assurancecontracthandler.common.workmanager

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables.INTENT_LIST_WORKER
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.common.utilities.DataStoreRepository
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.views.expiringactivity.ExpiringContractsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset

open class ExpirationWorker(val context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams) {
    private var dbViewModel: DBViewModel = DBViewModel(context.applicationContext as Application)
    override suspend fun doWork(): Result {
        checkExpiringContracts()
        return Result.success()
    }

    private suspend fun checkExpiringContracts(){
        val listCustomers = arrayListOf<Customer>()
        val today = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000
        val maxDate = LocalDateTime.now().plusMonths(1).toEpochSecond(ZoneOffset.UTC)*1000
        withContext(Dispatchers.IO){
            dbViewModel.apply {
                val result = fetchExpiringContractsIn(today, maxDate)
                result.forEach {
                    it.contract?.let { it1 ->
                        listCustomers.add(setCustomer(it1))
                    }
                }
            }

            if (listCustomers.isEmpty().not()){
                setupNotification(listCustomers)
            }
        }
    }

    open fun setupNotification(listContracts: List<Customer>){

        val intent = Intent(context, ExpiringContractsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putParcelableArrayListExtra(INTENT_LIST_WORKER, listContracts as ArrayList<Customer>)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0,
            intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, ConstantsVariables.expiryChannelID_str)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(ConstantsVariables.EXPIRY_NOTIFICATION_TITLE)
            .setContentText(context.getString(R.string.expiration_notification_message))
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setChannelId(ConstantsVariables.expiryChannelID_str)
            .addAction(R.drawable.ic_eye, context.getString(R.string.see), pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        NotificationManagerCompat.from(context).apply{
            notify(ConstantsVariables.expiryChannelID_str, ConstantsVariables.expiryChannelID_int, builder.build())
        }
    }

    private fun setCustomer(contract: Contract): Customer{
        val customer = Customer(contract.assure, contract.telephone.toString())
        customer.attestation = contract.attestation
        customer.carteRose = contract.carteRose
        customer.effet = contract.effet?.time
        customer.echeance = contract.echeance?.time
        customer.numeroPolice = contract.numeroPolice
        customer.mark = contract.mark
        return customer
    }
}