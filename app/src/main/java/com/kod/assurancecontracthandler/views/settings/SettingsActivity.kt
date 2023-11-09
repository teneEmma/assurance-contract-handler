package com.kod.assurancecontracthandler.views.settings

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.databinding.PermissionDialogBinding
import com.kod.assurancecontracthandler.databinding.SettingsActivityBinding
import com.kod.assurancecontracthandler.model.MimeTypes
import java.io.*
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        private var shouldGoToSettingsPage = false
        private val requestPermissionLauncher: ActivityResultLauncher<String> =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission ->
                if (!hasPermission) {
                    if (!shouldGoToSettingsPage) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", requireContext().packageName, null)
                        intent.setData(uri)
                        startActivity(intent)
                    }
                    return@registerForActivityResult
                }
                pickFile()
            }
        private val documentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val result = saveFileToInternalStorage(uri)
                if (result) {
                    shortSnack(resources.getString(R.string.settings_applied))
                } else {
                    shortSnack(resources.getString(R.string.error_on_file_reading))
                }
            } else {
                shortSnack(resources.getString(R.string.file_select_not))
            }
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            preference.setViewId(R.layout.root_preference_edit_text_dialog_preference)
            super.onDisplayPreferenceDialog(preference)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<ListPreference>(resources.getString(R.string.theme_mode_key))?.setOnPreferenceChangeListener { _, it2 ->
                val darkMode: String = it2 as String
                val themeValues = resources.getStringArray(R.array.entry_values_theme)
                when (darkMode) {
                    themeValues[1] -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }

                    themeValues[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                true
            }

            findPreference<Preference>(resources.getString(R.string.file_for_exporting_key))?.setOnPreferenceClickListener {
                checkPermissionsStatus()
                true
            }

            findPreference<EditTextPreference>(resources.getString(R.string.predefined_message_key))?.setOnPreferenceChangeListener { _, newValue ->
                newValue as String
                return@setOnPreferenceChangeListener newValue.length <= 500
            }
        }

        private fun saveFileToInternalStorage(uri: Uri): Boolean {
            return try {
                val excelFile = requireContext().contentResolver?.openFileDescriptor(uri, "r")
                val fileDescriptor = excelFile?.fileDescriptor
                val inputStream = FileInputStream(fileDescriptor)
                val outFile = File(requireContext().filesDir, ConstantsVariables.EXPORT_FILE_NAME)
                val outputStream = FileOutputStream(outFile)
                copyFile(inputStream, outputStream)
                excelFile?.close()
                inputStream.close()
                outputStream.flush()
                outputStream.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        private fun shortSnack(message: String) {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        }

        private fun checkPermissionsStatus() {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    requiredPermission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    checkManageExternalStoragePermission()
                }

                shouldShowRequestPermissionRationale(requiredPermission) -> {
                    shouldGoToSettingsPage = true
                    showPermissionDialog()
                }

                else -> {
                    shouldGoToSettingsPage = false
                    checkManageExternalStoragePermission()
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
                checkManageExternalStoragePermission()
                dialog.dismiss()
            }
        }

        private fun checkManageExternalStoragePermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.setData(uri)
                    startActivity(intent)
                    requestPermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    return
                }
            }
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        @Throws(IOException::class)
        private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
        }

        private fun pickFile() {
            documentLauncher.launch(arrayOf(MimeTypes.EXCEL_2007_SUP.value, MimeTypes.EXCEL_2007.value))
        }


    }
}
