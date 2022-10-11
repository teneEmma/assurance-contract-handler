package com.kod.assurancecontracthandler.usecases

import com.kod.assurancecontracthandler.model.Contract
import org.apache.poi.ss.formula.functions.T

sealed class SheetCursorPosition<T: Any>(){
    class BeginningOfSheet() : SheetCursorPosition<T>()
    class HeaderOfSheet(val headers: List<String>) : SheetCursorPosition<T>()
    class EmptyRow(val error: String? = null) : SheetCursorPosition<T>()
    class RowContent(val content: Contract) : SheetCursorPosition<T>()
    class Footer : SheetCursorPosition<T>()
}