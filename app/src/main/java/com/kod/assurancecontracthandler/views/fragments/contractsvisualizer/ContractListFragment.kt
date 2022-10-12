package com.kod.assurancecontracthandler.views.fragments.contractsvisualizer

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.databinding.FragmentSecondBinding
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModelFactory

class ContractListFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbViewModel: DBViewModel
    private lateinit var listContracts: List<ContractDbDto>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        dbViewModel = ViewModelProvider(this,
            DBViewModelFactory(requireActivity().application))[DBViewModel::class.java]
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Deprecated("Deprecated in ")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = "Faire une recherche..."
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)
    }

    private fun searchForAClient(str: String){
        val queryStr = "%$str%"
        swipeToRefresh(queryStr)
        dbViewModel.searchClient(queryStr)
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
            searchForAClient(str)
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
            searchForAClient(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchForAClient(newText)
        }
        return true
    }
}