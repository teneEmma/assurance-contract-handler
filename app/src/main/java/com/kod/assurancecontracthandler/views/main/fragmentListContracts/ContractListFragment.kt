package com.kod.assurancecontracthandler.views.main.fragmentListContracts

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.utilities.BottomDialogView
import com.kod.assurancecontracthandler.databinding.ContractDetailsBinding
import com.kod.assurancecontracthandler.databinding.FragmentListContractsBinding
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DatabaseViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DatabaseViewModelFactory
import com.kod.assurancecontracthandler.views.fragments.home.contractlist.ContractListAdapter


class ContractListFragment : Fragment() {

    private lateinit var binding: FragmentListContractsBinding

    private val dbViewModel by viewModels<DatabaseViewModel> {
        val contractDao = ContractDatabase.getDatabase(requireContext()).contractDao()
        val contractRepository = ContractRepository(contractDao)
        DatabaseViewModelFactory(contractRepository)
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListContractsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setRecyclerView()

        binding.addExcelFile.setOnClickListener {
            openAddExcelFilePage()
        }

        binding.ivEmptyDatabase.setOnClickListener {
            openAddExcelFilePage()
        }

        lifecycleScope.launchWhenStarted {
            dbViewModel.allContracts.observe(viewLifecycleOwner) { listContracts ->
                if (listContracts.isNullOrEmpty()) {
                    binding.ivEmptyDatabase.visibility = View.VISIBLE
                    binding.tvEmptyDatabase.visibility = View.VISIBLE
                    binding.rvListContract.visibility = View.GONE
                } else {
                    binding.ivEmptyDatabase.visibility = View.GONE
                    binding.tvEmptyDatabase.visibility = View.GONE
                    binding.rvListContract.visibility = View.VISIBLE
                    listContracts.filter { contractDbDto ->
                        contractDbDto.contract?.assure != "SOMME"
                    }.let { listContract ->
                        rvAdapter?.setContractList(listContract)
                    }
                }
            }
        }

        dbViewModel.hasQueried.observe(viewLifecycleOwner) {
            if (it) binding.progressBar.hide()
            else binding.progressBar.show()
        }

        updateContractsList()
        swipeToRefreshAfterChipCollapse()
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
            shortSnack(resources.getString(R.string.to_be_implemented_text))
        } else if (item.itemId == R.id.action_settings) {
            shortSnack(resources.getString(R.string.to_be_implemented_text))
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
                dbViewModel.apply {
                    executeFunWithoutAnimation { fetchAllContracts() }
                }
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
        binding.rvListContract.layoutManager = LinearLayoutManager(context)
        binding.rvListContract.setHasFixedSize(true)
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
        BottomDialogView().manageContractDetailViews(contractItemBinding, c, requireContext())

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()

        dialogTouchContract?.window?.setLayout(width, height)
        dialogTouchContract?.show()

        contractItemBinding.assurerName.setOnClickListener {
            if (c?.numeroPolice.isNullOrEmpty() || c?.attestation.isNullOrEmpty()) {
                toast(resources.getString(R.string.non_valid_option))
                return@setOnClickListener
            }
            shortSnack(resources.getString(R.string.to_be_implemented_text))
            dialogTouchContract?.dismiss()
        }
    }

    private fun updateContractsList() {
        dbViewModel.apply {
            executeFunWithAnimation { fetchAllContracts() }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}