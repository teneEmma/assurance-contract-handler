package com.kod.assurancecontracthandler.views.main.fragmentListContracts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.databinding.FragmentListContractsBinding


class ContractListFragment : Fragment() {

    private lateinit var binding: FragmentListContractsBinding

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.addExcelFile.setOnClickListener {
            openAddExcelFilePage()
        }

        binding.ivEmptyDatabase.setOnClickListener {
            openAddExcelFilePage()
        }
    }

    private fun openAddExcelFilePage(){
        findNavController().navigate(R.id.action_HomeFragment_to_SelectFileFragment)
    }
}