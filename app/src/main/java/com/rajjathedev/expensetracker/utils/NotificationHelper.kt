package com.rajjathedev.expensetracker.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rajjathedev.expensetracker.MainActivity
import com.rajjathedev.expensetracker.R

class NotificationHelper(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "budget_notifications"
        private const val CHANNEL_NAME = "Budget Notifications"
        private const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for budget alerts"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showBudgetExceededNotification(currentBudget: Double, totalExpenses: Double) {
        if (!hasNotificationPermission()) return

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Budget Exceeded!")
                .setContentText("You have exceeded your monthly budget of ${
                    PrefsHelper.getCurrency(
                        context
                    )
                } $currentBudget. Current expenses: ${PrefsHelper.getCurrency(context)} $totalExpenses")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            with(NotificationManagerCompat.from(context)) {
                if (hasNotificationPermission()) {
                    notify(NOTIFICATION_ID, notification)
                }
            }
        } catch (e: SecurityException) {
            // Handle SecurityException silently
        } catch (e: Exception) {
            // Handle other exceptions silently
        }
    }

    fun showBudgetWarningNotification(currentBudget: Double, totalExpenses: Double) {
        if (!hasNotificationPermission()) return

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Budget Warning")
                .setContentText("You are close to exceeding your monthly budget of ${
                    PrefsHelper.getCurrency(
                        context
                    )
                } $currentBudget. Current expenses: ${PrefsHelper.getCurrency(context)} $totalExpenses")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            with(NotificationManagerCompat.from(context)) {
                if (hasNotificationPermission()) {
                    notify(NOTIFICATION_ID, notification)
                }
            }
        } catch (e: SecurityException) {
            // Handle SecurityException silently
        } catch (e: Exception) {
            // Handle other exceptions silently
        }
    }
} 