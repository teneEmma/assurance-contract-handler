package com.kod.assurancecontracthandler.views.fragments.home.contractlist

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.databinding.ContractDeetailsBinding
import com.kod.assurancecontracthandler.databinding.FilterDialogBinding
import com.kod.assurancecontracthandler.databinding.FragmentSecondBinding
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModelFactory
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.FilterViewModel
import com.kod.assurancecontracthandler.views.fragments.home.customerdetails.CustomerDetailsActivity
import java.text.SimpleDateFormat
import java.util.*


class ContractListFragment : Fragment(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener{

    private lateinit var binding: FragmentSecondBinding
    private lateinit var dbViewModel: DBViewModel
    private lateinit var filterViewModel: FilterViewModel
    private lateinit var filterBinding: FilterDialogBinding
    private lateinit var expandableAdapter: ExpandableSliderAdapter
    lateinit var dialog: Dialog
    private val actionSearchExpanded = MutableLiveData<Boolean>()
    private var listContracts = MutableLiveData<List<ContractDbDto>?>()
    private var  dialogTouchContract: BottomSheetDialog? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSecondBinding.inflate(inflater, container, false)
        dbViewModel = ViewModelProvider(this,
            DBViewModelFactory(requireActivity().application))[DBViewModel::class.java]
        listContracts = dbViewModel.allContracts as MutableLiveData<List<ContractDbDto>?>
        filterViewModel = ViewModelProvider(this)[FilterViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        listContracts.observe(viewLifecycleOwner){listContracts->
            if(listContracts.isNullOrEmpty()){
                binding.ivEmptyDatabase.visibility = View.VISIBLE
                binding.tvEmptyDatabase.visibility = View.VISIBLE
                binding.rvListContract.visibility = View.GONE
            }else{
                binding.ivEmptyDatabase.visibility = View.GONE
                binding.tvEmptyDatabase.visibility = View.GONE
                binding.rvListContract.visibility = View.VISIBLE
                setRecyclerView()
            }
        }

        filterViewModel.isSearching.observe(viewLifecycleOwner){isSearching->
            if (isSearching)
                binding.progressCircular.show()
            else
                binding.progressCircular.hide()
        }

        actionSearchExpanded.observe(viewLifecycleOwner){expanded->
            if (expanded){
                binding.filterResults.visibility = View.VISIBLE
                binding.addExcelFile.visibility = View.GONE
            }else{
                binding.filterResults.visibility = View.GONE
                binding.addExcelFile.visibility = View.VISIBLE
            }
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
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)
        searchChipChecked()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.setOnActionExpandListener(this)
        return true
    }

    companion object {
        fun newInstance() = ContractListFragment()
    }

    private fun filterFabClicked(){
        binding.filterResults.setOnClickListener {
            filterBinding = FilterDialogBinding.inflate(layoutInflater)
            showDialog()
            filterViewModel.searchChip
                ?.let{searchChip-> ConstantsVariables.allChips.filterIndexed { index, _ -> index+1 != searchChip } }?.let { listChips->
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
            expandableAdapter.makeOthersVisible(groupPosition, childPosition, v)
            true
        }
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
            filterViewModel.prixSlidersValue?.clear()
            filterViewModel.puissanceSliderValue?.clear()
            filterBinding.chipGroupFilter.clearCheck()
            filterBinding.expandableLvSliders.clearChoices()
        }
    }

    private fun applyBtnListener(){
        filterBinding.btnAppliquerFiltre.setOnClickListener {
            Log.e("PRICE SLIDERS", expandableAdapter.getSliderValuesGrp1().toString())
            filterViewModel.apporteur = filterBinding.etFiltrerApporteur.text.toString()
            filterViewModel.immatriculation = filterBinding.etFiltrerimmatriculation.text.toString()
            filterViewModel.attestation = filterBinding.etFiltrerAttestation.text.toString()
            filterViewModel.carteRose = filterBinding.etFiltrerCarteRose.text.toString()
            filterViewModel.compagnie = filterBinding.etFiltrerCompagnie.text.toString()
            filterViewModel.assure = filterBinding.etFiltrerAssure.text.toString()
            filterViewModel.mark = filterBinding.etFiltrerMark.text.toString()
            filterViewModel.nPolice = filterBinding.etFiltrerPolice.text.toString()
            filterViewModel.prixSlidersValue = expandableAdapter.getSliderValuesGrp1()
            filterViewModel.puissanceSliderValue = expandableAdapter.getSliderValuesGrp2()
            applyFilter()
            dialog.cancel()
        }
    }

    private fun entriesListener() {
        val listTextViews= listOf(filterBinding.ilFiltrerAppoteur,
            filterBinding.ilFiltrerAssure, filterBinding.ilFiltrerAttestation,
            filterBinding.ilFiltrerCarteRose, filterBinding.ilFiltrerCompagnie,
            filterBinding.ilFiltrerImmatriculation, filterBinding.ilFiltrerMark,
            filterBinding.ilFiltrerPolice)
        filterBinding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            Log.e("CHECKED IDS", checkedIds.toString())
            filterViewModel.filterChip = checkedIds
            listTextViews.forEachIndexed { index, v ->
                if (!checkedIds.contains(index) || checkedIds.contains(filterViewModel.searchChip?.minus(1))){
                    v.visibility = View.GONE
                }else{
                    v.visibility = View.VISIBLE
                }
            }
        }
        resetBtnListener()
        applyBtnListener()
    }

    private fun showDatePickerDialog(){
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(resources.getString(R.string.time_interval))
            .setSelection(
                Pair(MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds())
            ).build()

        dateRangePicker.show(requireActivity().supportFragmentManager, ConstantsVariables.datePickerTag)
        dateRangePicker.addOnPositiveButtonClickListener {dates->
//            Log.e("DATE_RANGE_PICKER", dateRangePicker.headerText)
//            Log.e("IT_RANGE_PICKER", "${ dates.first } && ${dates.second}")
            filterViewModel.minDate = dates.first
            filterViewModel.maxDate = dates.second
        }
    }

    private fun setNewDialogParams(window: Window, width: Int, height: Int): WindowManager.LayoutParams{
        val layoutParams = window.attributes
        layoutParams.width = width
        layoutParams.height = height
        return layoutParams
    }

    private fun showChips(chipGroup: ChipGroup, listStr: List<String>, style: Int){
        listStr.forEachIndexed { index, chipName ->
            val chip = Chip(requireContext(), null, style)
            chip.id = index+1
            chip.isCheckable = true
            if (index==0){
                chip.isChecked = true
            }
            chip.text = chipName
            chip.isClickable =true
            chipGroup.addView(chip)
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

    private fun searchForClient(str: String, checkedChips: Int?){
//        swipeToRefresh(str)
        if (checkedChips == null){
            toast("La palette n'a pas été choisie")
            return
        }
        checkedChips.let { dbViewModel.searchClient(str, it) }
    }

    private fun toast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun searchChipChecked(){
        binding.chipGroupSearch.setOnCheckedStateChangeListener { _, checkedIds ->
//            Log.e("checkedIds ", "$checkedIds ${ConstantsVariables.allChips}")
            if (checkedIds.size > 0) {
                filterViewModel.searchChip = checkedIds[0]
                return@setOnCheckedStateChangeListener
            }
            filterViewModel.searchChip = null
        }
    }

    private fun updateContractsList(){
        dbViewModel.fetchAllContracts()
    }

    private fun swipeToRefreshCollapsed(){
        binding.swipeToRefresh.setOnRefreshListener {
            updateContractsList()
            binding.swipeToRefresh.isRefreshing = false
        }
    }

//    private fun swipeToRefresh(str: String){
//        binding.swipeToRefresh.setOnRefreshListener {
//            searchForClient(str, filterViewModel.searchChip)
//            binding.swipeToRefresh.isRefreshing = false
//        }
//    }

    private fun setRecyclerView(){
        val rvAdapter = listContracts.value?.filter {contractDbDto ->
            contractDbDto.contract?.assure != "SOMME"
        }?.let { ContractListAdapter(listContracts = it, clickFunction = { contractDbDto ->
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
        dialogTouchContract = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme).apply {
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
        findNavController().navigate(R.id.action_HomeFragment_to_FirstFragment)
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
                actionSearchExpanded.value = true
                filterFabClicked()
                showChips(binding.chipGroupSearch, ConstantsVariables.allChips,
                com.google.android.material.R.style.Widget_MaterialComponents_Chip_Action)
            }
        }
        return true
    }

    private fun applyFilter(){
        listContracts.value =  dbViewModel.allContracts?.value?.let { filterViewModel.filterFields(it) }
    }

    override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
        when(p0.itemId){
            R.id.action_search->{
                actionSearchExpanded.value = false
                deactivateAllChips()
            }
        }
        return true
    }

    private fun deactivateAllChips(){
        binding.chipGroupSearch.isActivated = false
        binding.chipGroupSearch.clearCheck()
        binding.chipGroupSearch.removeAllViews()
    }

}