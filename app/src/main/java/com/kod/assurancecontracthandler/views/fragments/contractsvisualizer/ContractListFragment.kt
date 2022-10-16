package com.kod.assurancecontracthandler.views.fragments.contractsvisualizer

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.databinding.FilterDialogBinding
import com.kod.assurancecontracthandler.databinding.FragmentSecondBinding
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModelFactory
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.FilterViewModel

class ContractListFragment : Fragment(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener{

    private lateinit var binding: FragmentSecondBinding
    private lateinit var dbViewModel: DBViewModel
    private lateinit var listContracts: List<ContractDbDto>
    private lateinit var filterViewModel: FilterViewModel
    private lateinit var filterBinding: FilterDialogBinding
    private var chipIds: List<Int>?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSecondBinding.inflate(inflater, container, false)
        dbViewModel = ViewModelProvider(this,
            DBViewModelFactory(requireActivity().application))[DBViewModel::class.java]
        filterViewModel = ViewModelProvider(this)[FilterViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        dbViewModel.allContracts.observe(viewLifecycleOwner){listContracts->
            if(listContracts == null){
               binding.ivEmptyDatabase.visibility = View.VISIBLE
               binding.tvEmptyDatabase.visibility = View.VISIBLE
            }else{
                binding.ivEmptyDatabase.visibility = View.GONE
                binding.tvEmptyDatabase.visibility = View.GONE
                setRecyclerView(listContracts)
                this.listContracts = listContracts
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
        chipChecked()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.setOnActionExpandListener(this)
        when(item.itemId){
            R.id.action_filter-> {
                filterBinding = FilterDialogBinding.inflate(layoutInflater)
                if (filterBinding != null) {
                    showDialog(filterBinding)
                    filterBinding.btnDatePicker.setOnClickListener {
                        showDatePickerDialog()
                    }
                }
                  entriesListener()
            }
        }

        return true
    }

    private fun entriesListener() {
        filterBinding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            val list = filterViewModel.filterChip as MutableList
            checkedIds.forEachIndexed { index, i ->
                when(checkedIds[index]){
                    R.id.attestationChip_filter-> list.add(ConstantsVariables.ChipIds.ATTESTATION.index)
                    R.id.nPolicechip_filter-> list.add(ConstantsVariables.ChipIds.N_POLICE.index)
                    R.id.assureChip_filter-> list.add(ConstantsVariables.ChipIds.ASSURE.index)
                    R.id.compagnieChip_filter-> list.add(ConstantsVariables.ChipIds.COMPAGNIE.index)
                    R.id.immatriculationChip_filter-> list.add(ConstantsVariables.ChipIds.IMMATRICULATION.index)
                    R.id.apporteurChip_filter-> list.add(ConstantsVariables.ChipIds.APPORTEUR.index)
                }
            }
            filterViewModel.filterChip = checkedIds
        }
        filterBinding.btnAppliquerFiltre.setOnClickListener {
            filterViewModel.apporteur = filterBinding.etFiltrerApporteur.text.toString()
            filterViewModel.immatriculation = filterBinding.etFiltrerimmatriculation.text.toString()
            filterViewModel.attestation = filterBinding.etFiltrerAttestation.text.toString()
            filterViewModel.carteRose = filterBinding.etFiltrerCarteRose.text.toString()
            filterViewModel.minPrix = filterBinding.prixSlider.values[0].toInt()
            filterViewModel.maxPrix = filterBinding.prixSlider.values[1].toInt()
            filterViewModel.minPuissance = filterBinding.puissanceSlider.values[0].toInt()
            filterViewModel.maxPuissance = filterBinding.puissanceSlider.values[1].toInt()

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

    private fun showChips(chipGroup: ChipGroup){
        ConstantsVariables.chipNames.forEachIndexed { index, chipName ->
            val chip = Chip(requireContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice)
            chip.isCheckable = true
            if (index==0){
                chip.isChecked = true
            }
            chip.text = chipName
            chip.isClickable =true
            chipGroup.addView(chip)
        }
    }
    private fun showDialog(filterBinding: FilterDialogBinding){
        val dialog = Dialog(requireContext())
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
    private fun chipChecked(){
        binding.chipGroupSearch.setOnCheckedStateChangeListener { group, checkedIds ->
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
            setRecyclerView(listContracts)
            binding.swipeToRefresh.isRefreshing = false
        }
    }
    private fun swipeToRefresh(str: String){
        binding.swipeToRefresh.setOnRefreshListener {
            searchForClient(str, filterViewModel.searchChip)
            setRecyclerView(listContracts)
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun setRecyclerView(list: List<ContractDbDto>){
        val rvAdapter = ContractListAdapter(list)
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

    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_search->{
                showChips(binding.chipGroupSearch)
            }
        }
        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_search->{
                binding.chipGroupSearch.isActivated = false
                binding.chipGroupSearch.clearCheck()
                binding.chipGroupSearch.removeAllViews()
            }
        }
        return true
    }
}