package com.kod.assurancecontracthandler.views.expiringactivity

import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables.INTENT_LIST_WORKER
import com.kod.assurancecontracthandler.databinding.ActivityExpiringContractsBinding
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity

class ExpiringContractsActivity : AppCompatActivity() {
    lateinit var binding: ActivityExpiringContractsBinding
    private var listContracts = listOf<Customer>()
    private var oldView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpiringContractsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        retrieveData()?.let { listContracts = it }

        val adapter = ExpiringContractAdapter(listContracts,
            {customer->
            val intent = Intent(this, CustomerDetailsActivity::class.java)
            intent.putExtra(ConstantsVariables.customerKey, customer)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }, {view ->
            when(oldView){
                null-> {
                    oldView = view
                    view.findViewById<LinearLayout>(R.id.ll_contract_clicked)?.visibility = View.VISIBLE
                }
                view ->{
                    if (oldView?.findViewById<LinearLayout>(R.id.ll_contract_clicked)?.isVisible == true)
                        oldView?.findViewById<LinearLayout>(R.id.ll_contract_clicked)?.visibility = View.GONE
                    else oldView?.findViewById<LinearLayout>(R.id.ll_contract_clicked)?.visibility = View.VISIBLE
                }
                else ->{
                    oldView = view
                    oldView?.findViewById<LinearLayout>(R.id.ll_contract_clicked)?.visibility = View.GONE
                    view.findViewById<LinearLayout>(R.id.ll_contract_clicked)?.visibility = View.VISIBLE
                }

            }
        })
        binding.rvSlidersWorker.adapter = adapter
        binding.rvSlidersWorker.layoutManager = LinearLayoutManager(applicationContext)
        binding.rvSlidersWorker.setHasFixedSize(true)
    }

    private fun retrieveData(): ArrayList<Customer>? {
        return if (Build.VERSION.SDK_INT >= TIRAMISU)
            intent.getParcelableArrayListExtra(INTENT_LIST_WORKER, Customer::class.java)
        else
            intent.getParcelableArrayListExtra(INTENT_LIST_WORKER)
    }
}