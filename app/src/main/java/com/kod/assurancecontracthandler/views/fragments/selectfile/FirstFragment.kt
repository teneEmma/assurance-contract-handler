package com.kod.assurancecontracthandler.views.fragments.selectfile

import android.Manifest
import android.app.Dialog
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.fragment.app.DialogFragment
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
import java.io.File

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
//    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var excelDocumentVM: ExcelDocumentsViewModel
    lateinit var dbViewModel: DBViewModel
    private lateinit var listOfContracts: List<ContractDbDto>
    lateinit var openDoc:  ActivityResultLauncher<Array<String>>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    var permissions : Map<String, Boolean>? = null


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

//        resultLauncher = registerForActivityResult(ActivityResultContracts
//            .StartActivityForResult()){
//                result->
//            if (result.resultCode == Activity.RESULT_OK && result.data != null){
////                documentActivityResult(result.data)
//            }
//        }

        openDoc = registerForActivityResult(ActivityResultContracts.OpenDocument()){uri->
            if (uri != null) {
                documentActivityResult(uri)
                return@registerForActivityResult
            }

            toast("Aucun Fichier N'a Ete Selectioné")
            setVisualEffects(false)
        }

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
            getDocument()
        }
    }

    private fun checkAndGrantPermission(){
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts
            .RequestMultiplePermissions()){ permissions->
            permissions.entries.forEach {
                this.permissions = permissions
                Log.e("isGranted", "${it.key} -- ${it.value}")
                if (!it.value){
                    toast("${it.key.substringAfter("permission.")} Needed")
                }
            }
        }

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
            //TODO: Go to permission page and ask user to grant permission
        }
    }

    private fun getDocument(){
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//            type = MimeTypes.EXCEL_2007_SUP.value
//            addCategory(Intent.CATEGORY_OPENABLE)
//        }
//        resultLauncher.launch(intent)

        permissions?.forEach { (_, value) ->
            if (!value){
                showPermissionDialog()
                return
            }
        }

        openDoc.launch(arrayOf(MimeTypes.EXCEL_2007_SUP.value, MimeTypes.EXCEL_2007.value))
    }

    private fun documentActivityResult(uri: Uri) {
        val file = uri.path?.let { File(it) }
        var selectedFileName = file?.name

        if (uri.toString().startsWith("content://")) {
            var cursor: Cursor? = null
            try {
                cursor = uri?.let { requireActivity().contentResolver.query(it,
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
        } else if (uri.toString().startsWith("file://")) {
            selectedFileName = file?.name
        }

        val path = uri.path
        if (path != null && selectedFileName != null) {
            readDocument(path)
        }

        setVisualEffects(true)
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
            excelDocumentVM.readDocumentContent(path)
    }

    private fun backToVisualize(){
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    fun setVisualEffects(isEnabled: Boolean){
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