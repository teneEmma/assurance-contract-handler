package com.kod.assurancecontracthandler.common.utilities

import android.util.Log
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.usecases.SheetCursorPosition
import org.apache.poi.ss.formula.functions.T
import java.util.*
import kotlin.collections.HashMap

interface ModelSchemaStructurer {

    fun verifyRowScheme(row: List<Any?>, header: List<String>): SheetCursorPosition<T>{
        Log.e("ROW CONTENT", row.toString())
        return when{
            row.isNotEmpty() && row[3] is String && row[7] !is Double ->{
                val timeConverters = TimeConverters()
                if(row.any{ it.toString().contains("SEMAINE DU")}){
                    Log.e("DATE", timeConverters.dateFromExcelHeader(row[3].toString()).toString())
                    SheetCursorPosition.BeginningOfSheet(
                        timeConverters.dateFromExcelHeader(row[3].toString())["startDate"]!!,
                        timeConverters.dateFromExcelHeader(row[3].toString())["endDate"]!!)
                }else if(row.any{ it.toString().contains("MOIS DE")}){
                    SheetCursorPosition.BeginningOfSheet(
                        timeConverters.dateFromExcelHeader(row[3].toString())["startDate"]!!,
                        timeConverters.dateFromExcelHeader(row[3].toString())["endDate"]!!)
                }else if(row.any { it.toString().contains("ATTESTATION") }){
                    @Suppress("UNCHECKED_CAST")
                    SheetCursorPosition.HeaderOfSheet(row as List<String>)
                }
                else{
                    SheetCursorPosition.Footer()
                }
            }
            row.isNotEmpty() && row[0] is Double -> {
                setContractObj(row as MutableList<Any?>, header as MutableList<String>)
            }
            else -> SheetCursorPosition.EmptyRow()
        }
    }

    private fun validateValues(params: MutableList<Any?>): MutableList<Any?>{
        params.forEachIndexed { valueIndex, _ ->
            when {
                params[valueIndex] is Double -> {
                    if(params[valueIndex].toString().contains(".*[.E].*".toRegex()) ){
                        if (ConstantsVariables.contractIntTypes.contains(valueIndex)){
//                            Log.e("isDOUBLE $valueIndex", params[valueIndex].toString())
                            params[valueIndex] =
                                convertAStringContainingDoubleToInt(params[valueIndex].toString())
                        }else{
                            params[valueIndex] = params[valueIndex].toString()
                        }
                    }else{
                        params[valueIndex] = null
                    }
                }
                params[valueIndex] is String -> {
//                    Log.e("isString $valueIndex", params[valueIndex].toString())
                    if (ConstantsVariables.contractIntTypes.contains(valueIndex)){
                        params[valueIndex] = null
                    }else {
                        params[valueIndex] = params[valueIndex].toString()
                    }
                }
            }
        }
        return params
    }
    private fun convertAStringContainingDoubleToInt(str: String): Int?{
        var string = str
        if (string.contains('E')){
            string = string.split("E")[0]
        }
        val splitString = string.split('.')
        var value = splitString[0] + splitString[1]
        if(splitString[1]=="0"){
            value = splitString[0]
        }
        return value.toIntOrNull()
    }
    private fun setContractObj(parameter: MutableList<Any?>, header: MutableList<String>): SheetCursorPosition<T> {

            val params: MutableList<Any?>
            if (parameter.size == ConstantsVariables.sizeOfRow){
                params = validateValues(parameter)
            }else{
                val originalHeader = ConstantsVariables.headerNames
                val notFoundHeaders: HashMap<String, Int> = hashMapOf()

                originalHeader.forEachIndexed { index, headerValue ->
                    if(header[index] != headerValue){
                        notFoundHeaders[headerValue] = index
                    }
                }
                notFoundHeaders.forEach { (notFoundValue, index) ->
                    if(!originalHeader.contains(notFoundValue)){
                        originalHeader.drop(index)
                        parameter[index] = null
                    }
                }
                params = validateValues(parameter)
            }

//        Log.e("PARAMS", params.toString())
        params[2] = params[2].toString()
//        Log.e("type of params2", params[2]!!::class.toString())
//        Log.e("value of params2", params[2].toString())

        return try {
            val contract = Contract(
                params[0] as Int,
                params[1] as String?,
                params[2] as String?,
                params[3] as String?,
                params[4] as String?,
                params[5] as String?,
                params[6] as String?,
                params[7] as Date?,
                params[8] as Date?,
                params[9] as String?,
                params[10] as String?,
                params[11] as String?,
                params[12] as Int?,
                params[13] as String?,
                params[14] as Int?,
                params[15] as Int?,
                params[16] as Int?,
                params[17] as Int?,
                params[18] as Int?,
                params[19] as Int?,
                params[20] as Int?,
                params[21] as Int?,
                params[22] as Int?,
                params[23] as Int?,
                params[24] as Int?,
                params[25] as Int?,
                params[26] as Int?,
                params[27] as Int?,
                params[28] as Int?,
                params[29] as String?
            )
            Log.e("CONTRACT", contract.toString())
            SheetCursorPosition.RowContent(contract)
        }catch (e: java.lang.Exception){
            Log.e("ERROR======", e.message.toString())
            SheetCursorPosition.EmptyRow(e.message)
        }
    }
}