package com.rajjathedev.expensetracker

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.rajjathedev.expensetracker.adapters.TransactionAdapter
import com.rajjathedev.expensetracker.models.Transaction
import com.rajjathedev.expensetracker.utils.NotificationHelper
import com.rajjathedev.expensetracker.utils.PrefsHelper

class HomeFragment : Fragment() {
    private lateinit var transactions: MutableList<Transaction>
    private lateinit var transactionsAdapter: TransactionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationHelper: NotificationHelper

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.rajjathedev.expensetracker.TRANSACTION_ADDED",
                "com.rajjathedev.expensetracker.BUDGET_UPDATED" -> {
                    refreshData()
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter().apply {
            addAction("com.rajjathedev.expensetracker.TRANSACTION_ADDED")
            addAction("com.rajjathedev.expensetracker.BUDGET_UPDATED")
        }
        requireContext().registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        notificationHelper = NotificationHelper(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            requireContext().unregisterReceiver(updateReceiver)
        } catch (e: Exception) {
            // Ignore if receiver is not registered
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerview)
        transactions = PrefsHelper.loadTransactions(requireContext()).toMutableList()

        setupRecyclerView()
        updateDashboard()

        val addBtn = view.findViewById<ExtendedFloatingActionButton>(R.id.addBtn)
        addBtn.setOnClickListener {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData() {
        transactions = PrefsHelper.loadTransactions(requireContext()).toMutableList()
        transactionsAdapter.updateData(transactions)
        updateDashboard()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        refreshData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView() {
        transactionsAdapter = TransactionAdapter(transactions,
            onEdit = { transaction ->
                val intent = Intent(requireContext(), EditTransactionActivity::class.java).apply {
                    putExtra("TRANSACTION_ID", transaction.id ?: "")
                }
                startActivity(intent)
            },
            onDelete = { transaction ->
                transactions.removeAll { it.id == transaction.id }
                PrefsHelper.saveTransactions(requireContext(), transactions)
                transactionsAdapter.updateData(transactions)
                updateDashboard()
                
                // Notify about the change
                val intent = Intent("com.rajjathedev.expensetracker.TRANSACTION_ADDED").apply {
                    setPackage(requireContext().packageName)
                }
                requireContext().sendBroadcast(intent)
            }
        )

        recyclerView.apply {
            adapter = transactionsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun updateDashboard() {
        val expenseTransactions = transactions.filter { it.amount < 0 }
        val totalExpenses = expenseTransactions.sumOf { it.amount } * -1
        val currentBudget = PrefsHelper.getBudget(requireContext())
        val remainingBudget = currentBudget - totalExpenses
        val currency = PrefsHelper.getCurrency(requireContext())

        view?.findViewById<TextView>(R.id.balance)?.text = "$currency %.2f".format(remainingBudget)
        view?.findViewById<TextView>(R.id.expense)?.text = "$currency %.2f".format(totalExpenses)

        val budgetTextView = view?.findViewById<TextView>(R.id.budget)
        budgetTextView?.text = if (currentBudget > 0) {
            "$currency %.2f".format(currentBudget)
        } else {
            "Not set"
        }

        val colorRes = when {
            currentBudget <= 0 -> R.color.gray
            remainingBudget < 0 -> R.color.red
            remainingBudget < (currentBudget * 0.2) -> R.color.orange
            else -> R.color.green
        }
        budgetTextView?.setTextColor(ContextCompat.getColor(requireContext(), colorRes))

        // Show notifications
        if (currentBudget > 0) {
            when {
                remainingBudget < 0 -> {

                    showBudgetSnackbar(
                        "Budget Exceeded!\n\n" +
                        "Monthly Budget: $currency $currentBudget\n" +
                        "Current Expenses: $currency $totalExpenses\n" +
                        "Remaining: $currency $remainingBudget"
                    )
                    notificationHelper.showBudgetExceededNotification(currentBudget, totalExpenses)
                }
                remainingBudget < (currentBudget * 0.2) -> {
                    showBudgetSnackbar(
                        "Budget Warning!\n\n" +
                        "Monthly Budget: $currency $currentBudget\n" +
                        "Current Expenses: $currency $totalExpenses\n" +
                        "Remaining: $currency $remainingBudget"
                    )
                }
            }
        }
    }

    private fun showBudgetSnackbar(message: String) {
        view?.let { view ->
            Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.red))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .setAction("Dismiss") {  }
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
        }
    }
} 