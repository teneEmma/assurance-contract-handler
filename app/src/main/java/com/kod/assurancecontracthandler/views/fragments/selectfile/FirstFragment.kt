package com.kod.assurancecontracthandler.views.fragments.selectfile

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.model.MimeTypes
import com.kod.assurancecontracthandler.databinding.FragmentFirstBinding
import com.kod.assurancecontracthandler.databinding.PermissionDialogBinding
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModelFactory
import com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel.ExcelDocumentsViewModel
import com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel.ExcelDocumentsViewModelFactory
import java.io.FileInputStream

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var excelDocumentVM: ExcelDocumentsViewModel
    private lateinit var dbViewModel: DBViewModel
    private lateinit var listOfContracts: List<ContractDbDto>
    private var permissions : Map<String, Boolean>? = null
    private val documentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()){ uri->
            if (uri != null) {
                val excelFile = context?.contentResolver?.openFileDescriptor(uri, "r")
                val fileDescriptor = excelFile?.fileDescriptor
                val inputStream = FileInputStream(fileDescriptor)
                val fileName = uri.path?.substringAfter(":")
                readDocument(inputStream)
                if (fileName != null) {
                    showViews(fileName)
                }
            } else
                shortSnack("Aucun Fichier Selectionés")
        }
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions->
            this.permissions = permissions
            permissions.entries.forEach {
                Log.e("isGranted", "${it.key} -- ${it.value}")
                if (!it.value){
                    longSnack("${getPermissionString(it.key.substringAfter("permission."))} Needed")
                    showPermissionDialog()
                    return@registerForActivityResult
                }
            }
            pickFile()
    }

    private fun getPermissionString(mapVal: String): String{
        return if(mapVal.contains("WRITE"))
            "WRITING PERMISSION"
        else
            "READ PERMISSION"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        excelDocumentVM = ViewModelProvider(this,
            ExcelDocumentsViewModelFactory(requireActivity().application)
        )[ExcelDocumentsViewModel::class.java]
        dbViewModel = ViewModelProvider(this,
            DBViewModelFactory(requireActivity().application))[DBViewModel::class.java]
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        excelDocumentVM.listOfContracts.observe(viewLifecycleOwner){
            listOfContracts = it
        }

        binding.btnCancelFile.setOnClickListener {
            backToVisualize()
        }

        binding.btnSaveFile.setOnClickListener {
            addContracts(listOfContracts)
            Log.e("LISTE ACTUALISEE", listOfContracts.toString())
            toast("Document Sauvegardé Avec Success")
            backToVisualize()
        }

        binding.actvImportFile.setOnClickListener {
            requestPermissions()
        }
    }

    private fun requestPermissions(){
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    private fun showPermissionDialog(){
        val dialogBinding = PermissionDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.show()

        dialogBinding.btnDialogPositive.setOnClickListener {
            requestPermissions()
        }
    }

    private fun pickFile(){
        documentLauncher.launch(arrayOf(MimeTypes.EXCEL_2007_SUP.value, MimeTypes.EXCEL_2007.value))
    }

    private fun showViews(name: String){
        setVisualEffects(true)
        binding.textviewSecond.text = name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun longSnack(message: String){
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun shortSnack(message: String){
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun toast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun addContracts(contracts: List<ContractDbDto>){
        dbViewModel.addContracts(contracts)
    }

    private fun readDocument(inputStream: FileInputStream){
            excelDocumentVM.readDocumentContent(inputStream)
    }

    private fun backToVisualize(){
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    private fun setVisualEffects(isEnabled: Boolean){
        if (isEnabled){
            binding.imageExcel.visibility = View.VISIBLE
            binding.textviewSecond.visibility = View.VISIBLE
            binding.btnSaveFile.isEnabled = true
            return
        }
        binding.imageExcel.visibility = View.GONE
        binding.textviewSecond.visibility = View.GONE
        binding.btnSaveFile.isEnabled = false
    }
}