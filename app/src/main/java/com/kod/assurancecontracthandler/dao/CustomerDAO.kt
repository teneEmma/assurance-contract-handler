package com.kod.assurancecontracthandler.dao

import androidx.room.Dao
import androidx.room.Query
import com.kod.assurancecontracthandler.model.Customer

@Dao
interface CustomerDAO {
    @Query("SELECT DISTINCT assure,telephone\n" +
            "FROM (SELECT * from contract \n" +
            "WHERE assure\n" +
            "NOT IN('DTA', 'SOMME', 'TOTAL', 'PRIME A REVERSER'))\n" +
            "WHERE assure IS NOT NULL\n" +
            "ORDER BY assure")
    fun getAllCustomers(): List<Customer>

    @Query("SELECT count(DISTINCT assure) from contract WHERE assure is not NULL")
    fun getNumberOfCustomers(): Int

    @Query("SELECT telephone FROM contract WHERE assure like :name AND telephone is NOT NULL")
    fun getCustomersPhoneNumbers(name: String): List<String?>

    @Query("SELECT count(DISTINCT assure) from contract WHERE telephone is not NULL")
    fun getNumberOfCustomersWithPhones(): Int

    @Query("SELECT count(assure) from contract Where assure is not NULL AND echeance < :today")
    fun getActiveContracts(today: String): Int

    //TODO: Add the ("OR") part for the customer's number
    @Query("SELECT DISTINCT assure,telephone  FROM contract WHERE assure LIKE :concatenatedName " +
            "ORDER BY assure")
    fun getAllCustomers(concatenatedName: String): List<Customer>
}