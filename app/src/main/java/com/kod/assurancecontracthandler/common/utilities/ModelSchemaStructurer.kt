package com.kod.assurancecontracthandler.common.utilities

import android.util.Log
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.common.usecases.SheetCursorPosition
import org.apache.poi.ss.formula.functions.T
import java.util.*

interface ModelSchemaStructurer {

    fun verifyRowScheme(row: List<Any?>, header: List<String>): SheetCursorPosition<T> {
//        Log.e("ROW CONTENT", row.toString())
        if(row.isEmpty())
            return SheetCursorPosition.EmptyRow()

        var firstValue : Any = ""
        row[0]?.let { firstValue = it }
        val sheetTitle = ConstantsVariables.possibleBeginningSheetValues
        val headerValue = ConstantsVariables.possibleHeaderValues
        val timeConverter = TimeConverters()

        return when{
            row.any{ it.toString().uppercase().contains(sheetTitle[0].uppercase())
                    || it.toString().uppercase().contains(sheetTitle[1].uppercase())}->
                SheetCursorPosition.BeginningOfSheet(
                    timeConverter.dateFromExcelHeader(
                        firstValue.toString().uppercase()
                    )["START_DATE"],
                    timeConverter.dateFromExcelHeader(firstValue.toString().uppercase())["END_DATE"]
                )
            row.any{it.toString().uppercase().contains(headerValue[0].uppercase()) ||
                    firstValue.toString().uppercase().contains(headerValue[1].uppercase())}->
                @Suppress("UNCHECKED_CAST") SheetCursorPosition.HeaderOfSheet(
                    row as List<String>
                )
            firstValue is Double && row.getOrNull(1) != null && row.getOrNull(6) != null
            -> setContractObj(row as MutableList<Any?>, header as MutableList<String>)
            else -> setFooter(row as MutableList<Any?>, header as? MutableList<String>)
        }
    }

    fun setFooter(parameters: MutableList<Any?>, header: MutableList<String>?): SheetCursorPosition<T>{
        if (header.isNullOrEmpty())
            return SheetCursorPosition.EmptyRow()

        val firstStr = parameters[0] as? String
        val contract = Contract(0)
        if (firstStr?.contains("TOTAL") == true && parameters[2] == null){

            contract.assure = "SOMME"
            contract.DTA = parameters.getOrNull(header.indexOf("DTA")).toString().toIntOrNull()
            contract.PN = parameters.getOrNull(header.indexOf("PN")).toString().toIntOrNull()
            contract.ACC = parameters.getOrNull(header.indexOf("ACC")).toString().toIntOrNull()
            contract.FC = parameters.getOrNull(header.indexOf("FC")).toString().toIntOrNull()
            contract.TVA = parameters.getOrNull(header.indexOf("TVA")).toString().toIntOrNull()
            contract.CR = parameters.getOrNull(header.indexOf("CR")).toString().toIntOrNull()
            contract.PTTC = parameters.getOrNull(header.indexOf("PTTC")).toString().toIntOrNull()
            contract.COM_PN = parameters.getOrNull(header.indexOf("COM PN")).toString().toIntOrNull()
            contract.COM_ACC = parameters.getOrNull(header.indexOf("COM ACC")).toString().toIntOrNull()
            contract.TOTAL_COM = parameters.getOrNull(header.indexOf("TOTAL COM")).toString().toIntOrNull()
            contract.NET_A_REVERSER = parameters.getOrNull(header.indexOf("NET A REVERSER")).toString().toIntOrNull()
            contract.ENCAIS = parameters.getOrNull(header.indexOf("ENCAIS")).toString().toIntOrNull()
            contract.COMM_LIMBE = parameters.getOrNull(header.indexOf("COMM LIMBE")).toString().toIntOrNull()
            contract.COMM_APPORT = parameters.getOrNull(header.indexOf("COMM APPORT")).toString().toIntOrNull()

        }else if(firstStr?.contains("DTA") == true){
            contract.assure = "DTA"
            contract.DTA = parameters[2].toString().toIntOrNull()
        }else if (firstStr?.contains("TOTAL") == true && parameters[2] != null){
            contract.assure = "TOTAL"
            contract.DTA = parameters[2].toString().toIntOrNull()
        }else if (firstStr?.contains("PRIME") == true){
            contract.assure = "PRIME A REVERSER"
            contract.DTA = parameters[2].toString().toIntOrNull()
        }
        return SheetCursorPosition.Footer(contract)
    }

