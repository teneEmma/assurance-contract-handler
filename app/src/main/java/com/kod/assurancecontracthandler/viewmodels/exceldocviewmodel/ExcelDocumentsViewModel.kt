package com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kod.assurancecontracthandler.common.utilities.ModelSchemaStructurer
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.usecases.SheetCursorPosition
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.util.*

class ExcelDocumentsViewModel(application: Application) : AndroidViewModel(application), ModelSchemaStructurer{
    private val _listOfContracts =  MutableLiveData<kotlin.collections.List<ContractDbDto>>()
    val listOfContracts = _listOfContracts

    fun readDocumentContent(path: String){
        viewModelScope.launch {
            val inputStream: FileInputStream
            val file = File("/storage/emulated/0/$path")
            inputStream = FileInputStream(file)
            val workBook = XSSFWorkbook(inputStream)
            val sheet = workBook.getSheetAt(0)
            val allDocumentRows: MutableList<ContractDbDto> = mutableListOf()

            val rowIterator: Iterator<Row> = sheet.iterator()
            while(rowIterator.hasNext()){
                val row = rowIterator.next()
                val cellIterator = row.cellIterator()
                val rowContentRead = mutableListOf<Any?>()
                Log.e("ROW", "Row: ${row.rowNum}")
                while(cellIterator.hasNext()){
                    val cell = cellIterator.next()
                    val cellValue = getCell(cell)

//                        Log.e("COLUMN", "Column: ${cell.columnIndex} has value: $cellValue")
                    rowContentRead.add(cellValue)
//                    Log.e("ROW SCHEME", verifyRowScheme(rowContentRead, sheet).toString())
                }
                val sheetCursor = verifyRowScheme(rowContentRead, sheet)
                if(sheetCursor is SheetCursorPosition.RowContent){
                    val contractDbDto = ContractDbDto(row.rowNum, sheetCursor.content, Date())
                    allDocumentRows.add(contractDbDto)
                }else if(sheetCursor is SheetCursorPosition.BeginningOfSheet){
                    //TODO: Implement the case of end of file
                }
                else if(sheetCursor is SheetCursorPosition.Footer){
                    //TODO: Implement the case of a footer
                }
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