package com.rajjathedev.expensetracker

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.rajjathedev.expensetracker.utils.PrefsHelper

class SpendingAnalysisFragment : Fragment() {
    private lateinit var pieChart: PieChart
    private lateinit var totalExpenseView: MaterialTextView
    private lateinit var budgetCard: MaterialCardView
    private lateinit var budgetStatusView: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_spending_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pieChart = view.findViewById(R.id.pieChart)
        totalExpenseView = view.findViewById(R.id.totalExpense)
        budgetCard = view.findViewById(R.id.budgetCard)
        budgetStatusView = view.findViewById(R.id.budgetStatus)

        setupPieChart()
        updateAnalysis()
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400)
            legend.isEnabled = true
        }
    }

    private fun updateAnalysis() {
        val transactions = PrefsHelper.loadTransactions(requireContext())
        val expenseTransactions = transactions.filter { it.amount < 0 }
        val totalExpenses = expenseTransactions.sumOf { it.amount } * -1

        // Group expenses by category
        val categoryMap = expenseTransactions.groupBy { it.category }
            .mapValues { (_, transactions) -> 
                transactions.sumOf { it.amount } * -1 
            }

        // Create pie chart entries
        val entries = categoryMap.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()

        // Update total expense
        totalExpenseView.text = "Total Expenses: Rs %.2f".format(totalExpenses)

        // Update budget status
        val currentBudget = PrefsHelper.getBudget(requireContext())
        if (currentBudget > 0) {
            val remainingBudget = currentBudget - totalExpenses
            val percentageUsed = (totalExpenses / currentBudget) * 100

            budgetStatusView.text = when {
                percentageUsed >= 100 -> "Budget Exceeded!"
                percentageUsed >= 80 -> "Warning: Budget nearly exceeded"
                else -> "Budget Status: %.1f%% used".format(percentageUsed)
            }

            budgetCard.setCardBackgroundColor(
                when {
                    percentageUsed >= 100 -> resources.getColor(R.color.red, null)
                    percentageUsed >= 80 -> resources.getColor(R.color.orange, null)
                    else -> resources.getColor(R.color.green, null)
                }
            )
        } else {
            budgetStatusView.text = "No budget set"
            budgetCard.setCardBackgroundColor(resources.getColor(R.color.gray, null))
        }
    }

    override fun onResume() {
        super.onResume()
        updateAnalysis()
    }
}