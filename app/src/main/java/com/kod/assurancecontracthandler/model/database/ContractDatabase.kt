package com.kod.assurancecontracthandler.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kod.assurancecontracthandler.Dao.ContractDAO
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.model.ContractDbDto

@Database(entities = [ContractDbDto::class], version = 1, exportSchema = true)
abstract class ContractDatabase: RoomDatabase() {

    abstract fun contractDao(): ContractDAO

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