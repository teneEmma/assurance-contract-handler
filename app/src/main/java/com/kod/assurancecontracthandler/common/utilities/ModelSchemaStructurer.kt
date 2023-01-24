package com.kod.assurancecontracthandler.common.utilities

import android.util.Log
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.common.usecases.SheetCursorPosition
import org.apache.poi.ss.formula.functions.T
import java.util.*

interface ModelSchemaStructurer {

    fun verifyRowScheme(row: List<Any?>, header: List<String>): SheetCursorPosition<T> {
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
        val result = Contract(0)

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
                    for(n in strContainer.length until 9){
                        strContainer += '0'
                    }
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