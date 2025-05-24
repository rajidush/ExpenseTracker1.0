package com.rajjathedev.expensetracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.rajjathedev.expensetracker.models.Transaction
import com.rajjathedev.expensetracker.utils.PrefsHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class AddTransactionActivity : AppCompatActivity() {
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        val addTransactionBtn = findViewById<Button>(R.id.addTransactionBtn)
        val labelInput = findViewById<TextInputEditText>(R.id.labelInput)
        val amountInput = findViewById<TextInputEditText>(R.id.amountInput)
        val labelLayout = findViewById<TextInputLayout>(R.id.labelLayout)
        val amountLayout = findViewById<TextInputLayout>(R.id.amountLayout)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val dateInput = findViewById<EditText>(R.id.dateInput)
        val closeBtn = findViewById<ImageButton>(R.id.closeBtn)


        labelInput.apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                       android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                       android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            setTextIsSelectable(false)
            isLongClickable = false
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = true
            isCursorVisible = true
            isSingleLine = true
        }


        labelInput.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            val filtered = source.subSequence(start, end).toString().replace(Regex("[^a-zA-Z\\s]"), "")
            if (filtered != source.subSequence(start, end).toString()) {
                labelLayout.error = "Only letters and spaces are allowed"
            } else {
                labelLayout.error = null
            }
            filtered
        })

        // Setup category spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        // Date picker
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

        labelInput.addTextChangedListener {
            if (it!!.count() > 0) {
                if (it.toString().matches(Regex("^[a-zA-Z\\s]*$"))) {
                    labelLayout.error = null
                } else {
                    labelLayout.error = "Only letters and spaces are allowed"
                }
            }
        }

        amountInput.addTextChangedListener {
            if (it!!.count() > 0)
                amountLayout.error = null
        }

        addTransactionBtn.setOnClickListener {
            val label = labelInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val category = categorySpinner.selectedItem.toString()
            val date = dateInput.text.toString()

            if (validateInput(label, amount, date)) {
                val transaction = Transaction(
                    label = label,
                    amount = -abs(amount ?: 0.0),
                    category = category,
                    date = date
                )

                val existing = PrefsHelper.loadTransactions(this)
                PrefsHelper.saveTransactions(this, existing + transaction)

                // Notify HomeFragment to refresh
                val intent = Intent("com.rajjathedev.expensetracker.TRANSACTION_ADDED").apply {
                    setPackage(packageName)
                }
                sendBroadcast(intent)

                setResult(RESULT_OK)
                finish()
            }
        }

        closeBtn.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(label: String, amount: Double?, date: String): Boolean {
        var isValid = true
        val labelLayout = findViewById<TextInputLayout>(R.id.labelLayout)
        val amountLayout = findViewById<TextInputLayout>(R.id.amountLayout)
        val dateLayout = findViewById<EditText>(R.id.dateInput)

        if (amount == null || amount <= 0) {
            amountLayout.error = "Amount must be positive"
            return false
        }
        if (label.isEmpty()) {
            labelLayout.error = "Title required"
            isValid = false
        } else if (!label.matches(Regex("^[a-zA-Z\\s]*$"))) {
            labelLayout.error = "Only letters and spaces are allowed"
            isValid = false
        }
        if (amount == null) {
            amountLayout.error = "Invalid amount"
            isValid = false
        }
        if (date.isEmpty()) {
            dateLayout.error = "Date required"
            isValid = false
        }
        return isValid
    }
}