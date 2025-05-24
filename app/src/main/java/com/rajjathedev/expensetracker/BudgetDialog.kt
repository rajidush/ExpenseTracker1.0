package com.rajjathedev.expensetracker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText

class BudgetDialog(private val context: Context, private val onBudgetSet: (Double) -> Unit) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_budget)

        val budgetInput = findViewById<TextInputEditText>(R.id.budgetInput)
//        val setButton = findViewById<Button>(R.id.setBudgetBtn)
//
//        setButton.setOnClickListener {
//            val amount = budgetInput.text.toString().toDoubleOrNull()
//            if (amount != null && amount > 0) {
//                onBudgetSet(amount)
//                dismiss()
//            } else {
//                budgetInput.error = "Enter valid amount"
//            }
//        }
    }
}