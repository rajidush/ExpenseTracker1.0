package com.rajjathedev.expensetracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.rajjathedev.expensetracker.utils.PrefsHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditTransactionActivity : AppCompatActivity() {
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Initialize all views
        val labelInput = findViewById<TextInputEditText>(R.id.labelInput)
        val amountInput = findViewById<TextInputEditText>(R.id.amountInput)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val dateInput = findViewById<EditText>(R.id.dateInput)
        val updateBtn = findViewById<Button>(R.id.addTransactionBtn)
        val closeBtn = findViewById<ImageButton>(R.id.closeBtn)

        // Setup category spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        // Setup date picker
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateInput.setOnClickListener {
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    dateInput.setText(dateFormat.format(calendar.time))
                },
                currentYear,
                currentMonth,
                currentDay
            )

            // Set min and max date to current month
            val minCalendar = Calendar.getInstance().apply {
                set(currentYear, currentMonth, 1)
            }
            val maxCalendar = Calendar.getInstance().apply {
                set(currentYear, currentMonth, getActualMaximum(Calendar.DAY_OF_MONTH))
            }

            datePickerDialog.datePicker.minDate = minCalendar.timeInMillis
            datePickerDialog.datePicker.maxDate = maxCalendar.timeInMillis
            datePickerDialog.show()
        }

        val transactionId = intent.getStringExtra("TRANSACTION_ID")
        val transaction = PrefsHelper.loadTransactions(this)
            .firstOrNull { it.id == transactionId } ?: run {
            finish()
            return
        }

        // Pre-fill form
        labelInput.setText(transaction.label)
        amountInput.setText(transaction.amount.toString())
        dateInput.setText(transaction.date)

        // Set category if available
        val categories = resources.getStringArray(R.array.categories)
        val categoryPosition = categories.indexOf(transaction.category)
        if (categoryPosition != -1) {
            categorySpinner.setSelection(categoryPosition)
        }

        // Update button
        updateBtn.text = "Update Transaction"
        updateBtn.setOnClickListener {
            // Validate inputs
            if (labelInput.text.isNullOrEmpty() || amountInput.text.isNullOrEmpty() || dateInput.text.isNullOrEmpty()) {
                return@setOnClickListener
            }

            val updatedTransaction = transaction.copy(
                label = labelInput.text.toString(),
                amount = amountInput.text.toString().toDouble(),
                category = categorySpinner.selectedItem.toString(),
                date = dateInput.text.toString()
            )

            val updatedList = PrefsHelper.loadTransactions(this)
                .map { if (it.id == transactionId) updatedTransaction else it }
            PrefsHelper.saveTransactions(this, updatedList)
            
            val intent = Intent("com.rajjathedev.expensetracker.TRANSACTION_ADDED").apply {
                setPackage(packageName)
            }
            sendBroadcast(intent)
            
            setResult(RESULT_OK)
            finish()
        }

        closeBtn.setOnClickListener {
            finish()
        }
    }
}