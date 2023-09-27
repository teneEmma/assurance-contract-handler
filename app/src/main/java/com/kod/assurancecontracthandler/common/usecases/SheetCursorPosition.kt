package com.kod.assurancecontracthandler.common.usecases

import com.kod.assurancecontracthandler.model.Contract
import org.apache.poi.ss.formula.functions.T

sealed class SheetCursorPosition<T : Any>() {
    class HeaderOfSheet(val headers: List<String>) : SheetCursorPosition<T>()
    class EmptyRow() : SheetCursorPosition<T>()
    class RowContent(val content: Contract) : SheetCursorPosition<T>()
    class Footer(val footer: Contract) : SheetCursorPosition<T>()
    class Error(val errorResourceId: Int? = null, val errorLevel: ErrorLevel) : SheetCursorPosition<T>()
}