package com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kod.assurancecontracthandler.common.utilities.ModelSchemaStructurer
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.usecases.SheetCursorPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

class ExcelDocumentsViewModel(application: Application) : AndroidViewModel(application), ModelSchemaStructurer{
    private val _listOfContracts =  MutableLiveData<kotlin.collections.List<ContractDbDto>>()
    val listOfContracts = _listOfContracts

    private val _toastMessages = MutableLiveData<String>()
    val toastMessages: LiveData<String> = _toastMessages

    fun readDocumentContent(inputStream: FileInputStream){
        viewModelScope.launch(Dispatchers.IO) {
            val workBook = XSSFWorkbook(inputStream)
            val sheet = workBook.getSheetAt(0)
            val allDocumentRows: MutableList<ContractDbDto> = mutableListOf()
            var header = listOf<String>()
            var startDate = Date()
            var endDate = Date()

            val rowIterator: Iterator<Row> = sheet.iterator()
            while(rowIterator.hasNext()){
                val row = rowIterator.next()
                val cellIterator = row.cellIterator()
                val rowContentRead = mutableListOf<Any?>()
                Log.e("ROW", "Row: ${row.rowNum}")
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
                when(val sheetCursor = verifyRowScheme(rowContentRead, header)){
                    is SheetCursorPosition.BeginningOfSheet -> {
                        startDate = sheetCursor.startDate
                        endDate = sheetCursor.endDate
                    }
                    is SheetCursorPosition.Footer -> {
                        //TODO: Implement the case of a footer
                    }
                    is SheetCursorPosition.HeaderOfSheet -> {
                        header = sheetCursor.headers
                    }
                    is SheetCursorPosition.RowContent -> {
                        val contractDbDto = ContractDbDto(
                            row.rowNum, contract = sheetCursor.content,
                            sheetCreationDateStart = startDate, sheetCreationDateEnd = endDate)
                        Log.e("CONTRACT DAO", contractDbDto.toString())
                        allDocumentRows.add(contractDbDto)
                    }
                    is SheetCursorPosition.EmptyRow -> {
                        _toastMessages.postValue(sheetCursor.error.toString())
                        //TODO: Implement the part for SheetCursor.EmptyRow or show a toast message
                    }
                }
            }
            allDocumentRows.forEach {
                it.id = 0
            }
            _listOfContracts.postValue(allDocumentRows)
        }
    }

    private fun getCell(cell: Cell): Any?{
        return when(cell.cellType){
            Cell.CELL_TYPE_BOOLEAN -> cell.booleanCellValue
            Cell.CELL_TYPE_STRING -> cell.stringCellValue
            Cell.CELL_TYPE_NUMERIC -> cell.numericCellValue
            Cell.CELL_TYPE_FORMULA -> cell.cellFormula
            Cell.CELL_TYPE_ERROR -> cell.errorCellValue
            else -> null
        }
    }
}