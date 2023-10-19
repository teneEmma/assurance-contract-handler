package com.kod.assurancecontracthandler.views.main.fragmentListCustomers

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.databinding.FragmentListCustomersBinding
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.CustomerRepository
import com.kod.assurancecontracthandler.viewmodels.customerlistviewmodel.CustomerListViewModel
import com.kod.assurancecontracthandler.viewmodels.customerlistviewmodel.CustomerListViewModelFactory
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity
import com.kod.assurancecontracthandler.views.settings.SettingsActivity

class ListCustomersFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentListCustomersBinding
    private lateinit var rvAdapter: ListCustomersAdapter
    private lateinit var sortImage: ImageView
    private val customerListViewModel by viewModels<CustomerListViewModel> {
        val customerDao = ContractDatabase.getDatabase(requireContext()).customerDao()
        val customerRepository = CustomerRepository(customerDao)
        CustomerListViewModelFactory(customerRepository)
    }

    companion object {
        fun newInstance() = ListCustomersFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (!customerListViewModel.searchViewState && !customerListViewModel.isSortIconSelected) {
            updateAllData()
            return
        }
        this.onQueryTextSubmit(customerListViewModel.searchText)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_list_customers, menu)
        sortImage = menu.findItem(R.id.action_sort).actionView as ImageView
        sortImage.isSelected = customerListViewModel.isSortIconSelected
        sortImage.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.filter_selector
            )
        )
        sortImage.apply {
            setOnClickListener {
                customerListViewModel.toggleSortIconState()
                isSelected = customerListViewModel.isSortIconSelected
                if (customerListViewModel.isSortIconSelected) {
                    shortToast(resources.getString(R.string.sort_applied))
                    customerListViewModel.sortWithNumbers()
                } else {
                    customerListViewModel.fetchCustomers()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings_customers) {
            val intent = Intent(activity, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSearchView()
        setupRecyclerView()
        setupSwipeToRefresh()

        customerListViewModel.customerList.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                binding.ivEmptyDatabase.visibility = View.GONE
                binding.tvEmptyDatabase.visibility = View.GONE
                binding.rvListCustomers.visibility = View.VISIBLE
                rvAdapter.setCustomerList(list)
            } else {
                if (!customerListViewModel.noResultFound) {
                    rvAdapter.setCustomerList(emptyList())
                    shortSnack(resources.getString(R.string.no_result_found))
                    return@observe
                }
                binding.ivEmptyDatabase.visibility = View.VISIBLE
                binding.tvEmptyDatabase.visibility = View.VISIBLE
                binding.rvListCustomers.visibility = View.GONE
            }
        }

        var tvString: String
        customerListViewModel.numbCustomers.observe(viewLifecycleOwner) { value ->
            tvString = "<font color=#FFFFFF>${resources.getString(R.string.total_text)}: $value</font> "
            binding.totalCustomers.text = Html.fromHtml(
                tvString,
                Html.FROM_HTML_OPTION_USE_CSS_COLORS
            )
        }

        customerListViewModel.numbCustomersWithPhones.observe(viewLifecycleOwner) { value ->
            tvString = "<font color=#FED300>${resources.getString(R.string.numbers_text)}: $value</font> "
            binding.totalHasNumbers.text = Html.fromHtml(
                tvString,
                Html.FROM_HTML_OPTION_USE_CSS_COLORS
            )
        }

        customerListViewModel.activeContracts.observe(viewLifecycleOwner) { value ->
            tvString = "<font color=#08FE00>${resources.getString(R.string.active_text)}: $value </font> "
            binding.totalActive.text = Html.fromHtml(
                tvString,
                Html.FROM_HTML_OPTION_USE_CSS_COLORS
            )
        }

        binding.customerDetails.setOnClickListener {
            shortToast(getString(R.string.customer_details_cardview))
        }
    }

    private fun setupSwipeToRefresh() {
        val arrowColor = MaterialColors.getColor(requireContext(), androidx.appcompat.R.attr.colorPrimary, Color.GREEN)
        val backgroundColor =
            MaterialColors.getColor(requireContext(), androidx.appcompat.R.attr.colorAccent, Color.WHITE)
        binding.swipeToRefresh.setProgressBackgroundColorSchemeColor(backgroundColor)
        binding.swipeToRefresh.setColorSchemeColors(arrowColor)
        binding.swipeToRefresh.setOnRefreshListener {
            if (!customerListViewModel.searchViewState) {
                updateAllData()
            }
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun updateAllData() {
        customerListViewModel.fetchCustomers()
        customerListViewModel.validNumbCustomers()
        customerListViewModel.validNumbCustomersWithPhones()
        customerListViewModel.getActiveContracts()
    }

    private fun setupRecyclerView() {
        rvAdapter = ListCustomersAdapter { customer ->
            openCustomerDetailsPage(customer)
        }
        binding.rvListCustomers.adapter = rvAdapter
        binding.rvListCustomers.layoutManager = LinearLayoutManager(context)
        binding.rvListCustomers.setHasFixedSize(true)
    }

    private fun openCustomerDetailsPage(customer: Customer) {
        val intent = Intent(activity, CustomerDetailsActivity::class.java)
        intent.putExtra(ConstantsVariables.customerNameKey, customer.customerName)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun setupSearchView() {
        binding.searchViewClient.queryHint = resources.getString(R.string.search_bar_query_hint)
        binding.searchViewClient.isSubmitButtonEnabled = false
        binding.searchViewClient.background = AppCompatResources.getDrawable(requireContext(), R.drawable.elevated_zone)
        binding.searchViewClient.setIconifiedByDefault(false)
        binding.searchViewClient.setOnQueryTextListener(this)
        binding.searchViewClient.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.searchViewClient.isActivated = true
                customerListViewModel.setSearchBarState(true)
            }
        }
        binding.searchViewClient.findViewById<ImageView>(com.google.android.material.R.id.search_close_btn).apply {
            setOnClickListener {
                binding.searchViewClient.findViewById<TextView>(com.google.android.material.R.id.search_src_text).text =
                    ""
            }
        }
        binding.btnDismissSearch.setOnClickListener {
            binding.searchViewClient.isActivated = false
            customerListViewModel.setSearchBarState(false)
            binding.searchViewClient.findViewById<TextView>(com.google.android.material.R.id.search_src_text).text =
                ""
        }
    }

    private fun shortToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun shortSnack(message: String) {
        Snackbar.make(requireContext(), requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onQueryTextSubmit(name: String?): Boolean {
        if (name != null) {
            customerListViewModel.fetchCustomersWithName(name)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            customerListViewModel.fetchCustomersWithName(newText)
        }
        return true
    }
}