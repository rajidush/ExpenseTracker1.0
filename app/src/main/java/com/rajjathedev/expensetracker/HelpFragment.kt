package com.rajjathedev.expensetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajjathedev.expensetracker.adapters.BackupAdapter
import com.rajjathedev.expensetracker.databinding.FragmentHelpBinding
import com.rajjathedev.expensetracker.utils.BackupManager
import com.rajjathedev.expensetracker.utils.PrefsHelper

class HelpFragment : Fragment() {
    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!
    private lateinit var backupManager: BackupManager
    private lateinit var backupAdapter: BackupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        backupManager = BackupManager(requireContext())
        setupBackupAdapter()
        setupClickListeners()
        loadBackups()
    }

    private fun setupBackupAdapter() {
        backupAdapter = BackupAdapter(
            onRestore = { filePath ->
                showRestoreConfirmationDialog(filePath)
            },
            onDelete = { filePath ->
                showDeleteConfirmationDialog(filePath)
            }
        )
        binding.rvBackups.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = backupAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnExportJson.setOnClickListener {
            exportData(ExportFormat.JSON)
        }

        binding.btnExportText.setOnClickListener {
            exportData(ExportFormat.TEXT)
        }
    }

    private fun exportData(format: ExportFormat) {
        try {
            val transactions = PrefsHelper.loadTransactions(requireContext())
            val filePath = when (format) {
                ExportFormat.JSON -> backupManager.exportToJson(transactions)
                ExportFormat.TEXT -> backupManager.exportToText(transactions)
            }
            Toast.makeText(context, "Backup created: $filePath", Toast.LENGTH_LONG).show()
            loadBackups()
        } catch (e: Exception) {
            Toast.makeText(context, "Error creating backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRestoreConfirmationDialog(filePath: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Restore Backup")
            .setMessage("Are you sure you want to restore this backup? This will replace all current transactions.")
            .setPositiveButton("Restore") { _, _ ->
                restoreBackup(filePath)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(filePath: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Backup")
            .setMessage("Are you sure you want to delete this backup?")
            .setPositiveButton("Delete") { _, _ ->
                deleteBackup(filePath)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun restoreBackup(filePath: String) {
        try {
            val transactions = if (filePath.endsWith(".json")) {
                backupManager.importFromJson(filePath)
            } else {
                backupManager.importFromText(filePath)
            }
            PrefsHelper.saveTransactions(requireContext(), transactions)
            Toast.makeText(context, "Backup restored successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error restoring backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteBackup(filePath: String) {
        try {
            backupManager.deleteBackup(filePath)
            Toast.makeText(context, "Backup deleted successfully", Toast.LENGTH_SHORT).show()
            loadBackups()
        } catch (e: Exception) {
            Toast.makeText(context, "Error deleting backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBackups() {
        val backupFiles = backupManager.getBackupFiles()
        backupAdapter.submitList(backupFiles)
        binding.tvNoBackups.visibility = if (backupFiles.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class ExportFormat {
        JSON, TEXT
    }
}