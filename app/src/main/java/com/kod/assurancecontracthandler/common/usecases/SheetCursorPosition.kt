package com.kod.assurancecontracthandler.common.usecases

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kod.assurancecontracthandler.model.Contract
import org.apache.poi.ss.formula.functions.T
import java.util.*

sealed class SheetCursorPosition<T: Any>(){
    class BeginningOfSheet(val startDate: Date?, val endDate: Date?) : SheetCursorPosition<T>()
    class HeaderOfSheet(val headers: List<String>) : SheetCursorPosition<T>()
    class EmptyRow() : SheetCursorPosition<T>()
    class RowContent(val content: Contract) : SheetCursorPosition<T>()
    class Footer(val footer: Contract) : SheetCursorPosition<T>()
    class Error(val error: String? = null, val errorLevel: ErrorLevel) : SheetCursorPosition<T>()
}