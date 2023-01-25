package com.kod.assurancecontracthandler.views.fragments.home.listcustomers

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.databinding.FragmentListCustomersBinding
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.viewmodels.customerviewmodel.CustomerViewModel
import com.kod.assurancecontracthandler.viewmodels.customerviewmodel.CustomerViewModelFactory
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListCustomersFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentListCustomersBinding
    private lateinit var rvAdapter: ListCustomersAdapter
    private lateinit var customerViewModel: CustomerViewModel
    private lateinit var sortImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListCustomersBinding.inflate(inflater, container, false)
        customerViewModel = ViewModelProvider(this,
            CustomerViewModelFactory(requireActivity().application))[CustomerViewModel::class.java]
        return binding.root
    }

    private fun setupSearchView(){
        binding.searchViewClient.queryHint = resources.getString(R.string.search_bar_query_hint)
        binding.searchViewClient.isSubmitButtonEnabled = false
        binding.searchViewClient.background = AppCompatResources.getDrawable(requireContext(), R.drawable.elevated_zone)
        binding.searchViewClient.setIconifiedByDefault(false)
        binding.searchViewClient.setOnQueryTextListener(this)
        binding.searchViewClient.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if(hasFocus) binding.searchViewClient.isActivated = true
        }
        binding.searchViewClient.findViewById<ImageView>(com.google.android.material.R.id.search_close_btn).apply {
            setOnClickListener {
                binding.searchViewClient.
                findViewById<TextView>(com.google.android.material.R.id.search_src_text).text = ""
                binding.searchViewClient.isActivated = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(binding.searchViewClient.isActivated.not() &&
                sortImage.isSelected.not()) updateAllData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSearchView()
        setRecyclerView()

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

        var tvString = ""
        customerViewModel.numbCustomers.observe(viewLifecycleOwner){value->
            tvString = "<font color=#FFFFFF>TOTAL: $value</font> "
            binding.totalCustomers.text = Html.fromHtml(tvString,
                Html.FROM_HTML_OPTION_USE_CSS_COLORS)
        }

        customerViewModel.numbCustomersWithPhones.observe(viewLifecycleOwner){value->
            tvString = "<font color=#FED300>Numéros: $value</font> "
            binding.totalHasNumbers.text = Html.fromHtml(tvString,
                Html.FROM_HTML_OPTION_USE_CSS_COLORS)
        }

        customerViewModel.activeContracts.observe(viewLifecycleOwner){value->
            tvString = "<font color=#08FE00>ACTIVE: $value </font> "
            binding.totalActive.text = Html.fromHtml(tvString,
                Html.FROM_HTML_OPTION_USE_CSS_COLORS)
        }

        binding.customerDetails.setOnClickListener {
            toast(getString(R.string.customer_details_cardview))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_list_customers, menu)
        sortImage = menu.findItem(R.id.action_sort).actionView as ImageView
        sortImage.isSelected = false
        sortImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),
            R.drawable.filter_selector))
        sortImage.apply {
            setOnClickListener {
                isSelected = isSelected.not()
                if(isSelected){
                    toast(getString(R.string.sorted_toast))
                    sortAllCustomers()
                }else{
                    customerViewModel.fetchCustomers()
                }
            }
        }
    }

    private fun sortAllCustomers() {
        lifecycleScope.launch(Dispatchers.IO) {
            customerViewModel.sortWithNumbers()
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
            if(sortImage.isSelected.not()){
                updateAllData()
            }
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