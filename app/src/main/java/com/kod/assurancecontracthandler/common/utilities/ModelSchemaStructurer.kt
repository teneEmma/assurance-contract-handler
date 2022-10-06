package com.kod.assurancecontracthandler.common.utilities

import android.util.Log
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.usecases.SheetCursorPosition
import org.apache.poi.ss.formula.functions.T
import org.apache.poi.ss.usermodel.Sheet

interface ModelSchemaStructurer {

    fun verifyRowScheme(row: List<Any?>, sheet: Sheet): SheetCursorPosition<T>{
        Log.e("ROW CONTENT", row.toString())
        return when{
            row.isNotEmpty() && row[0] is String ->{
                if(row.any{ it.toString().contains("PRODUCTION ORION")} ||
                    row.any{ it.toString().contains("SEMAINE DU")}){
                    SheetCursorPosition.BeginningOfSheet(sheet.sheetName)
                }else if(row.any { it.toString().contains("ATTESTATION") }){
                    SheetCursorPosition.BeginningOfSheet(sheet.sheetName)
                }
                else{
                    SheetCursorPosition.Footer()
                }
            }
            row.isNotEmpty() && row[0] is Double -> {
                val contract : Contract = setContractObj(row)
//                Log.e("List Of Contracts", contract.toString())
                SheetCursorPosition.RowContent(contract)
            }
            else -> SheetCursorPosition.EmptyRow()
        }
    }

    private fun setContractObj(params: List<Any?>): Contract {
        return Contract(
            params[0] as Double,
            params[1] ,
            params[2] ,
            params[3] ,
            params[4] ,
            params[5],
            params[6] ,
            params[7] ,
            params[8] ,
            params[9] ,
            params[10] ,
            params[11] ,
            params[12] ,
            params[13] ,
            params[14] ,
            params[15],
            params[16] ,
            params[17] ,
            params[18] ,
            params[19] ,
            params[20] ,
            params[21] ,
            params[22] ,
            params[23] ,
            params[24] ,
            params[25],
            params[26] ,
            params[27] ,
            params[28],
            params[29]
        )
    }
}