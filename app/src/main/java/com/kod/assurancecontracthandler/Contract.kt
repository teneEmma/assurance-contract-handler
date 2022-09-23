package com.kod.assurancecontracthandler

import java.util.*

data class Contract(
    val firstName: String ="",
    val lastName: String = "",
    val gender: String = "MALE",
    val country: String = "",
    val age: Int = 18,
    val date: Date,
    val id: Long
)
