package com.kod.assurancecontracthandler.model

/**
 *  The different types of files or Excel files we should ask permission for
 *  @param EXCEL_2007 Excel files with .xls extension
 *  @param EXCEL_2007_SUP Excel files with .xlsx extension
 */
enum class MimeTypes(val value: String) {
    EXCEL_2007("application/vnd.ms-excel"),
    EXCEL_2007_SUP("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
}