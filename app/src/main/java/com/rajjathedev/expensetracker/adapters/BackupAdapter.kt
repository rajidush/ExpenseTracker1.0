package com.rajjathedev.expensetracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rajjathedev.expensetracker.databinding.ItemBackupBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupAdapter(
    private val onRestore: (String) -> Unit,
    private val onDelete: (String) -> Unit
) : ListAdapter<File, BackupAdapter.BackupViewHolder>(BackupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupViewHolder {
        val binding = ItemBackupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BackupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BackupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BackupViewHolder(
        private val binding: ItemBackupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(file: File) {
            binding.apply {
                tvFileName.text = file.name
                tvFileSize.text = "${file.length() / 1024} KB"
                tvFileDate.text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    .format(Date(file.lastModified()))

                btnRestore.setOnClickListener { onRestore(file.absolutePath) }
                btnDelete.setOnClickListener { onDelete(file.absolutePath) }
            }
        }
    }

    private class BackupDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.absolutePath == newItem.absolutePath
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.lastModified() == newItem.lastModified() &&
                    oldItem.length() == newItem.length()
        }
    }
} 