package com.kod.assurancecontracthandler.Dao

import androidx.room.Dao
import androidx.room.Query
import com.kod.assurancecontracthandler.model.Customer
import org.apache.poi.ss.formula.functions.Today

@Dao
interface CustomerDAO {

    @Query("SELECT DISTINCT assure,telephone  FROM contract  WHERE assure IS NOT NULL ORDER BY assure")
    fun getAllCustomers(): List<Customer>

    @Query("SELECT count(DISTINCT assure) from contract WHERE assure is not NULL")
    fun getNumberOfCustomers(): Int

    @Query("SELECT telephone FROM contract WHERE assure like :name AND telephone is NOT NULL")
    fun getCustomersPhoneNumbers(name: String): List<Long?>

    @Query("SELECT count(DISTINCT assure) from contract WHERE telephone is not NULL")
    fun getNumberOfCustomersWithPhones(): Int

    @Query("SELECT count(assure) from contract Where assure is not NULL AND echeance < :today")
    fun getValidContracts(today: Long): Int
}