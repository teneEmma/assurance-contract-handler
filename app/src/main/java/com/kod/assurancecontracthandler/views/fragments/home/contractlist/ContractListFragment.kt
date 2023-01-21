package com.kod.assurancecontracthandler.views.fragments.home.contractlist

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.RangeSlider
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
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
import java.text.SimpleDateFormat
import java.util.*


class ContractListFragment : Fragment(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener{

    private lateinit var binding: FragmentListContractsBinding
    lateinit var dbViewModel: DBViewModel
    private lateinit var filterViewModel: FilterViewModel
    private lateinit var filterBinding: FilterDialogBinding
    private lateinit var expandableAdapter: ExpandableSliderAdapter
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

    override fun onResume() {
        super.onResume()
        updateContractsList()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        filterViewModel.listContracts.observe(viewLifecycleOwner){listContracts->
            dbViewModel.setContracts(listContracts)
        }

        dbViewModel.allContracts?.observe(viewLifecycleOwner){listContracts->
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
                setRecyclerView(listContracts)
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
        swipeToRefreshCollapsed()
    }

    @Deprecated("Deprecated in ")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = resources.getString(R.string.search_bar_query_hint)
        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(this)
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                binding.fabFilterResults.visibility = View.VISIBLE
                binding.addExcelFile.visibility = View.GONE
            }else{
                binding.fabFilterResults.visibility = View.GONE
                binding.addExcelFile.visibility = View.VISIBLE
            }
        }
        searchChipsChecked()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.setOnActionExpandListener(this)
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchForClient(query, filterViewModel.searchChip)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchForClient(newText, filterViewModel.searchChip)
        }
        return true
    }

    override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
        when(p0.itemId){
            R.id.action_search->{
                binding.chipGroupSearch.invalidate()
                filterFabClicked()
                showChips(binding.chipGroupSearch, ConstantsVariables.allChips,
                    com.google.android.material.R.style.Widget_MaterialComponents_Chip_Action)
            }
        }
        return true
    }

    override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
        when(p0.itemId){
            R.id.action_search->{
                deactivateAllChips()
            }
        }
        return true
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

    private fun showChips(chipGroup: ChipGroup, listStr: List<String>, style: Int){
        listStr.forEachIndexed { index, chipName ->
            val chip = Chip(requireContext(), null, style).apply {
                id = index+1
                isCheckable = true
                if (index==0){
                    isChecked = true
                    filterViewModel.searchChip = 1
                }
                text = chipName
                isClickable =true
            }
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
        )

        filterBinding.expandableLvSliders.setAdapter(expandableAdapter)
        filterBinding.expandableLvSliders.setOnChildClickListener { _, v, groupPosition, childPosition, _ ->
            makeOthersVisible(groupPosition, childPosition, v)
            true
        }
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
                if (!checkedIds.contains(index+1) || checkedIds.contains(filterViewModel.searchChip?.minus(1))){
                    v.visibility = View.GONE
                }else{
                    v.visibility = View.VISIBLE
                }
            }
        }
        resetBtnListener()
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

    private fun makeOthersVisible(groupPosition: Int, childPosition: Int, convertView: View?){
        val view = expandableAdapter.getChildView(groupPosition, childPosition , false,  convertView, null)
        filterViewModel.childrenTouched.add(childPosition)
        expandableSBinding = ExpandableSliderItemBinding.bind(view)

        expandableSBinding?.prixSlider?.apply {
            visibility = View.VISIBLE
            if (groupPosition == 1){
                values = mutableListOf(1.0f, 30.0f)
                valueFrom = 1.0f
                valueTo = 30.0f
                stepSize = 1.0f
                expandableSBinding!!.etPriceRangeMin.text = resources.getText(R.string.puissance_min)
                expandableSBinding!!.etPriceRangeMax.text = resources.getText(R.string.puissance_max)
            }

            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus.not())
                    v.visibility = View.GONE
            }

            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
                override fun onStartTrackingTouch(slider: RangeSlider) {

                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    val minVal = slider.values[0].toInt()
                    val maxVal = slider.values[1].toInt()
                    val minStr: String
                    val maxStr: String
                    if(groupPosition == 0){
                        filterViewModel.group1SliderValues[childPosition] =
                            kotlin.Pair(minVal, maxVal)
                        minStr = minVal.toString() + ConstantsVariables.priceUnit
                        maxStr = maxVal.toString() + ConstantsVariables.priceUnit
                    }else{
                        filterViewModel.group2SliderValues[childPosition] =
                            kotlin.Pair(minVal, maxVal)
                        minStr = minVal.toString() + ConstantsVariables.powerUnit
                        maxStr = maxVal.toString() + ConstantsVariables.powerUnit
                    }
                    expandableSBinding!!.etPriceRangeMin.text = minStr
                    expandableSBinding!!.etPriceRangeMax.text = maxStr
                }
            })
        }
        expandableSBinding!!.llEtPriceRange.visibility = View.VISIBLE

    }

    private fun resetBtnListener(){
        filterBinding.btnReset.setOnClickListener {
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
    }

    private fun applyBtnListener(){
        filterBinding.btnAppliquerFiltre.setOnClickListener {
            filterViewModel.apporteur = filterBinding.etFiltrerApporteur.text.toString()
            filterViewModel.immatriculation = filterBinding.etFiltrerimmatriculation.text.toString()
            filterViewModel.attestation = filterBinding.etFiltrerAttestation.text.toString()
            filterViewModel.carteRose = filterBinding.etFiltrerCarteRose.text.toString()
            filterViewModel.compagnie = filterBinding.etFiltrerCompagnie.text.toString()
            filterViewModel.assure = filterBinding.etFiltrerAssure.text.toString()
            filterViewModel.mark = filterBinding.etFiltrerMark.text.toString()
            filterViewModel.nPolice = filterBinding.etFiltrerPolice.text.toString()

            applyFilter()
            resetBtnListener()
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
        }
    }

    private fun swipeToRefreshCollapsed(){
        binding.swipeToRefresh.setOnRefreshListener {
            dbViewModel.apply {
                executeFunWithoutAnimation { fetchAllContracts() }
            }
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun setRecyclerView(listContract: List<ContractDbDto>){
        val rvAdapter = listContract.filter {contractDbDto ->
            contractDbDto.contract?.assure != "SOMME"
        }.let { ContractListAdapter(listContracts = it, clickFunction = { contractDbDto ->
            itemLongClick(contractDbDto)
        }, touchFunction = {
            touchListener()
        })}

        binding.rvListContract.adapter = rvAdapter
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
        manageContractDetailViews(contractItemBinding, c)

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
            val intent = Intent(activity, CustomerDetailsActivity::class.java)
            intent.putExtra(ConstantsVariables.customerKey, customer)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            dialogTouchContract?.dismiss()
        }
    }

    private fun manageContractDetailViews(contractItemBinding: ContractDeetailsBinding, c: Contract?){
        contractItemBinding.assureName.text = c?.assure
        if (c?.numeroPolice.isNullOrEmpty() || c?.attestation.isNullOrEmpty()){
            val price = "${c?.DTA} XAF"
            contractItemBinding.tvGrandTotal.visibility = View.VISIBLE
            contractItemBinding.llCarStuff.visibility = View.GONE
            contractItemBinding.tvApporteur.visibility = View.GONE
            contractItemBinding.effetEcheance.visibility = View.GONE
            contractItemBinding.dividerBottom.visibility = View.GONE
            contractItemBinding.dividerEffetEcheance.visibility = View.GONE
            contractItemBinding.tvGrandTotal.text = price
            return
        }

        contractItemBinding.tvGrandTotal.visibility = View.GONE
        contractItemBinding.llCarStuff.visibility = View.VISIBLE
        contractItemBinding.tvApporteur.visibility = View.VISIBLE
        contractItemBinding.effetEcheance.visibility = View.VISIBLE
        contractItemBinding.dividerBottom.visibility = View.VISIBLE
        contractItemBinding.dividerEffetEcheance.visibility = View.VISIBLE

        val carTitles = ConstantsVariables.carDetailsTitle
        val pricesTitles = ConstantsVariables.pricesTitle
        val pricesValues = listOf(
            c?.DTA.toString(), c?.PN.toString(), c?.ACC.toString(), c?.FC.toString(),
            c?.TVA?.toString(),c?.CR.toString(), c?.PTTC?.toString(), c?.COM_PN.toString(),
            c?.COM_ACC.toString(), c?.TOTAL_COM?.toString(), c?.NET_A_REVERSER.toString(),
            c?.ENCAIS.toString(), c?.COMM_LIMBE?.toString(), c?.COMM_APPORT.toString() )
        val carValues: List<String?> = listOf(
            c?.mark, c?.immatriculation, c?.puissanceVehicule, c?.carteRose, c?.categorie?.toString(), c?.zone )
        val apporteur = "APPORTEUR: ${c?.APPORTEUR}"


        contractItemBinding.tvApporteur.text = apporteur
        contractItemBinding.dateEffet.text = c?.effet?.let {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it) }
        contractItemBinding.dateEcheance.text = c?.echeance?.let {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it) }
        contractItemBinding.gvPrices.adapter = GridViewItemAdapter(requireContext(),
            pricesTitles, pricesValues)
        contractItemBinding.gvCarStuff.adapter = GridViewItemAdapter(requireContext(),
            carTitles, carValues)

    }

    private fun addExcelFile(){
        findNavController().navigate(R.id.action_HomeFragment_to_SelectFileFragment)
    }

    private fun applyFilter(){
        dbViewModel.allContracts?.value?.let { filterViewModel.filterFields(it) }
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