    private fun setContractObj(parameter: List<Any?>, header: MutableList<String>): SheetCursorPosition<T> {
//        val headers = ConstantsVariables.headerNames
        val result = Contract(0)
//        val iterator = headers.iterator()

        doubleToInt(parameter.getOrNull(header.indexOf("N°")).toString())?.let { result.index = it }
        result.attestation = parameter.getOrNull(header.indexOf("ATTESTATION")).toString()
        result.carteRose = doubleToInt(parameter.getOrNull(header.indexOf("CARTE ROSE"))).toString()
        result.numeroPolice = parameter.getOrNull(header.indexOf("N°POLICE")).toString()
        result.compagnie = parameter.getOrNull(header.indexOf("COMPAGNIE")).toString()
        result.telephone = phoneNumbValidation(parameter.getOrNull(header.indexOf("TEL")).toString())
        result.assure = parameter.getOrNull(header.indexOf("ASSURE")).toString()
        result.effet = parameter.getOrNull(header.indexOf("EFFET")) as? Date
        result.echeance = parameter.getOrNull(header.indexOf("ECHEANCE")) as? Date
        result.puissanceVehicule = parameter.getOrNull(header.indexOf("PUISS / ENERGIE")).toString()
        result.mark = parameter.getOrNull(header.indexOf("MARK")).toString()
        result.immatriculation = parameter.getOrNull(header.indexOf("IMMATRICULATION")).toString()
        result.categorie = doubleToInt(parameter.getOrNull(header.indexOf("CATEGORIE")))
        result.zone = parameter.getOrNull(header.indexOf("ZONE")).toString()
        result.duree = doubleToInt(parameter.getOrNull(header.indexOf("DUREE")).toString())
        result.DTA = doubleToInt(parameter.getOrNull(header.indexOf("DTA")))
        result.PN = doubleToInt(parameter.getOrNull(header.indexOf("PN")))
        result.ACC = doubleToInt(parameter.getOrNull(header.indexOf("ACC")))
        result.FC = doubleToInt(parameter.getOrNull(header.indexOf("FC")))
        result.TVA = doubleToInt(parameter.getOrNull(header.indexOf("TVA")))
        result.CR = doubleToInt(parameter.getOrNull(header.indexOf("CR")))
        result.PTTC = doubleToInt(parameter.getOrNull(header.indexOf("PTTC")))
        result.COM_PN = doubleToInt(parameter.getOrNull(header.indexOf("COM PN")))
        result.COM_ACC = doubleToInt(parameter.getOrNull(header.indexOf("COM ACC")))
        result.TOTAL_COM = doubleToInt(parameter.getOrNull(header.indexOf("TOTAL COM")))
        result.NET_A_REVERSER = doubleToInt(parameter.getOrNull(header.indexOf("NET A REVERSER")))
        result.ENCAIS = doubleToInt(parameter.getOrNull(header.indexOf("ENCAIS")))
        result.COMM_LIMBE = doubleToInt(parameter.getOrNull(header.indexOf("COMM LIMBE")))
        result.COMM_APPORT = doubleToInt(parameter.getOrNull(header.indexOf("COMM APPORT")))
        result.APPORTEUR = parameter.getOrNull(header.indexOf("APPORTEUR")).toString()

//        while (iterator.hasNext()){
//            val curString = iterator.next()
//            if(header.getOrNull(0) == curString) {
//                doubleToInt(parameter.getOrNull(0))?.also { result.index = it }
//            }
//            if(header.getOrNull(1) == curString) {
//                (parameter.getOrNull(1) as? String)?.let { result.attestation = it }
//            }
//            if(header.getOrNull(2) == curString) {
//                doubleToInt(parameter.getOrNull(2))?.toString()?.let { result.carteRose = it }
//            }
//            if(header.getOrNull(3) == curString) {
//                (parameter.getOrNull(3).toString() as? String)?.let { result.numeroPolice = it }
//            }
//            if(header.getOrNull(4) == curString) {
//                (parameter.getOrNull(4) as? String)?.let { result.compagnie = it }
//            }
//            if(header.getOrNull(5) == curString) {
//                phoneNumbValidation(parameter.getOrNull(5))?.let { result.telephone = it }
//            }
//            if(header.getOrNull(6) == curString) {
//                (parameter.getOrNull(6) as? String).let { result.assure = it }
//            }
//            if(header.getOrNull(7) == curString) {
//                (parameter.getOrNull(7) as? Date)?.let { result.effet = it }
//            }
//            if(header.getOrNull(8) == curString) {
//                (parameter.getOrNull(8) as? Date)?.let { result.echeance = it }
//            }
//            if(header.getOrNull(9) == curString) {
//                (parameter.getOrNull(9) as? String)?.let { result.puissanceVehicule = it }
//            }
//            if(header.getOrNull(10) == curString) {
//                (parameter.getOrNull(10) as? String)?.let { result.mark = it }
//            }
//            if(header.getOrNull(11) == curString) {
//                (parameter.getOrNull(11) as? String)?.let { result.immatriculation = it }
//            }
//            if(header.getOrNull(12) == curString) {
//                doubleToInt(parameter.getOrNull(12))?.let { result.categorie = it }
//            }
//            if(header.getOrNull(13) == curString) {
//                (parameter.getOrNull(13) as? String)?.let { result.zone = it }
//            }
//            if(header.getOrNull(14) == curString) {
//                doubleToInt(parameter.getOrNull(14))?.let { result.duree = it }
//            }
//            if(header.getOrNull(15) == curString) {
//                doubleToInt(parameter.getOrNull(15))?.let { result.DTA = it }
//            }
//            if(header.getOrNull(16) == curString) {
//                doubleToInt(parameter.getOrNull(16))?.let { result.PN = it }
//            }
//            if(header.getOrNull(17) == curString) {
//                doubleToInt(parameter.getOrNull(17))?.let { result.ACC = it }
//            }
//            if(header.getOrNull(18) == curString) {
//                doubleToInt(parameter.getOrNull(18))?.let { result.FC = it }
//            }
//            if(header.getOrNull(19) == curString) {
//                doubleToInt(parameter.getOrNull(19))?.let { result.TVA = it }
//            }
//            if(header.getOrNull(20) == curString) {
//                doubleToInt(parameter.getOrNull(20))?.let { result.CR = it }
//            }
//            if(header.getOrNull(21) == curString) {
//                doubleToInt(parameter.getOrNull(21))?.let { result.PTTC = it }
//            }
//            if(header.getOrNull(22) == curString) {
//                doubleToInt(parameter.getOrNull(22))?.let { result.COM_PN = it }
//            }
//            if(header.getOrNull(23) == curString) {
//                doubleToInt(parameter.getOrNull(23))?.let { result.COM_ACC = it }
//            }
//            if(header.getOrNull(24) == curString) {
//                doubleToInt(parameter.getOrNull(24))?.let { result.TOTAL_COM = it }
//            }
//            if(header.getOrNull(25) == curString) {
//                doubleToInt(parameter.getOrNull(25))?.let { result.NET_A_REVERSER = it }
//            }
//            if(header.getOrNull(26) == curString) {
//                doubleToInt(parameter.getOrNull(26))?.let { result.ENCAIS = it}
//            }
//            if(header.getOrNull(27) == curString) {
//                doubleToInt(parameter.getOrNull(27))?.let { result.COMM_LIMBE = it }
//            }
//            if(header.getOrNull(28) == curString) {
//                doubleToInt(parameter.getOrNull(28))?.let { result.COMM_APPORT = it }
//            }
//            if(header.getOrNull(29) == curString) {
//                (parameter.getOrNull(29) as? String)?.let { result.APPORTEUR = it }
//            }
//        }

        return  SheetCursorPosition.RowContent(result)
    }

    private fun phoneNumbValidation(parameter: Any?): Long? {
        return if(parameter is String?){
            if (parameter?.contains(".*[0-9].*".toRegex()) == true) {
                var strNumber = ""
                parameter?.split('-')?.forEach { strNumber += it }
                strNumber.toLongOrNull()
            }else null
        }
        else null
    }

    private fun doubleToInt(parameter: Any?): Int? {
        return if(parameter is Double?){
            var strContainer: String? = parameter.toString()
            if(strContainer?.contains(".*[.E].*".toRegex()) == true){
                if (strContainer.contains('E')){
                    strContainer = strContainer.split("E")[0]
                }
                val splitString = strContainer.split('.')
                val value = if(splitString[1] == "0") splitString[0] else splitString[0] + splitString[1]
                value.toInt()
            }
            else parameter?.toInt()
        }else if (parameter is Int?) parameter
        else null
    }
}