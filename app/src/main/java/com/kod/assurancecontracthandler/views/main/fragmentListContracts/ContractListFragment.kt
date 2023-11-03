package com.kod.assurancecontracthandler.views.main.fragmentListContracts

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.usecases.DateType
import com.kod.assurancecontracthandler.common.utilities.BottomDialogView
import com.kod.assurancecontracthandler.common.utilities.SimpleItemTouchCallback
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.databinding.ContractDetailsBinding
import com.kod.assurancecontracthandler.databinding.FilterDialogBinding
import com.kod.assurancecontracthandler.databinding.FragmentListContractsBinding
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.contractListViewModel.ContractListViewModel
import com.kod.assurancecontracthandler.viewmodels.contractListViewModel.ContractListViewModelFactory
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity
import com.kod.assurancecontracthandler.views.expiringactivity.ExpiringContractsActivity
import com.kod.assurancecontracthandler.views.settings.SettingsActivity

class ContractListFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentListContractsBinding
    private lateinit var filterDialogBinding: FilterDialogBinding
    private lateinit var dialog: Dialog
    private lateinit var expandableAdapter: ExpandableSliderAdapter

    private val contractListViewModel by viewModels<ContractListViewModel> {
        val contractDao = ContractDatabase.getDatabase(requireContext()).contractDao()
        val contractRepository = ContractRepository(contractDao)
        val groupTitleList = resources.getStringArray(R.array.expandable_group_titles_list).toList()
        val childrenTitleList = listOf(
            resources.getStringArray(R.array.expandable_children_titles_list_1).toList(),
            resources.getStringArray(R.array.expandable_children_titles_list_2).toList(),
            resources.getStringArray(R.array.expandable_children_titles_list_3).toList()
        )
        ContractListViewModelFactory(contractRepository, groupTitleList, childrenTitleList)
    }
    private var rvAdapter: ContractListAdapter? = null
    private var dialogTouchContract: BottomSheetDialog? = null

    companion object {
        fun newInstance() = ContractListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListContractsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (contractListViewModel.selectedSearchChip == null) {
            updateContractsList()
            return
        }

        this.onQueryTextSubmit(contractListViewModel.searchText)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setRecyclerView()
        setupSearchBarView()

        binding.addExcelFile.setOnClickListener {
            openAddExcelFilePage()
        }

        binding.ivEmptyDatabase.setOnClickListener {
            openAddExcelFilePage()
        }

        lifecycleScope.launchWhenStarted {
            contractListViewModel.allContracts.observe(viewLifecycleOwner) { listContracts ->
                if (listContracts.isNullOrEmpty() && contractListViewModel.searchText.isEmpty() && contractListViewModel.selectedSearchChip == null) {
                    binding.ivEmptyDatabase.visibility = View.VISIBLE
                    binding.tvEmptyDatabase.visibility = View.VISIBLE
                    binding.rvListContract.visibility = View.GONE
                } else {
                    binding.ivEmptyDatabase.visibility = View.GONE
                    binding.tvEmptyDatabase.visibility = View.GONE
                    binding.rvListContract.visibility = View.VISIBLE
                    if (listContracts != null) {
                        rvAdapter?.setContractList(listContracts)
                    }
                }
            }
        }

        contractListViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.show()
            } else {
                binding.progressBar.hide()
            }
        }
        contractListViewModel.messageResourceId.observe(viewLifecycleOwner) { resourceId ->
            shortSnack(resources.getString(resourceId))
        }

        updateContractsList()
        swipeToRefreshAfterChipCollapse()
    }

    private fun setupSearchBarView() {
        binding.searchView.isSubmitButtonEnabled = false
        binding.searchView.setIconifiedByDefault(false)
        binding.searchView.setOnQueryTextListener(this)
        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                return@setOnQueryTextFocusChangeListener
            }
            binding.chipGroupSearch.isEnabled = false
            if (!binding.chipGroupSearch.isActivated) {
                onClickFilterFab()
                showChips(
                    binding.chipGroupSearch,
                    ConstantsVariables.searchBarChipsTitles,
                    com.google.android.material.R.style.Widget_MaterialComponents_Chip_Action
                )
            }
        }
        binding.btnDismissSearch.setOnClickListener {
            deactivateAllSearchChips()
        }
        contractListViewModel.shouldDisplayFabFilter.observe(viewLifecycleOwner) { containsSomeText ->
            if (containsSomeText) {
                binding.fabFilterDialog.visibility = View.VISIBLE
                binding.addExcelFile.visibility = View.GONE
                return@observe
            }
            binding.fabFilterDialog.visibility = View.GONE
            binding.addExcelFile.visibility = View.VISIBLE
        }

        binding.searchView.findViewById<ImageView>(com.google.android.material.R.id.search_close_btn)
            .setOnClickListener {
                binding.searchView.findViewById<TextView>(com.google.android.material.R.id.search_src_text).text = ""
            }
        searchChipsChecked()
    }

    private fun onClickFilterFab() {
        binding.fabFilterDialog.setOnClickListener {
            filterDialogBinding = FilterDialogBinding.inflate(layoutInflater)
            showFilterDialog()
            showChips(
                filterDialogBinding.chipGroupFilter,
                contractListViewModel.filterChipsToShow,
                com.google.android.material.R.style.Widget_MaterialComponents_Chip_Filter
            )

            filterDialogBinding.btnPickStartDate.setOnClickListener {
                showDatePickerDialog(resources.getString(R.string.select_start_date), DateType.START_DATE)
            }
            filterDialogBinding.btnPickDueDate.setOnClickListener {
                showDatePickerDialog(resources.getString(R.string.select_due_date), DateType.DUE_DATE)
            }
            setupExpandableListView()
            filterChipsChecked()
        }
    }

    private fun filterChipsChecked() {
        // This order of appearance of textInputLayouts in this list has been order with respect to
        // ConstantsVariables.filterDialogChips
        val listTextInputLayoutsViews = listOf(
            filterDialogBinding.ilFilterApporteur,
            filterDialogBinding.ilFilterAssurer,
            filterDialogBinding.ilFilterAttestation,
            filterDialogBinding.ilFilterCarteRose,
            filterDialogBinding.ilFilterCategory,
            filterDialogBinding.ilFilterCompagnie,
            filterDialogBinding.ilFilterImmatriculation,
            filterDialogBinding.ilFilterMark,
            filterDialogBinding.ilFilterNumeroPolice
        )
        val listEditTextViews = listOf(
            filterDialogBinding.etFilterApporteur,
            filterDialogBinding.etFilterAssurer,
            filterDialogBinding.etFilterAttestation,
            filterDialogBinding.etFilterCarteRose,
            filterDialogBinding.etFilterCategory,
            filterDialogBinding.etFilterCompagnie,
            filterDialogBinding.etFilterImmatriculation,
            filterDialogBinding.etFilterMark,
            filterDialogBinding.etFilterPolice
        )

        if (filterDialogBinding.chipGroupFilter.checkedChipIds.isNotEmpty()) {
            filterDialogBinding.chipGroupFilter.checkedChipIds.let { checkedChips ->
                listTextInputLayoutsViews[checkedChips[0] - 1].visibility = View.VISIBLE
            }
        }

        filterDialogBinding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            contractListViewModel.onFilterChipCheckChanged(checkedIds)
            listTextInputLayoutsViews.forEachIndexed { index, v ->
                if (!checkedIds.contains(index)) {
                    v.visibility = View.GONE
                    return@forEachIndexed
                }
                v.visibility = View.VISIBLE
            }
        }
        filterDialogBinding.btnReset.setOnClickListener {
            onResetBtnClicked(listEditTextViews)
        }
        applyBtnListener(listEditTextViews)
    }

    private fun applyBtnListener(listEditText: List<TextInputEditText>) {

        filterDialogBinding.btnApplyFilter.setOnClickListener {
            contractListViewModel.setEditTextValues(listEditText)

            contractListViewModel.filterContracts()
            onResetBtnClicked(listEditText)
            dialog.cancel()
        }
    }

    private fun onResetBtnClicked(listEditText: List<TextInputEditText>) {
        contractListViewModel.clearData()
        listEditText.forEach {
            it.text?.clear()
        }
        filterDialogBinding.chipGroupFilter.clearCheck()
        filterDialogBinding.expandableLvSliders.clearChoices()
        filterDialogBinding.expandableLvSliders.invalidate()
        expandableAdapter.notifyDataSetInvalidated()
        setDatesTextViewsValues()
    }

    private fun setupExpandableListView() {
        val groupTitleList = resources.getStringArray(R.array.expandable_group_titles_list).toList()
        val childrenTitleList = listOf(
            resources.getStringArray(R.array.expandable_children_titles_list_1).toList(),
            resources.getStringArray(R.array.expandable_children_titles_list_2).toList(),
            resources.getStringArray(R.array.expandable_children_titles_list_3).toList()
        )
        expandableAdapter = ExpandableSliderAdapter(
            requireContext(),
            groupTitleList,
            childrenTitleList,
            contractListViewModel.slidersValues,
            sliderListenerCallback = { groupChildPositions, minMaxValues ->
                contractListViewModel.onSliderTouched(groupChildPositions, minMaxValues)
            },
        )

        filterDialogBinding.expandableLvSliders.setAdapter(expandableAdapter)
    }

    private fun showFilterDialog() {
        dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(filterDialogBinding.root)

        val layoutParams = dialog.window?.let { window ->
            setNewDialogParams(window)
        }

        dialog.show()
        dialog.window?.attributes = layoutParams
    }

    private fun setNewDialogParams(window: Window): WindowManager.LayoutParams {
        val layoutParams = window.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        return layoutParams
    }

    private fun showDatePickerDialog(calendarTitle: String, dateType: DateType) {
        val dateRangePicker =
            MaterialDatePicker.Builder.datePicker().setTitleText(calendarTitle)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()

        dateRangePicker.show(requireActivity().supportFragmentManager, ConstantsVariables.datePickerTag)
        dateRangePicker.addOnPositiveButtonClickListener { date ->
            if (dateType == DateType.START_DATE) {
                contractListViewModel.onDateChanged(minDate = date, maxDate = null)
            } else {
                contractListViewModel.onDateChanged(minDate = null, maxDate = date)
            }
            setDatesTextViewsValues()
        }
    }

    private fun setDatesTextViewsValues() {
        filterDialogBinding.startDate.text = TimeConverters.formatLongToLocaleDate(contractListViewModel.startDate)
            ?: resources.getString(R.string.start_date_text)
        filterDialogBinding.endDate.text = TimeConverters.formatLongToLocaleDate(contractListViewModel.endDate)
            ?: resources.getString(R.string.end_date_text)
    }

    private fun deactivateAllSearchChips() {
        binding.searchView.findViewById<TextView>(com.google.android.material.R.id.search_src_text).text = ""
        binding.chipGroupSearch.isActivated = false
        binding.chipGroupSearch.clearCheck()
        binding.chipGroupSearch.removeAllViews()
        contractListViewModel.deactivateAllSearchChips()
        updateContractsList()
    }

    private fun searchChipsChecked() {
        binding.chipGroupSearch.setOnCheckedStateChangeListener { _, checkedIds ->
            contractListViewModel.onSearchChipCheckChanged(checkedIds.getOrNull(0))
        }
    }

    private fun showChips(chipGroup: ChipGroup, searchChipGroupTitles: List<String>, chipGroupStyle: Int) {
        searchChipGroupTitles.forEachIndexed { index, chipName ->
            val chip = Chip(requireContext(), null, chipGroupStyle).apply {
                id = index
                isCheckable = true
                text = chipName
                isClickable = true
                setChipBackgroundColorResource(R.color.chip_background_color)
                setChipStrokeColorResource(R.color.chip_stroke_color)
                setTextColor(ContextCompat.getColorStateList(context, R.color.chip_text_color))
                chipStrokeWidth = 10f
                setCheckedIconTintResource(R.color.chip_text_color)
            }
            chipGroup.isActivated = true
            chipGroup.addView(chip)
        }
    }

    private fun openAddExcelFilePage() {
        findNavController().navigate(R.id.action_HomeFragment_to_SelectFileFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onDestroyView() {
        rvAdapter = null
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_exp_contracts) {
            val intent = Intent(activity, ExpiringContractsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            startActivity(intent)
        } else if (item.itemId == R.id.action_settings) {
            val intent = Intent(activity, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        return true
    }

    private fun shortSnack(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun swipeToRefreshAfterChipCollapse() {
        val arrowColor = MaterialColors.getColor(requireContext(), androidx.appcompat.R.attr.colorPrimary, Color.GREEN)
        val backgroundColor =
            MaterialColors.getColor(requireContext(), androidx.appcompat.R.attr.colorAccent, Color.WHITE)
        binding.swipeToRefresh.setProgressBackgroundColorSchemeColor(backgroundColor)
        binding.swipeToRefresh.setColorSchemeColors(arrowColor)
        binding.swipeToRefresh.setOnRefreshListener {
            if (!binding.chipGroupSearch.isActivated) {
                contractListViewModel.apply {
                    executeFunctionWithoutAnimation { contractListViewModel.fetchAllContracts() }
                }
            } else {
                this.onQueryTextSubmit(contractListViewModel.searchText)
            }
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun setRecyclerView() {
        rvAdapter = ContractListAdapter(clickFunction = { baseContract ->
            itemLongClick(baseContract)
        }, touchFunction = {
            touchListener()
        })
        binding.rvListContract.adapter = rvAdapter
        binding.rvListContract.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListContract.setHasFixedSize(true)
        ItemTouchHelper(
            SimpleItemTouchCallback(
                requireContext(),
                rvAdapter!!,
                onSwipeCallback = { idClicked ->
                    contractListViewModel.exportContractToFile(
                        idClicked,
                        requireContext().assets,
                        requireContext().filesDir
                    )
                }
            )
        ).attachToRecyclerView(binding.rvListContract)
    }

    private fun touchListener() {
        dialogTouchContract?.dismiss()
    }

    private fun itemLongClick(baseContract: BaseContract) {
        val contractItemBinding = ContractDetailsBinding.inflate(layoutInflater)
        dialogTouchContract = BottomSheetDialog(requireContext()).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(contractItemBinding.root)
        }

        val c = baseContract.contract
        val carDetailsListTitles = resources.getStringArray(R.array.car_details_title).toList()
        val priceDetailsListTitles = resources.getStringArray(R.array.price_details_title).toList()
        BottomDialogView(
            carDetailsListTitles,
            priceDetailsListTitles,
            resources.getString(R.string.provider_text),
        ).manageContractDetailViews(
            contractItemBinding, c, requireContext()
        )

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()

        dialogTouchContract?.window?.setLayout(width, height)
        dialogTouchContract?.show()

        contractItemBinding.assurerName.setOnClickListener {
            if (c?.numeroPolice.isNullOrEmpty() || c?.attestation.isNullOrEmpty()) {
                toast(resources.getString(R.string.non_valid_option))
                return@setOnClickListener
            }
            openCustomerDetailsActivity(c?.assure, baseContract.id)
            dialogTouchContract?.dismiss()
        }
    }

    private fun openCustomerDetailsActivity(assurerName: String?, contractId: Int) {
        Log.e("SENDING...", "-> $assurerName | $contractId")
        val intent = Intent(activity, CustomerDetailsActivity::class.java)
        intent.putExtra(ConstantsVariables.customerNameKey, assurerName)
        intent.putExtra(ConstantsVariables.relatedContractIdKey, contractId)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        dialogTouchContract?.dismiss()
    }

    private fun updateContractsList() {
        contractListViewModel.apply {
            executeFunctionWithAnimation {
                fetchAllContracts()
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            contractListViewModel.onSearchText(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            contractListViewModel.onSearchText(newText)
        }
        return true
    }
}