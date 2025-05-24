package com.rajjathedev.expensetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rajjathedev.expensetracker.databinding.FragmentSettingsBinding
import com.rajjathedev.expensetracker.utils.PrefsHelper

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val currencies = arrayOf(
        "Rs",
        "₹",
        "$",
        "€",
        "£",
        "¥",
        "₣",
        "₩",
        "₽",
        "₴",
        "₸"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCurrencyDropdown()
    }

    private fun setupCurrencyDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies)
        binding.currencyDropdown.setAdapter(adapter)

        // Set current currency
        val currentCurrency = PrefsHelper.getCurrency(requireContext())
        binding.currencyDropdown.setText(currentCurrency, false)

        // Handle currency selection
        binding.currencyDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedCurrency = currencies[position]
            PrefsHelper.saveCurrency(requireContext(), selectedCurrency)
            Toast.makeText(context, "Currency changed to $selectedCurrency", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}