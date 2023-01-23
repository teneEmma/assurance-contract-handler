package com.kod.assurancecontracthandler

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

fun main(args: Array<String>){
    println("today in Long: "+LocalDateTime.now().minusDays(23).toEpochSecond(ZoneOffset.UTC)*1000)
    val plus1month = LocalDateTime.now().plusDays(8).toEpochSecond(ZoneOffset.UTC)*1000
    println("+1 month in Long: $plus1month")
    println(SimpleDateFormat("dd/MM/yyyy").format(Date(1650019560000)))
}