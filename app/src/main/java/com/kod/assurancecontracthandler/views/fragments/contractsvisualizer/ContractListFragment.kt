package com.kod.assurancecontracthandler.views.fragments.contractsvisualizer

import android.app.Dialog
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.databinding.FilterDialogBinding
import com.kod.assurancecontracthandler.databinding.FragmentSecondBinding
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModelFactory
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.FilterViewModel
import com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel.ExcelDocumentsViewModel
import kotlin.collections.HashMap

class ContractListFragment : Fragment(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener{

    private lateinit var binding: FragmentSecondBinding
    private lateinit var dbViewModel: DBViewModel
    private lateinit var listContracts: MutableLiveData<List<ContractDbDto>?>
    private lateinit var filterViewModel: FilterViewModel
    private lateinit var filterBinding: FilterDialogBinding
    private lateinit var expandableAdapter: ExpandableSliderAdapter
    lateinit var listTextViews: List<View>
    lateinit var dialog: Dialog

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
            if(listContracts == null){
               binding.ivEmptyDatabase.visibility = View.VISIBLE
               binding.tvEmptyDatabase.visibility = View.VISIBLE
            }else{
                binding.ivEmptyDatabase.visibility = View.GONE
                binding.tvEmptyDatabase.visibility = View.GONE
                setRecyclerView()
            }
        }

        binding.addExcelFile.setOnClickListener {
            addExcelFile()
        }

        updateContractsList()
    }

    @Deprecated("Deprecated in ")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = "Faire une recherche..."
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)
        searchChipChecked()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.setOnActionExpandListener(this)
        when(item.itemId){
            R.id.action_filter-> {
                filterBinding = FilterDialogBinding.inflate(layoutInflater)
                listTextViews = listOf(filterBinding.ilFiltrerAppoteur,
                    filterBinding.ilFiltrerImmatriculation, filterBinding.ilFiltrerAttestation,
                    filterBinding.ilFiltrerCarteRose, filterBinding.ilFiltrerCompagnie,
                    filterBinding.ilFiltrerAssure, filterBinding.ilFiltrerMark,
                    filterBinding.ilFiltrerPolice)
                showDialog()
                showChips(filterBinding.chipGroupFilter, ConstantsVariables.filterChipNames, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Filter)
                filterBinding.btnDatePicker.setOnClickListener {
                    showDatePickerDialog()
                }
                setExpandableListView()
                entriesListener()
            }
        }

        return true
    }

    
    private fun setExpandableListView(){
        val groupTitleList = listOf("FILTRER LES PRIX", "FILTRER LA PUISSANCE")
        val childTitleList = listOf(listOf("DTA", "PN", "ACC", "FC", "TVA", "CR", "PTTC", "ENCAIS", "REVER", "SOLDE"),
        listOf("PUISSANCE"))
        val childDataList: HashMap<String, List<String>> = hashMapOf()

        groupTitleList.forEachIndexed { index, _ ->
            childDataList[groupTitleList[index]] = childTitleList[index]
        }

        expandableAdapter = ExpandableSliderAdapter(requireContext(),
        groupTitleList, childDataList
        )

        filterBinding.expandableLvSliders.setAdapter(expandableAdapter)
        filterBinding.expandableLvSliders.setOnChildClickListener { _, v, groupPosition, childPosition, _ ->
            toast(childDataList[groupTitleList[groupPosition]]?.get(childPosition)!!)
            expandableAdapter.makeOthersVisible(groupPosition, childPosition, v)
            true
        }
    }

    private fun entriesListener() {
        filterBinding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            Log.e("CHECKED IDS", checkedIds.toString())
            filterViewModel.filterChip = checkedIds
            listTextViews.forEachIndexed { index, v ->
                if (checkedIds.contains(index+1)){
                    v.visibility = View.VISIBLE
                }else{
                    v.visibility = View.GONE
                }
            }
        }

        filterBinding.btnReset.setOnClickListener {
            filterBinding.etFiltrerAttestation.text?.clear().also { filterViewModel.attestation = "" }
            filterBinding.etFiltrerApporteur.text?.clear().also { filterViewModel.apporteur = "" }
            filterBinding.etFiltrerimmatriculation.text?.clear().also { filterViewModel.immatriculation= "" }
            filterBinding.etFiltrerCarteRose.text?.clear().also { filterViewModel.carteRose = "" }
            filterBinding.etFiltrerCompagnie.text?.clear().also { filterViewModel.compagnie = "" }
            filterBinding.etFiltrerAssure.text?.clear().also { filterViewModel.assure = "" }
            filterBinding.etFiltrerMark.text?.clear().also { filterViewModel.mark = "" }
            filterBinding.etFiltrerPolice.text?.clear().also { filterViewModel.nPolice = "" }
            filterViewModel.prixSlidersValue?.clear()
            filterViewModel.puissanceSliderValue?.clear()
            filterBinding.chipGroupFilter.clearCheck()
            filterBinding.expandableLvSliders.clearChoices()
        }

        filterBinding.btnAppliquerFiltre.setOnClickListener {
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

    private fun showDatePickerDialog(){
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Choisir L'Interval De Temps Auquel Le Filtre Sera Appliquer")
            .setSelection(
                Pair(MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds())
            ).build()

        dateRangePicker.show(requireActivity().supportFragmentManager, "DATE_RANGE_PICKER")
        dateRangePicker.addOnPositiveButtonClickListener {dates->
            Log.e("DATE_RANGE_PICKER", dateRangePicker.headerText)
            Log.e("IT_RANGE_PICKER", "${ dates.first } && ${dates.second}")
            filterViewModel.minDate = dates.first
            filterViewModel.maxDate = dates.second
        }
    }

    private fun setNewDialogParams(attributs: Window, width: Int, height: Int): WindowManager.LayoutParams{
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(attributs.attributes)
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
        swipeToRefresh(str)
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
            Log.e("checkedIds ", checkedIds.toString())
            if (checkedIds.size > 0) {
                filterViewModel.searchChip = checkedIds[0]
                return@setOnCheckedStateChangeListener
            }
            filterViewModel.searchChip = null
            Log.e("checkedIds ", checkedIds.toString())
        }
    }

    private fun updateContractsList(){
        swipeToRefresh()
        dbViewModel.fetchAllContracts()
    }

    private fun swipeToRefresh(){
        binding.swipeToRefresh.setOnRefreshListener {
            updateContractsList()
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun swipeToRefresh(str: String){
        binding.swipeToRefresh.setOnRefreshListener {
            searchForClient(str, filterViewModel.searchChip)
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun setRecyclerView(){
        val rvAdapter = listContracts.value?.let { ContractListAdapter(it) }
        binding.rvListContract.adapter = rvAdapter
        binding.rvListContract.layoutManager =LinearLayoutManager(context)
        binding.rvListContract.setHasFixedSize(true)
    }

    private fun addExcelFile(){
        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
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
                showChips(binding.chipGroupSearch, ConstantsVariables.searchChipNames,
                com.google.android.material.R.style.Widget_MaterialComponents_Chip_Action)
            }
        }
        return true
    }

    private fun applyFilter(){
        val timeConverter = TimeConverters()
        Log.e("LIST CONTRACTS", dbViewModel.allContracts.value.toString())
        Log.e("LIST SIZE", dbViewModel.allContracts.value?.size.toString())
        listContracts.value =  dbViewModel.allContracts.value?.let { filterViewModel.filterFields(it) }
    }

    override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
        when(p0.itemId){
            R.id.action_search->{
                binding.chipGroupSearch.isActivated = false
                binding.chipGroupSearch.clearCheck()
                binding.chipGroupSearch.removeAllViews()
            }
        }
        return true
    }
}