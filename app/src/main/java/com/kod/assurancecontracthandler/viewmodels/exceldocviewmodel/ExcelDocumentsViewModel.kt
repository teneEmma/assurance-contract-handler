package com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel

import ExcelSheetHeadersKeys
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.usecases.ProcessState
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.viewmodels.baseviewmodel.BaseViewModel
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.io.IOException
import kotlin.math.roundToInt

class ExcelDocumentsViewModel : BaseViewModel() {
    private val _listOfContracts = MutableLiveData<List<BaseContract>?>()
    val listOfContracts: LiveData<List<BaseContract>?>
        get() = _listOfContracts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _isSuccessful = MutableLiveData<ProcessState?>()
    val successful: MutableLiveData<ProcessState?>
        get() = _isSuccessful

    private val _messageResourceId = MutableLiveData<Int>()
    val messageResourceId: LiveData<Int>
        get() = _messageResourceId

    private val _progression = MutableLiveData<Int>()
    val progression: LiveData<Int>
        get() = _progression

    private fun readDocumentContent(inputStream: FileInputStream) {
        _progression.postValue(0)
        val workBook = XSSFWorkbook(inputStream)
        val sheetIterator = workBook.iterator()
        val contracts = readSheetsFromDocument(sheetIterator)
        _isLoading.postValue(false)
        if (contracts.isEmpty()) {
            _isSuccessful.postValue(ProcessState.Failed)
            _messageResourceId.postValue(R.string.file_cant_be_read)
        } else {
            _messageResourceId.postValue(R.string.load_successful)
            _isSuccessful.postValue(ProcessState.Success)
        }
        _listOfContracts.postValue(contracts)
    }

    private fun readSheetsFromDocument(sheetIterator: MutableIterator<Sheet>): List<BaseContract> {
        val contracts = mutableListOf<BaseContract>()
        while (sheetIterator.hasNext()) {
            val sheet = sheetIterator.next()
            val numbRows = sheet.physicalNumberOfRows
            val rowIterator: Iterator<Row> = sheet.iterator()
            val contractList = readRowsFromSheet(rowIterator, numbRows)
            contracts.addAll(contractList)
        }
        return contracts
    }

    private fun readRowsFromSheet(rowIterator: Iterator<Row>, numberOfRows: Int): List<BaseContract> {
        var headers: Map<String, String> = mutableMapOf()
        val contracts: MutableList<BaseContract> = mutableListOf()
        val hashCodes = mutableListOf<Int>()

        while (rowIterator.hasNext()) {
            val row = rowIterator.next()
            _progression.postValue(row.rowNum * 100 / numberOfRows)
            val cellIterator = row.cellIterator()
            var rowMap: Map<String, Any?> = mutableMapOf()

            if (cellIterator.hasNext()) {
                val cell = cellIterator.next()
                val cellValue = getCell(cell)

                if (cellValue != null) {
                    when {
                        ConstantsVariables.possibleHeaderValues.contains(cellValue.toString().uppercase()) -> {
                            val resultingHeader = readColumn(cellIterator)
                            headers = resultingHeader.associateWith {
                                it
                            }
                        }

                        cellValue is Double -> {
                            val resultingColumn = readColumn(cellIterator)
                            val columnEntry = headers.keys.zip(resultingColumn).toMap()
                            rowMap = columnEntry
                        }
                    }
                }
            }
            if (rowMap.isNotEmpty()) {
                val baseContract = setContractObj2(rowMap)

                if (baseContract.contract?.assure == null) {
                    continue
                }
                if (contracts.contains(baseContract)) {
                    continue
                }
                contracts.add(baseContract)
            }
        }
        return contracts
    }

    private fun contractWithoutIrrelevantFieldsHashCode(contract: Contract?): Int {
        val newContract = contract?.apply {
            telephone = null
            ENCAIS = null
            APPORTEUR = null
        }
        return newContract.hashCode()
    }

    private fun readColumn(cellIterator: Iterator<Cell>): List<String> {
        val headerMap = mutableListOf<String>()
        while (cellIterator.hasNext()) {
            val cell = cellIterator.next()
            val cellValue = getCell(cell)
            headerMap.add(cellValue.toString())
        }
        return headerMap
    }

    private fun notStringifyingNull(value: Any?): String? {
        return value?.toString()
    }

