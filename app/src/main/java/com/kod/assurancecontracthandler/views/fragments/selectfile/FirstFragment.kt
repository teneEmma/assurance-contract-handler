package com.kod.assurancecontracthandler.views.fragments.selectfile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.system.ErrnoException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.model.IntentRequestCodes
import com.kod.assurancecontracthandler.common.model.MimeTypes
import com.kod.assurancecontracthandler.databinding.FragmentFirstBinding
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        checkAndGrantPermission()
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.actvImportFile.setOnClickListener {
            getDocument()
            toast("Clicked")
        }
    }

    private fun checkAndGrantPermission(){
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions->
            permissions.entries.forEach {
                Log.e("isGranted", "${it.key} -- ${it.value}")
            }
        }

        Log.e("filesDir", "${requireActivity().filesDir}")
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    private fun getDocument(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = MimeTypes.EXCEL_2007_SUP.value
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, IntentRequestCodes.OPEN_DOCUMENT.value)
    }

    private fun snack(message: String){
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun toast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 10000 && data?.data != null){
            val selectedDocUri: Uri? = data.data
            val file = selectedDocUri?.path?.let { File(it) }
            var selectedFileName: String? = null

             if (selectedDocUri.toString().startsWith("content://")) {
                var cursor: Cursor? = null
                try {
                    cursor = selectedDocUri?.let { requireActivity().contentResolver.query(it, null, ":", null, "") }
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
            Log.e("file name", selectedFileName.toString())

            var path = selectedDocUri?.path
            Log.e("path", path.toString())
            if (path != null) {
                path = path.substring(path.indexOf(":") +1)
                if (selectedFileName != null) {
                    openFile(path)
                }
            }

            Log.e("path", path.toString())

            binding.textviewSecond.text = selectedFileName
        }
    }

    private fun openFile(path: String){
//        val externalStorageDirectory: File =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        var inputStream: FileInputStream? = null
        val folder = File("/storage/emulated/0/$path")
        try{
             inputStream = FileInputStream(folder)
        }catch (e: java.lang.Exception){
            if(e is FileNotFoundException && e.cause is ErrnoException){
                toast("please you need to grant App permission")
            }
        }

        if (inputStream != null){
            val workBook = XSSFWorkbook(inputStream)
            val sheet = workBook.getSheetAt(0)
            val numberOfRows = sheet.physicalNumberOfRows
            for (rowIndex in 0..numberOfRows){
                val row: Row = sheet.getRow(rowIndex)
                Log.e("ROW", "Of index: $rowIndex has value: $row")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}