package com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kod.assurancecontracthandler.common.usecases.ErrorLevel
import com.kod.assurancecontracthandler.common.utilities.ModelSchemaStructurer
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.common.usecases.SheetCursorPosition
import com.kod.assurancecontracthandler.model.Contract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.ss.formula.functions.T
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt

class ExcelDocumentsViewModel(application: Application) : AndroidViewModel(application), ModelSchemaStructurer{
    private val _listOfContracts =  MutableLiveData<List<ContractDbDto>?>()
    val listOfContracts = _listOfContracts

    val isLoading: LiveData<Boolean>
    get() = _hasStarted
    private val _hasStarted = MutableLiveData<Boolean>()
    val hasStarted: MutableLiveData<Boolean>
    get() = _hasStarted
    private val _success = MutableLiveData<Boolean?>()
    val successful: MutableLiveData<Boolean?>
    get() = _success
    private var isSuccessfull: Boolean? = null

    private val _toastMessages = MutableLiveData<String>()
    val toastMessages: LiveData<String> = _toastMessages

    private val _progression = MutableLiveData<Int>()
    val progression: LiveData<Int> = _progression

    private var header = listOf<String>()
    private fun readDocumentContent(inputStream: FileInputStream){
        _progression.postValue(0)
        val workBook = XSSFWorkbook(inputStream)
        val sheetIterator = workBook.iterator()
        val allDocumentRows: MutableList<ContractDbDto> = mutableListOf()
        isSuccessfull = null
        while (sheetIterator.hasNext()){
            val sheet = sheetIterator.next()
            var startDate = Date()
            var endDate = Date()
            val numbRows = sheet.physicalNumberOfRows
            val rowIterator: Iterator<Row> = sheet.iterator()
            while(rowIterator.hasNext()){
                val row = rowIterator.next()
                _progression.postValue((row.rowNum*100/numbRows))
                val cellIterator = row.cellIterator()
                val rowContentRead = mutableListOf<Any?>()
                while(cellIterator.hasNext()){
                    val cell = cellIterator.next()
                    var cellValue = getCell(cell)

                    if (cell.cellType == HSSFCell.CELL_TYPE_NUMERIC){
                        if(HSSFDateUtil.isCellDateFormatted(cell)){
                            cellValue = cell.dateCellValue
                        }
                    }
                    rowContentRead.add(cellValue)
                }
                when(val sheetCursor = verifyRow { verifyRowScheme(rowContentRead, header) }){
                    is SheetCursorPosition.BeginningOfSheet -> {
                        sheetCursor.startDate?.let {value-> startDate = value }
                        sheetCursor.endDate?.let {value-> endDate = value }
                    }
                    is SheetCursorPosition.Footer -> {
                        val contractDbDto = ContractDbDto(
                            0, contract = sheetCursor.footer,
                            sheetCreationDateStart = startDate, sheetCreationDateEnd = endDate)

                        if (!contractDbDto.contract?.assure.isNullOrEmpty())
                            allDocumentRows.add(contractDbDto)
                    }
                    is SheetCursorPosition.HeaderOfSheet -> {
                        header = sheetCursor.headers
                    }
                    is SheetCursorPosition.RowContent -> {
                        val contractDbDto = ContractDbDto(
                            0, contract = sheetCursor.content,
                            sheetCreationDateStart = startDate, sheetCreationDateEnd = endDate)
                        if (contractDbDto.contract?.assure != null) {
                            allDocumentRows.add(contractDbDto)
                        }else{
                            //TODO: Put an error level to say that it is has encountered an error ina sheetName while reading
                        }
                    }
                    is SheetCursorPosition.EmptyRow -> Log.e("em", "ufutc")
                    is SheetCursorPosition.Error -> {
                        if(!sheetCursor.error.isNullOrEmpty())
                            _toastMessages.postValue(sheetCursor.error.toString())
                        if (sheetCursor.errorLevel != ErrorLevel.HIGH){
                            isSuccessfull = true
                            setStates(false, isSuccessfull)
                            break
                        }else break
                    }
                }
            }
        }
        setStates(hasStarted = false, isSuccess = isSuccessfull)
        _listOfContracts.postValue(allDocumentRows)
    }

    fun readDocument(inputStream: FileInputStream){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setStates(hasStarted = true, isSuccess = isSuccessfull)
                readDocumentContent(inputStream)
            }catch (e: java.lang.Exception){
                when(e){
                    is IOException -> {
                        isSuccessfull = false
                        setStates(false, isSuccessfull)
                        _toastMessages.postValue("Erreur de fichier")
                    }
                    else -> {
                        isSuccessfull = false
                        setStates(false, isSuccessfull)
                        _toastMessages.postValue("Veuillez Réessayer")
                        e.stackTraceToString()
                    }
                }
            }
        }
    }
    private fun verifyRow(verifySchemeFunc: () -> SheetCursorPosition<T>): SheetCursorPosition<T> {
        return try {
            isSuccessfull = true
            verifySchemeFunc.invoke()
        }catch (e: java.lang.Exception){
            e.stackTraceToString()
            isSuccessfull = false
            setStates(hasStarted = false, isSuccess = isSuccessfull)
            SheetCursorPosition.Error("Le Fichier n'a pas pu être complemtement ou entierement lu", ErrorLevel.MEDIUM)
        }
    }

    private fun getCell(cell: Cell): Any?{
        return when(cell.cellType){
            Cell.CELL_TYPE_BOOLEAN -> cell.booleanCellValue
            Cell.CELL_TYPE_STRING -> cell.stringCellValue
            Cell.CELL_TYPE_NUMERIC -> cell.numericCellValue
            Cell.CELL_TYPE_FORMULA -> {
                when(cell.cachedFormulaResultType){
                    Cell.CELL_TYPE_NUMERIC -> cell.numericCellValue.roundToInt()
                    else -> cell.richStringCellValue
                }
            }
            Cell.CELL_TYPE_ERROR -> cell.errorCellValue
            else -> null
        }
    }

    private fun setStates(hasStarted: Boolean, isSuccess: Boolean?){
        _hasStarted.postValue(hasStarted)
        _success.postValue(isSuccess)
    }
}