package com.kod.assurancecontracthandler.viewmodels.baseviewmodel

import android.content.res.AssetManager
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.DataTypesConversionAndFormattingUtils
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.model.Contract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.util.*


/**
 *  The base ViewModel for all our viewModels. It contains the base values which each viewModel can contain.
 *  @param hasQueried A live value which indicates the state of the execution of a function through the
 *  [executeFunctionWithAnimation].
 *  @property executeFunctionWithAnimation Executes concurrently a given function while updating a live value.
 *  @property executeFunctionWithoutAnimation Just concurrently execute a function.
 */
abstract class BaseViewModel : ViewModel() {
    companion object {
        @JvmStatic
        protected val _allContracts: MutableLiveData<List<BaseContract>?> = MutableLiveData(null)

        @JvmStatic
        protected val _isLoading = MutableLiveData<Boolean>()

        @JvmStatic
        protected val _messageResourceId = MutableLiveData<Int?>(null)
    }

    open fun executeFunctionWithAnimation(execute: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            execute()
            _isLoading.postValue(false)
        }
    }

    fun executeFunctionWithoutAnimation(execute: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            execute()
        }
    }

    fun exportContractToFile(contract: Contract, assetManager: AssetManager): Boolean {
        try {
            val inputStream = assetManager.open("export_to_file.xlsx")
            val dateTime = TimeConverters.formatLocaleDateTimeForFileName(Date().time)
            val fileName = "${contract.assure}-$dateTime.xlsx"

            val folderDirectory = File(Environment.getExternalStorageDirectory(), ConstantsVariables.FOLDER_DIR)
            if (!folderDirectory.exists() && !folderDirectory.mkdir()) {
                _messageResourceId.postValue(R.string.folder_created_no)
                return false
            }

            val outFile = File(folderDirectory, fileName)
            val outputStream = FileOutputStream(outFile)
            val isWorkBookEdited = editWorkbook(inputStream, outputStream, contract)
            if (!isWorkBookEdited) {
                outFile.deleteOnExit()
                closeStreams(inputStream, outputStream)
                return false
            }
            copyFile(inputStream, outputStream)
            outputStream.flush()
            closeStreams(inputStream, outputStream)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    private fun closeStreams(inputStream: InputStream, outputStream: OutputStream) {
        inputStream.close()
        outputStream.close()
    }

    @Throws(IOException::class)
    private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
    }

    private fun editWorkbook(inputStream: InputStream, outputStream: FileOutputStream, contract: Contract): Boolean {
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheet(ConstantsVariables.EXPORT_SHEET_NAME)
        ConstantsVariables.appLocal
        if (sheet == null) {
            _messageResourceId.postValue(R.string.error_on_file_reading)
            return false
        }

        val data: MutableMap<Int, MutableMap<Pair<Int, Int>, String?>> = mutableMapOf()
        val formattedPhoneNumber =
            DataTypesConversionAndFormattingUtils.formatPhoneNumberForExporting(contract.telephone)
        val formattedPowerForExport =
            DataTypesConversionAndFormattingUtils.removePowerToString(contract.puissanceVehicule)

        data[0] = mutableMapOf((7 to 1) to contract.assure)
        data[1] = mutableMapOf((8 to 1) to formattedPhoneNumber)
        data[2] = mutableMapOf((8 to 4) to contract.numeroPolice)
        data[3] = mutableMapOf((9 to 5) to TimeConverters.formatLongToLocaleDate(Date().time, "dd-MM-yyyy"))
        data[4] = mutableMapOf((9 to 9) to contract.duree.toString())
        data[5] = mutableMapOf((11 to 1) to contract.assure)
        data[6] = mutableMapOf((11 to 5) to contract.compagnie)
        data[7] = mutableMapOf((12 to 1) to contract.assure)
        data[8] = mutableMapOf((12 to 5) to contract.attestation)
        data[9] = mutableMapOf((16 to 1) to contract.mark)
        data[10] = mutableMapOf((16 to 7) to contract.immatriculation)
        data[11] = mutableMapOf((17 to 4) to contract.categorie.toString())
        data[12] = mutableMapOf((18 to 1) to formattedPowerForExport)

        for (value in data.values) {
            for ((rowAndCell, cellContent) in value) {
                val rowNumber = rowAndCell.first
                val row: Row = sheet.getRow(rowNumber)
                val cellNumb = rowAndCell.second
                val cell = row.getCell(cellNumb)
                if (cellContent is String) cell.setCellValue(cellContent)
            }
        }

        workbook.write(outputStream)
        return true
    }
}