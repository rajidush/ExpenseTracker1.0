package com.rajjathedev.expensetracker.models

import java.util.UUID

data class Transaction (
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val amount: Double,
    val category: String,
    val date: String
) {

}
