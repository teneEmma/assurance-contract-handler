package com.kod.assurancecontracthandler.views.fragments.home.listcustomers

import android.content.Intent
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.databinding.FragmentListCustomersBinding
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.viewmodels.customerviewmodel.CustomerViewModel
import com.kod.assurancecontracthandler.viewmodels.customerviewmodel.CustomerViewModelFactory
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity

class ListCustomersFragment : Fragment(), SearchView.OnQueryTextListener {

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
        setupSearchView()
        setRecyclerView()
        return binding.root
    }

    private fun setupSearchView(){
        binding.searchViewClient.queryHint = resources.getString(R.string.search_bar_query_hint)
        binding.searchViewClient.isSubmitButtonEnabled = true
        binding.searchViewClient.background = AppCompatResources.getDrawable(requireContext(), R.drawable.elevated_zone)
        binding.searchViewClient.setIconifiedByDefault(false)
        binding.searchViewClient.setOnQueryTextListener(this)
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

        val tvString = arrayListOf<String>("","","")
        customerViewModel.numbCustomers.observe(viewLifecycleOwner){value->
            tvString[0] = "<font color=#FFFFFF>TOTAL: $value\t</font> "
            binding.totalCustomers.text = Html.fromHtml(tvString.sort().toString(),
                Html.FROM_HTML_OPTION_USE_CSS_COLORS)
        }

        customerViewModel.numbCustomersWithPhones.observe(viewLifecycleOwner){value->
            tvString[1] = "<font color=#FED300>Numéros: $value\t</font> "
            binding.totalCustomers.text = Html.fromHtml(tvString.toString(),
                Html.FROM_HTML_OPTION_USE_CSS_COLORS)
        }

        customerViewModel.activeContracts.observe(viewLifecycleOwner){value->
            tvString[2] = "<font color=#08FE00>ACTIVE: $value </font> "
            binding.totalCustomers.text = Html.fromHtml(tvString.toString(),
                Html.FROM_HTML_OPTION_USE_CSS_COLORS)
        }

        binding.customerDetails.setOnClickListener {
            toast(getString(R.string.customer_details_cardview))
        }
    }

    private fun toast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let { fetchCustomers(it) }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let { fetchCustomers(it) }
        return true
    }

    private fun fetchCustomers(name: String?){
        name?.let { customerViewModel.fetchCustomers(it) }
    }
}