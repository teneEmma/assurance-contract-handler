package com.kod.assurancecontracthandler.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kod.assurancecontracthandler.Dao.ContractDAO
import com.kod.assurancecontracthandler.Dao.CustomerDAO
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.model.Customer

@Database(entities = [ContractDbDto::class], version = 1, exportSchema = true)
@TypeConverters(TimeConverters::class)
abstract class ContractDatabase: RoomDatabase() {

    abstract fun contractDao(): ContractDAO

    abstract fun customerDao(): CustomerDAO
    companion object{
        @Volatile
        private var INSTANCE: ContractDatabase? = null
        fun getDatabase(context: Context): ContractDatabase{
            if(INSTANCE == null){
                synchronized(this){
                    INSTANCE = buildDatabase(context)
                }
            }
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): ContractDatabase{
            return Room.databaseBuilder(context, ContractDatabase::class.java, "contract_database")
                .build()
        }
    }
}