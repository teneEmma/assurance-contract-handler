package com.kod.assurancecontracthandler.views.fragments.home.contractlist

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
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.RangeSlider
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.BottomDialogView
import com.kod.assurancecontracthandler.databinding.ContractDeetailsBinding
import com.kod.assurancecontracthandler.databinding.ExpandableSliderItemBinding
import com.kod.assurancecontracthandler.databinding.FilterDialogBinding
import com.kod.assurancecontracthandler.databinding.FragmentListContractsBinding
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModelFactory
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.FilterViewModel
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity
import com.kod.assurancecontracthandler.views.expiringactivity.ExpiringContractsActivity
import com.kod.assurancecontracthandler.views.settings.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


class ContractListFragment : Fragment(), SearchView.OnQueryTextListener{

    private lateinit var binding: FragmentListContractsBinding
    private lateinit var dbViewModel: DBViewModel
    private lateinit var filterViewModel: FilterViewModel
    private lateinit var filterBinding: FilterDialogBinding
    private lateinit var expandableAdapter: ExpandableSliderAdapter
    private var rvAdapter: ContractListAdapter? = null
    private lateinit var dialog: Dialog
    private var  dialogTouchContract: BottomSheetDialog? =null
    private var expandableSBinding: ExpandableSliderItemBinding? = null

    companion object {
        fun newInstance() = ContractListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListContractsBinding.inflate(inflater, container, false)
        dbViewModel = ViewModelProvider(this,
            DBViewModelFactory(requireActivity().application))[DBViewModel::class.java]
        filterViewModel = ViewModelProvider(this)[FilterViewModel::class.java]
        return binding.root
    }

    override fun onDestroyView() {
        rvAdapter = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        if (binding.chipGroupSearch.isActivated.not()) updateContractsList()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setRecyclerView()
        setupSearchView()

        filterViewModel.listContracts.observe(viewLifecycleOwner){listContracts->
            dbViewModel.setContracts(listContracts)
        }

        lifecycleScope.launchWhenStarted {
            dbViewModel.allContracts.observe(viewLifecycleOwner) {listContracts->
                if(listContracts.isNullOrEmpty()){
                    binding.ivEmptyDatabase.visibility = View.VISIBLE
                    binding.tvEmptyDatabase.visibility = View.VISIBLE
                    binding.rvListContract.visibility = View.GONE
                }else{
                    if(filterViewModel.success.value == true)
                        toast("${listContracts.size} éléments trouvés")
                    binding.ivEmptyDatabase.visibility = View.GONE
                    binding.tvEmptyDatabase.visibility = View.GONE
                    binding.rvListContract.visibility = View.VISIBLE
                    listContracts.filter {contractDbDto ->
                        contractDbDto.contract?.assure != "SOMME"
                    }.let {listContract->
                        rvAdapter?.setContractList(listContract)
                    }
                }
            }
        }

        filterViewModel.isSearching.observe(viewLifecycleOwner){isSearching->
            if (isSearching) binding.progressBar.show()
            else binding.progressBar.hide()
        }

        filterViewModel.success.observe(viewLifecycleOwner){isSuccessful->
            if(!isSuccessful) binding.tvEmptyDatabase.text = getString(R.string.no_result)
        }

        dbViewModel.hasQueried.observe(viewLifecycleOwner){
            if (it) binding.progressBar.hide()
            else binding.progressBar.show()
        }

        binding.addExcelFile.setOnClickListener {
            addExcelFile()
        }

        binding.ivEmptyDatabase.setOnClickListener {
            addExcelFile()
        }

        updateContractsList()
        swipeToRefreshAfterChipCollapse()
    }

