package com.kod.assurancecontracthandler.views.main.fragmentFileSelection

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.usecases.ProcessState
import com.kod.assurancecontracthandler.databinding.FragmentSelectFileBinding
import com.kod.assurancecontracthandler.databinding.PermissionDialogBinding
import com.kod.assurancecontracthandler.model.MimeTypes
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel.SelectFileViewModel
import com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel.SelectFileViewModelFactory
import java.io.FileInputStream


class SelectFileFragment : Fragment() {

    private var _binding: FragmentSelectFileBinding? = null
    private val requiredPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private var numberOfTimesClicked = 0
    private var shouldGoToSettingsPage = false
    private val binding get() = _binding!!
    private val selectFileViewModel by viewModels<SelectFileViewModel> {
        val contractDao = ContractDatabase.getDatabase(requireContext()).contractDao()
        val contractRepository = ContractRepository(contractDao)
        SelectFileViewModelFactory(contractRepository)
    }
    private var fileName: String? = null
    private val documentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val excelFile = requireContext().contentResolver?.openFileDescriptor(uri, "r")
            val fileDescriptor = excelFile?.fileDescriptor
            val inputStream = FileInputStream(fileDescriptor)
            fileName = uri.path?.substringAfter(":")
            readDocument(inputStream)
        } else shortSnack(resources.getString(R.string.file_select_not))
    }
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission ->
            if (!hasPermission) {
                if(!shouldGoToSettingsPage){
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireActivity().applicationContext.packageName, null)
                    intent.setData(uri)
                    startActivity(intent)
                }
                return@registerForActivityResult
            }
            pickFile()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectFileViewModel.messageResourceId.observe(viewLifecycleOwner) {
            if (it != null) {
                shortSnack(resources.getString(it))
            }
        }

        binding.btnCancelFile.setOnClickListener {
            backToVisualize()
        }

        selectFileViewModel.successful.observe(viewLifecycleOwner) { success ->
            if (success == ProcessState.Success) {
                binding.btnSaveFile.visibility = View.VISIBLE
            } else {
                binding.btnSaveFile.visibility = View.GONE
            }
        }

        selectFileViewModel.isLoading.observe(viewLifecycleOwner) { value ->
            if (value) {
                binding.fileLoadingPB.visibility = View.VISIBLE
                setVisualEffects(false)
            } else {
                binding.fileLoadingPB.visibility = View.GONE
                setVisualEffects(true)
                binding.textviewSecond.text = fileName
            }
        }

        selectFileViewModel.progression.observe(viewLifecycleOwner) {
            binding.fileLoadingPB.progress = it
        }

        binding.btnSaveFile.setOnClickListener {
            if (!selectFileViewModel.listOfContracts.isNullOrEmpty()) {
                addContracts()
                shortSnack(resources.getString(R.string.save_successful))
                backToVisualize()
            } else {
                shortSnack(resources.getString(R.string.save_failed))
            }

        }

        binding.actvImportFile.setOnClickListener {
            numberOfTimesClicked++
            checkPermissionsStatus()
        }
    }

    private fun checkPermissionsStatus() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                requiredPermission
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickFile()
            }

            shouldShowRequestPermissionRationale(requiredPermission) -> {
                shouldGoToSettingsPage = true
                showPermissionDialog()
            }

            else -> {
                shouldGoToSettingsPage = false
                requestPermissionLauncher.launch(
                    requiredPermission
                )
            }
        }
    }

    private fun showPermissionDialog() {
        val dialogBinding = PermissionDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.show()

        dialogBinding.btnDialogPositive.setOnClickListener {
            requestPermissionLauncher.launch(
                requiredPermission
            )
            dialog.dismiss()
        }
    }

    private fun pickFile() {
        documentLauncher.launch(arrayOf(MimeTypes.EXCEL_2007_SUP.value, MimeTypes.EXCEL_2007.value))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun shortSnack(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun longSnack(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun addContracts() = selectFileViewModel.apply {
        executeFunctionWithAnimation { addContracts() }
    }

    private fun readDocument(inputStream: FileInputStream) {
        selectFileViewModel.readDocumentWithAnimation(inputStream)
    }

    private fun backToVisualize() {
        findNavController().navigate(R.id.action_SelectFileFragment_to_HomeFragment)
        findNavController().popBackStack(R.id.SelectFileFragment, true)
    }

    private fun setVisualEffects(isEnabled: Boolean) {
        if (isEnabled) {
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