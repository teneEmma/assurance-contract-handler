package com.kod.assurancecontracthandler.repository

import com.kod.assurancecontracthandler.Dao.CustomerDAO

class CustomerRepository(private val customerDAO: CustomerDAO) {

    fun getAllCustomers() = customerDAO.getAllCustomers()

    fun validNumberOfCustomers() = customerDAO.getNumberOfCustomers()

    fun getCustomerPhoneNumbers(name: String) = customerDAO.getCustomersPhoneNumbers(name)

    fun numberCustomersWithTelephone() = customerDAO.getNumberOfCustomersWithPhones()

    fun getActiveContracts(today: Long) = customerDAO.getActiveContracts(today)

    fun getAllCustomers(name: String) = customerDAO.getAllCustomers(name)
}