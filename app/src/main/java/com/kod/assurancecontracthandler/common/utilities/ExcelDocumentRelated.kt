import com.kod.assurancecontracthandler.model.Contract

object ExcelDocumentRelated {
    fun getEntryId(contract: Contract) = "${contract.attestation}_${contract.carteRose}_${contract.assure}"
}