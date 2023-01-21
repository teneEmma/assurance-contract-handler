package com.kod.assurancecontracthandler.views.fragments.home.listcustomers

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.databinding.FragmentListCustomersBinding
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.viewmodels.customerviewmodel.CustomerViewModel
import com.kod.assurancecontracthandler.viewmodels.customerviewmodel.CustomerViewModelFactory
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity

class ListCustomersFragment : Fragment() {

    private lateinit var binding: FragmentListCustomersBinding
    private lateinit var rvAdapter: ListCustomersAdapter
    private lateinit var customerViewModel: CustomerViewModel

        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListCustomersBinding.inflate(inflater, container, false)
        customerViewModel = ViewModelProvider(this,
            CustomerViewModelFactory(requireActivity().application))[CustomerViewModel::class.java]
            setRecyclerView()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateAllData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateAllData()
        swipeToRefresh()

        customerViewModel.customerList.observe(viewLifecycleOwner){list->
            if (list.isNotEmpty()) {
                binding.ivEmptyDatabase.visibility = View.GONE
                binding.tvEmptyDatabase.visibility = View.GONE
                binding.rvListCustomers.visibility = View.VISIBLE
                rvAdapter.setCustomerList(list)
            }else{
                binding.ivEmptyDatabase.visibility = View.VISIBLE
                binding.tvEmptyDatabase.visibility = View.VISIBLE
                binding.rvListCustomers.visibility = View.GONE
            }
        }

        customerViewModel.numbCustomers.observe(viewLifecycleOwner){value->
            binding.totalCustomers.text = value.toString()
        }

        customerViewModel.numbCustomersWithPhones.observe(viewLifecycleOwner){value->
            binding.totalHasNumbers.text = value.toString()
        }

        customerViewModel.activeContracts.observe(viewLifecycleOwner){value->
            binding.totalActive.text = value.toString()
        }
    }

    companion object {
        fun newInstance() = ListCustomersFragment()
    }

    private fun setRecyclerView(){
        ListCustomersAdapter { customer->
            showCustomerDetails(customer) }.let { it2->
            rvAdapter = it2
        }
        binding.rvListCustomers.adapter = rvAdapter
        binding.rvListCustomers.addItemDecoration(DividerItemDecoration(requireContext(),
            DividerItemDecoration.VERTICAL))
        binding.rvListCustomers.layoutManager = LinearLayoutManager(context)
        binding.rvListCustomers.setHasFixedSize(true)
    }

    private fun swipeToRefresh(){
        binding.swipeToRefresh.setOnRefreshListener {
            updateAllData()
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun updateAllData(){
        customerViewModel.fetchCustomers()
        customerViewModel.validNumbCustomers()
        customerViewModel.validNumbCustomersWithPhones()
        customerViewModel.getActiveContracts()
    }

    private fun showCustomerDetails(customer: Customer){
        val intent = Intent(activity, CustomerDetailsActivity::class.java)
        intent.putExtra(ConstantsVariables.customerKey, customer)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}