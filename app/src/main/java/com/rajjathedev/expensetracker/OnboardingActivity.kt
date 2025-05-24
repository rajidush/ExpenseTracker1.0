package com.rajjathedev.expensetracker

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.rajjathedev.expensetracker.adapters.OnboardingAdapter
import com.rajjathedev.expensetracker.models.OnboardingItem

class OnboardingActivity : AppCompatActivity() {

    private lateinit var onboardingViewPager: ViewPager2
    private lateinit var indicatorsContainer: LinearLayout
    private lateinit var buttonNext: MaterialButton
    private lateinit var buttonSkip: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        onboardingViewPager = findViewById(R.id.onboardingViewPager)
        indicatorsContainer = findViewById(R.id.indicatorsContainer)
        buttonNext = findViewById(R.id.buttonNext)
        buttonSkip = findViewById(R.id.buttonSkip)

        setupOnboardingItems()
        setupOnboardingIndicators()
        setCurrentOnboardingIndicator(0)

        onboardingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentOnboardingIndicator(position)

                if (position == 2) {
                    buttonNext.text = "Get Started"
                } else {
                    buttonNext.text = "Next"
                }
            }
        })

        buttonNext.setOnClickListener {
            if (onboardingViewPager.currentItem + 1 < 3) {
                onboardingViewPager.currentItem = onboardingViewPager.currentItem + 1
            } else {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
        }

        buttonSkip.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    private fun setupOnboardingItems() {
        val onboardingItems: MutableList<OnboardingItem> = ArrayList()

        val trackExpensesItem = OnboardingItem(
            R.drawable.ic_track_expenses,
            "Track Your Expenses",
            "Easily record and categorize your daily expenses to understand your spending habits."
        )

        val budgetGoalsItem = OnboardingItem(
            R.drawable.ic_budget_goals,
            "Set Budget Goals",
            "Create personalized budget goals and get alerts when you're approaching your limits."
        )

        val insightsItem = OnboardingItem(
            R.drawable.ic_insights,
            "Get Financial Insights",
            "View detailed reports and analytics to make smarter financial decisions."
        )

        onboardingItems.add(trackExpensesItem)
        onboardingItems.add(budgetGoalsItem)
        onboardingItems.add(insightsItem)

        val onboardingAdapter = OnboardingAdapter(onboardingItems)
        onboardingViewPager.adapter = onboardingAdapter
    }

    private fun setupOnboardingIndicators() {
        val indicators = arrayOfNulls<ImageView>(3)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.onboarding_indicator_inactive
                )
            )
            indicators[i]?.layoutParams = layoutParams
            indicatorsContainer.addView(indicators[i])
        }
    }

    private fun setCurrentOnboardingIndicator(position: Int) {
        for (i in 0 until indicatorsContainer.childCount) {
            val imageView = indicatorsContainer.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.onboarding_indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.onboarding_indicator_inactive
                    )
                )
            }
        }
    }
}