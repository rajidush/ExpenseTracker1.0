package com.rajjathedev.expensetracker.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rajjathedev.expensetracker.models.Transaction
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(private val context: Context) {
    private val gson = Gson()
    private val backupDir = File(context.filesDir, "backups")

    init {
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
    }

    fun exportToJson(transactions: List<Transaction>): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFile = File(backupDir, "expense_backup_$timestamp.json")
        
        val json = gson.toJson(transactions)
        FileOutputStream(backupFile).use { it.write(json.toByteArray()) }
        
        return backupFile.absolutePath
    }

    fun exportToText(transactions: List<Transaction>): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFile = File(backupDir, "expense_backup_$timestamp.txt")
        
        val text = transactions.joinToString("\n") { transaction ->
            "${transaction.date}|${transaction.amount}|${transaction.category}|${transaction.label}"
        }
        
        FileOutputStream(backupFile).use { it.write(text.toByteArray()) }
        
        return backupFile.absolutePath
    }

    fun importFromJson(filePath: String): List<Transaction> {
        val file = File(filePath)
        if (!file.exists()) throw Exception("Backup file not found")
        
        val json = FileInputStream(file).bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type)
    }

    fun importFromText(filePath: String): List<Transaction> {
        val file = File(filePath)
        if (!file.exists()) throw Exception("Backup file not found")
        
        return FileInputStream(file).bufferedReader().useLines { lines ->
            lines.map { line ->
                val parts = line.split("|")
                Transaction(
                    date = parts[0],
                    amount = parts[1].toDouble(),
                    category = parts[2],
                    label = parts[3]
                )
            }.toList()
        }
    }

    fun getBackupFiles(): List<File> {
        return backupDir.listFiles()?.filter { 
            it.name.startsWith("expense_backup_") && 
            (it.name.endsWith(".json") || it.name.endsWith(".txt"))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun deleteBackup(filePath: String) {
        File(filePath).delete()
    }
} 