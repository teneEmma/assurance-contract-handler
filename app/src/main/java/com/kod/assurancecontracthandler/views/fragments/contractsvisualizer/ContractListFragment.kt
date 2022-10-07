package com.kod.assurancecontracthandler.views.fragments.contractsvisualizer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.databinding.FragmentSecondBinding
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModelFactory

class ContractListFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbViewModel: DBViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dbViewModel = ViewModelProvider(this,
            DBViewModelFactory(requireActivity().application))[DBViewModel::class.java]
        updateContractsList()

        dbViewModel.allContracts.observe(viewLifecycleOwner){listContracts->
            if(listContracts == null){
               binding.ivEmptyDatabase.visibility = View.VISIBLE
               binding.tvEmptyDatabase.visibility = View.VISIBLE
            }else{
                binding.ivEmptyDatabase.visibility = View.GONE
                binding.tvEmptyDatabase.visibility = View.GONE
                setRecyclerView(listContracts)
            }
        }

        binding.addExcelFile.setOnClickListener {
            addExcelFile()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateContractsList(){
        dbViewModel.fetchAllContracts()
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
}