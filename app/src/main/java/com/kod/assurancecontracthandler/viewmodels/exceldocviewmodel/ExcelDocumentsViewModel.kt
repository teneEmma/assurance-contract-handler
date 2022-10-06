package com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.kod.assurancecontracthandler.common.utilities.ModelSchemaStructurer
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream

class ExcelDocumentsViewModel(application: Application) : AndroidViewModel(application), ModelSchemaStructurer{

    fun readDocumentContent(path: String){
        val inputStream: FileInputStream
        val file = File("/storage/emulated/0/$path")

            inputStream = FileInputStream(file)
            val workBook = XSSFWorkbook(inputStream)
            val sheet = workBook.getSheetAt(0)

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
                Log.e("Cursor Position", sheetCursor.toString())
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