    private fun setupSearchView(){
        binding.searchView.queryHint = resources.getString(R.string.search_bar_query_hint)
        binding.searchView.isSubmitButtonEnabled = false
        binding.searchView.background = AppCompatResources.getDrawable(requireContext(), R.drawable.elevated_zone)
        binding.searchView.setIconifiedByDefault(false)
        binding.searchView.setOnQueryTextListener(this)
        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                binding.fabFilterResults.visibility = View.VISIBLE
                binding.addExcelFile.visibility = View.GONE
                binding.chipGroupSearch.isEnabled = false
                if (binding.chipGroupSearch.isActivated.not()){
                    filterFabClicked()
                    showChips(
                        binding.chipGroupSearch, ConstantsVariables.allChips,
                        com.google.android.material.R.style.Widget_MaterialComponents_Chip_Action
                    )
                }
            }else{
                binding.fabFilterResults.visibility = View.GONE
                binding.addExcelFile.visibility = View.VISIBLE
            }
        }

        binding.searchView.findViewById<ImageView>(com.google.android.material.R.id.search_close_btn).apply {
            setOnClickListener {
                binding.searchView.
                findViewById<TextView>(com.google.android.material.R.id.search_src_text).text = ""
                deactivateAllChips()
            }
        }
        searchChipsChecked()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchForClient(query, filterViewModel.searchChip)
            filterViewModel.searchText = query
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchForClient(newText, filterViewModel.searchChip)
            filterViewModel.searchText = newText
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_exp_contracts){
            val listCustomers = arrayListOf<Customer>()
            val today = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000
            val maxDate = LocalDateTime.now().plusMonths(1).toEpochSecond(ZoneOffset.UTC)*1000
            lifecycleScope.launch(Dispatchers.IO) {
                dbViewModel.apply {
                    val result = fetchExpiringContractsIn(today, maxDate)
                    result?.forEach {
                        it.contract?.let { it1 ->
                            listCustomers.add(setCustomer(it1))
                        }
                    }
                }
                val intent = Intent(activity, ExpiringContractsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                intent.putParcelableArrayListExtra(ConstantsVariables.INTENT_LIST_WORKER, listCustomers)
                startActivity(intent)
            }
        }
        else if(item.itemId == R.id.action_settings){
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        return true
    }

    private fun setCustomer(contract: Contract): Customer{
        val customer = Customer(contract.assure, contract.telephone.toString())
        customer.immatriculation = contract.immatriculation
        customer.carteRose = contract.carteRose
        customer.effet = contract.effet?.time
        customer.echeance = contract.echeance?.time
        customer.numeroPolice = contract.numeroPolice
        customer.mark = contract.mark
        return customer
    }

    private fun filterFabClicked(){
        binding.fabFilterResults.setOnClickListener {
            filterBinding = FilterDialogBinding.inflate(layoutInflater)
            showDialog()
            filterViewModel.searchChip
                ?.let{searchChip->
                    ConstantsVariables.allChips.filterIndexed { index, _ -> index+1 != searchChip } }?.let { listChips->
                    showChips(filterBinding.chipGroupFilter, listChips,
                        com.google.android.material.R.style.Widget_MaterialComponents_Chip_Filter)
                }
            filterBinding.btnDatePicker.setOnClickListener {
                showDatePickerDialog()
            }
            setExpandableListView()
            entriesListener()
        }
    }

    private fun showDialog(){
        dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(filterBinding.root)

        val layoutParams = dialog.window?.let {window->
            setNewDialogParams(window,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT)
        }

        dialog.show()
        dialog.window?.attributes = layoutParams
    }

    private fun showChips(chipGroup: ChipGroup, listStr: List<String>, style: Int, isSearch: Boolean =false){
        listStr.forEachIndexed { index, chipName ->
            val chip = Chip(requireContext(), null, style).apply {
                id = index+1
                isCheckable = true
                if (index==0){
                    isChecked = true
                    if (isSearch) filterViewModel.searchChip = 1
                }
                text = chipName
                isClickable =true
            }
            chipGroup.isActivated = true
            chipGroup.addView(chip)
        }
    }

    private fun setNewDialogParams(window: Window, width: Int, height: Int): WindowManager.LayoutParams{
        val layoutParams = window.attributes
        layoutParams.width = width
        layoutParams.height = height
        return layoutParams
    }

    private fun setExpandableListView(){
        val groupTitleList = ConstantsVariables.expandableListsTitles
        val childDataList: HashMap<String, List<String>> = hashMapOf()

        groupTitleList.forEachIndexed { index, _ ->
            childDataList[groupTitleList[index]] = ConstantsVariables.expandableChildListTitles[index]
        }

        expandableAdapter = ExpandableSliderAdapter(requireContext(),
            groupTitleList, childDataList
        ) { groupChild, minMax ->
            if(groupChild.first == 0){
                filterViewModel.group1SliderValues[groupChild.second] =
                    kotlin.Pair(minMax.first, minMax.second)
            }else{
                filterViewModel.group2SliderValues[groupChild.second] =
                    kotlin.Pair(minMax.first, minMax.second)
            }
        }

        filterBinding.expandableLvSliders.setAdapter(expandableAdapter)
//        filterBinding.expandableLvSliders.setOnChildClickListener { parent, v, groupPosition, childPosition, _ ->
//            expandableAdapter.getChildView(groupPosition, childPosition, false, v, parent)
//            true
//        }
    }

    private fun showDatePickerDialog(){
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(resources.getString(R.string.time_interval))
            .setSelection( Pair(MaterialDatePicker.thisMonthInUtcMilliseconds(),
                MaterialDatePicker.todayInUtcMilliseconds())).build()

        dateRangePicker.show(requireActivity().supportFragmentManager, ConstantsVariables.datePickerTag)
        dateRangePicker.addOnPositiveButtonClickListener {dates->
            filterViewModel.minDate = dates.first
            filterViewModel.maxDate = dates.second
        }
    }

    private fun entriesListener() {
        val listTextViews= listOf(filterBinding.ilFiltrerAppoteur,
            filterBinding.ilFiltrerAssure, filterBinding.ilFiltrerAttestation,
            filterBinding.ilFiltrerCarteRose, filterBinding.ilFiltrerCompagnie,
            filterBinding.ilFiltrerImmatriculation, filterBinding.ilFiltrerMark,
            filterBinding.ilFiltrerPolice).filterIndexed { index, _ ->
            index+1 != filterViewModel.searchChip
        }

        if(filterBinding.chipGroupFilter.checkedChipIds.isNotEmpty()){
            filterBinding.chipGroupFilter.checkedChipIds.let {checkedChips->
                listTextViews[checkedChips[0]-1].visibility = View.VISIBLE }
        }

        filterBinding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            filterViewModel.filterChip = checkedIds
            listTextViews.forEachIndexed { index, v ->
                if (!checkedIds.contains(index+1)) v.visibility = View.GONE
                else v.visibility = View.VISIBLE
            }
        }
        filterBinding.btnReset.setOnClickListener{
            resetAllFilterData()
        }
        applyBtnListener()
    }

    private fun deactivateAllChips(){
        binding.chipGroupSearch.isActivated = false
        binding.chipGroupSearch.clearCheck()
        binding.chipGroupSearch.removeAllViews()
        updateContractsList()
    }

    private fun searchForClient(str: String, checkedChips: Int?){
        if (checkedChips == null){
            toast("Aucune palette n'a été choisie")
            return
        }
        checkedChips.let { dbViewModel.apply {
            executeFunWithAnimation { searchClient(str, it) }
        }}
    }



    private fun resetAllFilterData(){
        filterViewModel.clearData()
        filterBinding.etFiltrerAttestation.text?.clear()
        filterBinding.etFiltrerApporteur.text?.clear()
        filterBinding.etFiltrerimmatriculation.text?.clear()
        filterBinding.etFiltrerCarteRose.text?.clear()
        filterBinding.etFiltrerCompagnie.text?.clear()
        filterBinding.etFiltrerAssure.text?.clear()
        filterBinding.etFiltrerMark.text?.clear()
        filterBinding.etFiltrerPolice.text?.clear()
        filterViewModel.group1SliderValues.clear()
        filterViewModel.group2SliderValues.clear()
        filterBinding.chipGroupFilter.clearCheck()
        filterBinding.expandableLvSliders.clearChoices()
        clearExpandable()
    }

    private fun applyBtnListener(){
        filterBinding.btnAppliquerFiltre.setOnClickListener {
            filterViewModel.apporteur = filterBinding.etFiltrerApporteur.text.toString().trim()
            filterViewModel.immatriculation = filterBinding.etFiltrerimmatriculation.text.toString().trim()
            filterViewModel.attestation = filterBinding.etFiltrerAttestation.text.toString().trim()
            filterViewModel.carteRose = filterBinding.etFiltrerCarteRose.text.toString().trim()
            filterViewModel.compagnie = filterBinding.etFiltrerCompagnie.text.toString().trim()
            filterViewModel.assure = filterBinding.etFiltrerAssure.text.toString().trim()
            filterViewModel.mark = filterBinding.etFiltrerMark.text.toString().trim()
            filterViewModel.nPolice = filterBinding.etFiltrerPolice.text.toString().trim()

            applyFilter()
            resetAllFilterData()
            dialog.cancel()
        }
    }

    private fun clearExpandable() {
        expandableSBinding?.prixSlider?.invalidate()
        expandableAdapter.notifyDataSetInvalidated()
    }

    private fun searchChipsChecked(){
        binding.chipGroupSearch.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.getOrNull(0)?.let { filterViewModel.searchChip = it }
            if (filterViewModel.searchText.isNotEmpty())
                this.onQueryTextSubmit(filterViewModel.searchText)
        }
    }

    private fun swipeToRefreshAfterChipCollapse(){
        binding.swipeToRefresh.setOnRefreshListener {
            if (binding.chipGroupSearch.isActivated.not()){
                dbViewModel.apply {
                    executeFunWithoutAnimation { fetchAllContracts() }
                }
            }else this.onQueryTextSubmit(filterViewModel.searchText)
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun setRecyclerView(){
        rvAdapter = ContractListAdapter(clickFunction = { contractDbDto ->
            itemLongClick(contractDbDto)
        }, touchFunction = {
            touchListener()
        })
        binding.rvListContract.adapter = rvAdapter
        binding.rvListContract.addItemDecoration(DividerItemDecoration(requireContext(),
            DividerItemDecoration.VERTICAL))
        binding.rvListContract.layoutManager =LinearLayoutManager(context)
        binding.rvListContract.setHasFixedSize(true)
    }

    private fun touchListener(){
        dialogTouchContract?.dismiss()
    }

    private fun itemLongClick(contractDbDto: ContractDbDto) {
        val contractItemBinding = ContractDeetailsBinding.inflate(layoutInflater)
        dialogTouchContract = BottomSheetDialog(requireContext()).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(contractItemBinding.root)
        }

        val c = contractDbDto.contract
        BottomDialogView().manageContractDetailViews(contractItemBinding, c, requireContext())

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()

        dialogTouchContract?.window?.setLayout(width, height)
        dialogTouchContract?.show()

        contractItemBinding.assureName.setOnClickListener {
            if (c?.numeroPolice.isNullOrEmpty()|| c?.attestation.isNullOrEmpty()){
                toast(resources.getString(R.string.it_is_a_footer))
                return@setOnClickListener
            }
            val customer = Customer(c?.assure, c?.telephone.toString())
            customer.echeance = c?.echeance?.time
            customer.mark = c?.mark
            customer.immatriculation = c?.immatriculation
            customer.numeroPolice = c?.numeroPolice
            customer.carteRose = c?.carteRose
            customer.effet = c?.effet?.time
            val intent = Intent(activity, CustomerDetailsActivity::class.java)
            intent.putExtra(ConstantsVariables.customerKey, customer)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            dialogTouchContract?.dismiss()
        }
    }



    private fun addExcelFile(){
        findNavController().navigate(R.id.action_HomeFragment_to_SelectFileFragment)
    }

    private fun applyFilter(){
        dbViewModel.allContracts.value?.let { filterViewModel.filterFields(it) }
    }

    private fun updateContractsList(){
        dbViewModel.apply {
            executeFunWithAnimation { fetchAllContracts() }
        }
    }

    private fun toast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}