package com.rajjathedev.expensetracker.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rajjathedev.expensetracker.R
import com.rajjathedev.expensetracker.models.Transaction
import com.rajjathedev.expensetracker.utils.PrefsHelper
import kotlin.math.abs
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {


    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    class TransactionHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.label)
        val amount: TextView = view.findViewById(R.id.amount)
        val date: TextView = view.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_layout, parent, false)
        return TransactionHolder(view)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.amount.context
        val currency = PrefsHelper.getCurrency(context)

        if(transaction.amount >= 0) {
            holder.amount.text = "+ $currency%.2f".format(transaction.amount)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green))
        }
        else {
            holder.amount.text = "- $currency%.2f".format(abs(transaction.amount))
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red))
        }
        holder.label.text = transaction.label


        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            val date = inputFormat.parse(transaction.date)
            holder.date.text = date?.let { outputFormat.format(it) }
        } catch (e: Exception) {
            holder.date.text = transaction.date
        }

        holder.itemView.setOnClickListener { onEdit(transaction) }

        holder.itemView.findViewById<ImageButton>(R.id.deleteBtn).setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete") { _, _ ->
                    onDelete(transaction)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}