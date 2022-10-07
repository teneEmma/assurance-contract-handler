package com.kod.assurancecontracthandler.views.fragments.selectfile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.model.MimeTypes
import com.kod.assurancecontracthandler.databinding.FragmentFirstBinding
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModelFactory
import com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel.ExcelDocumentsViewModel
import com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel.ExcelDocumentsViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    lateinit var excelDocumentVM: ExcelDocumentsViewModel
    lateinit var dbViewModel: DBViewModel
    lateinit var listOfContracts: List<ContractDbDto>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        checkAndGrantPermission()
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

        resultLauncher = registerForActivityResult(ActivityResultContracts
            .StartActivityForResult()){
                result->
            if (result.resultCode == Activity.RESULT_OK && result.data != null){
                documentActivityResult(result.data)
            }
        }

        excelDocumentVM.listOfContracts.observe(viewLifecycleOwner){
            listOfContracts = it
        }

        binding.btnCancelFile.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.btnSaveFile.setOnClickListener {
            addContracts(listOfContracts)
            toast("Document Sauvegardé Avec Success")
        }

        binding.actvImportFile.setOnClickListener {
            getDocument()
        }

    }

    private fun checkAndGrantPermission(){
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts
            .RequestMultiplePermissions()){ permissions->
            permissions.entries.forEach {
                Log.e("isGranted", "${it.key} -- ${it.value}")
            }
        }

        requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    private fun getDocument(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = MimeTypes.EXCEL_2007_SUP.value
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        resultLauncher.launch(intent)
    }

    private fun documentActivityResult(data: Intent?) {
            val selectedDocUri: Uri? = data?.data
            val file = selectedDocUri?.path?.let { File(it) }
            var selectedFileName: String? = null

             if (selectedDocUri.toString().startsWith("content://")) {
                var cursor: Cursor? = null
                try {
                    cursor = selectedDocUri?.let { requireActivity().contentResolver.query(it,
                        null, ":", null, "") }
                    if (cursor != null && cursor.moveToFirst()) {
                        val nameIndex =
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        selectedFileName = cursor.getString(nameIndex)
                    }
                } finally {
                    cursor?.close()
                }
            } else if (selectedDocUri.toString().startsWith("file://")) {
                selectedFileName = file?.name
            }
            snack("$selectedFileName ${resources.getString(R.string.success_select)}")

            var path = selectedDocUri?.path
            if (path != null && selectedFileName != null) {
                path = path.substring(path.indexOf(":") +1)
                readDocument(path)
            }
            binding.textviewSecond.text = selectedFileName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun snack(message: String){
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun toast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun addContracts(contracts: List<ContractDbDto>){
        dbViewModel.addContracts(contracts)
    }

    private fun readDocument(path: String){
        lifecycleScope.launch(Dispatchers.IO){
            excelDocumentVM.readDocumentContent(path)
        }
    }
}