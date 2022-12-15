package com.kod.assurancecontracthandler.views.fragments.home.customerdetails

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.databinding.ActivityCustomerDetailsBinding
import com.kod.assurancecontracthandler.model.Customer

class CustomerDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerDetailsBinding
    private var customer: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customer = retrieveCustomer()
        setViews()

        binding.toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked){
                when(checkedId){
                    R.id.callBtn -> snackBar("Implement Call Feature")
                    R.id.smsBtn -> snackBar("Implement SMS Feature")
                    R.id.whatsappBtn -> snackBar("Implement Whatsapp Feature")
                }
            }
        }
    }

    private fun snackBar(message: String){
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun setViews(){
        binding.tvCustomerName.text = customer?.customerName
        binding.tvCustomerNumber.text = customer?.phoneNumber
    }

    private fun retrieveCustomer(): Customer?{
        return if (Build.VERSION.SDK_INT >= 33)
            intent.getParcelableExtra("customer", Customer::class.java)
        else
            intent.getParcelableExtra("customer") as? Customer
    }
}