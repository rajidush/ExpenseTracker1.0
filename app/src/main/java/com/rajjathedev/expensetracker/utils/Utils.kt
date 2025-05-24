package com.rajjathedev.expensetracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rajjathedev.expensetracker.models.Transaction
import java.util.*

object PrefsHelper {
    private const val PREFS_NAME = "ExpenseTrackerPrefs"
    private const val KEY_TRANSACTIONS = "transactions"
    private const val KEY_MONTHLY_BUDGET = "monthly_budget"
    private const val KEY_CURRENCY = "currency"
    private const val KEY_LAST_MONTH_RESET = "last_month_reset"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val json = Gson().toJson(transactions)
        getPrefs(context).edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    fun loadTransactions(context: Context): List<Transaction> {
        val json = getPrefs(context).getString(KEY_TRANSACTIONS, "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    fun saveBudget(context: Context, amount: Double) {
        getPrefs(context).edit().putFloat(KEY_MONTHLY_BUDGET, amount.toFloat()).apply()

        val calendar = Calendar.getInstance()
        getPrefs(context).edit().putInt(KEY_LAST_MONTH_RESET, calendar.get(Calendar.MONTH)).apply()
    }

    fun getBudget(context: Context): Double {

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val lastMonthReset = getPrefs(context).getInt(KEY_LAST_MONTH_RESET, currentMonth)

        if (currentMonth != lastMonthReset) {

            getPrefs(context).edit().putInt(KEY_LAST_MONTH_RESET, currentMonth).apply()
            return 0.0
        }

        return getPrefs(context).getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
    }

    fun saveCurrency(context: Context, currency: String) {
        getPrefs(context).edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(context: Context): String {
        return getPrefs(context).getString(KEY_CURRENCY, "Rs") ?: "Rs"
    }

    fun formatAmount(context: Context, amount: Double): String {
        val currency = getCurrency(context)
        return "$currency %.2f".format(amount)
    }
}