    private fun setContractObj2(row: Map<String, Any?>): BaseContract {
        val contract = Contract()

        contract.apply {
            attestation = notStringifyingNull(row[ExcelSheetHeadersKeys.keyAttestation])
            compagnie = notStringifyingNull(row[ExcelSheetHeadersKeys.keyCompany])
            assure = notStringifyingNull(row[ExcelSheetHeadersKeys.keyAssurer])
            effet = notStringifyingNull(row[ExcelSheetHeadersKeys.keyStartDate])
            echeance = notStringifyingNull(row[ExcelSheetHeadersKeys.keyDueDate])
            puissanceVehicule = notStringifyingNull(row[ExcelSheetHeadersKeys.keyPower])
            mark = notStringifyingNull(row[ExcelSheetHeadersKeys.keyMark])
            immatriculation = notStringifyingNull(row[ExcelSheetHeadersKeys.keyRegistration])
            zone = notStringifyingNull(row[ExcelSheetHeadersKeys.keyZone])
            APPORTEUR = notStringifyingNull(row[ExcelSheetHeadersKeys.keyPROVIDER])
            numeroPolice = notStringifyingNull(row[ExcelSheetHeadersKeys.keyPoliceNumber])?.let {
                policeNumberValidation(
                    it
                )
            }
            telephone = phoneNumbValidation(row[ExcelSheetHeadersKeys.keyPhoneNumber])
            categorie = doubleToInt(row[ExcelSheetHeadersKeys.keyCategory])
            carteRose = notStringifyingNull(doubleToInt(row[ExcelSheetHeadersKeys.keyPinkCard]))
            duree = doubleToInt(notStringifyingNull(row[ExcelSheetHeadersKeys.keyDuration]))
            DTA = doubleToInt(row[ExcelSheetHeadersKeys.keyDta])
            PN = doubleToInt(row[ExcelSheetHeadersKeys.keyPn])
            ACC = doubleToInt(row[ExcelSheetHeadersKeys.keyAcc])
            FC = doubleToInt(row[ExcelSheetHeadersKeys.keyFC])
            TVA = doubleToInt(row[ExcelSheetHeadersKeys.keyTVA])
            CR = doubleToInt(row[ExcelSheetHeadersKeys.keyCR])
            PTTC = doubleToInt(row[ExcelSheetHeadersKeys.keyPTTC])
            COM_PN = doubleToInt(row[ExcelSheetHeadersKeys.keyCOM_PN])
            COM_ACC = doubleToInt(row[ExcelSheetHeadersKeys.keyCOM_ACC])
            TOTAL_COM = doubleToInt(row[ExcelSheetHeadersKeys.keyTOTAL_COM])
            NET_A_REVERSER = doubleToInt(row[ExcelSheetHeadersKeys.keyTOTAL_TO_GIVE])
            ENCAIS = doubleToInt(row[ExcelSheetHeadersKeys.keyCASH_IN])
            COMM_LIMBE = doubleToInt(row[ExcelSheetHeadersKeys.keyCOMM_LIMBE])
            COMM_APPORT = doubleToInt(row[ExcelSheetHeadersKeys.keyCOMM_PROVIDER])
        }

        val hashCode = contractWithoutIrrelevantFieldsHashCode(contract)
        return BaseContract(id = hashCode, contract)
    }

    private fun phoneNumbValidation(parameter: Any?): String? {
        return if (parameter is String?) {
            val result = parameter?.toString()?.split('-')?.joinToString("")?.trim()

            if (result?.contains("\\d{9}$".toRegex()) == true) {
                result
            } else null
        } else null
    }

    private fun policeNumberValidation(strValue: String): String {
        var result = strValue
        if (result.contains(".")) {
            result = ""
            strValue.split(".").forEach { result += it }
            result = result.split("E")[0]
        }
        if (result.contains("3018")) {
            result = "PRU${result}"
        }
        return result
    }

    private fun doubleToInt(parameter: Any?): Int? {
        return when (parameter) {
            is Double? -> {
                var strContainer: String? = parameter.toString()
                if (strContainer?.contains(".*[.E].*".toRegex()) == true) {
                    if (strContainer.contains('E')) {
                        strContainer = strContainer.split("E")[0]
                        for (n in strContainer.length until 9) {
                            strContainer += '0'
                        }
                    }
                    val splitString = strContainer.split('.')
                    val value = if (splitString[1] == "0") splitString[0] else splitString[0] + splitString[1]
                    value.toInt()
                } else parameter?.toInt()
            }

            is Int? -> {
                parameter
            }

            is String -> {
                val newValue = parameter.toDoubleOrNull()
                doubleToInt(newValue)
            }

            else -> {
                null
            }
        }
    }

    fun readDocumentWithAnimation(inputStream: FileInputStream) {
        executeFunctionWithAnimation {
            try {
                _isLoading.postValue(true)
                _isSuccessful.postValue(ProcessState.Init)
                readDocumentContent(inputStream)
            } catch (e: java.lang.Exception) {
                when (e) {
                    is IOException -> {
                        _isLoading.postValue(false)
                        _isSuccessful.postValue(ProcessState.Failed)
                        _messageResourceId.postValue(R.string.error_on_file_reading)
                    }

                    else -> {
                        _isLoading.postValue(false)
                        _isSuccessful.postValue(ProcessState.Failed)
                        _messageResourceId.postValue(R.string.please_retry)
                        e.stackTraceToString()
                    }
                }
            }
        }
    }

    private fun getCell(cell: Cell): Any? {
        return when (cell.cellType) {
            CellType.BOOLEAN -> cell.booleanCellValue
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    TimeConverters.formatISODateToLocaleDate(cell.localDateTimeCellValue.toString())
                } else {
                    cell.numericCellValue
                }
            }

            CellType.FORMULA -> {
                when (cell.cachedFormulaResultType) {
                    CellType.NUMERIC -> cell.numericCellValue.roundToInt()
                    else -> cell.richStringCellValue
                }
            }

            CellType.ERROR -> cell.errorCellValue
            else -> null
        }
    }
}