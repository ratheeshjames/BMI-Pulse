package com.example.integrations

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.BmiPulseApplication
import com.example.MainActivity
import com.example.R
import com.example.data.local.ReminderEntity
import com.example.data.local.WeightLogEntity
import com.example.domain.calculator.BmiCalculatorEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ReminderScheduler(private val context: Context) {

    fun scheduleReminder(reminder: ReminderEntity) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_REMINDER_TRIGGER
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(EXTRA_REMINDER_TYPE, reminder.type)
            putExtra(EXTRA_SMART_SKIP, reminder.smartSkipIfLogged)
            putExtra(EXTRA_RESPECT_DND, reminder.respectDnd)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hourOfDay)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerCal.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerCal.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerCal.timeInMillis, pendingIntent)
        }
    }

    fun cancelReminder(reminderId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_REMINDER_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun triggerImmediateTestNotification(title: String = "Morning Fasted Weigh-In") {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_REMINDER_TRIGGER
            putExtra(EXTRA_REMINDER_ID, 999L)
            putExtra(EXTRA_REMINDER_TITLE, title)
            putExtra(EXTRA_REMINDER_TYPE, "DAILY_WEIGH_IN")
            putExtra(EXTRA_SMART_SKIP, false)
            putExtra(EXTRA_RESPECT_DND, false)
        }
        context.sendBroadcast(intent)
    }

    companion object {
        const val ACTION_REMINDER_TRIGGER = "com.example.ACTION_REMINDER_TRIGGER"
        const val ACTION_QUICK_LOG_REPLY = "com.example.ACTION_QUICK_LOG_REPLY"
        const val ACTION_SNOOZE_REMINDER = "com.example.ACTION_SNOOZE_REMINDER"
        const val KEY_QUICK_WEIGHT_INPUT = "key_quick_weight_input"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
        const val EXTRA_SMART_SKIP = "extra_smart_skip"
        const val EXTRA_RESPECT_DND = "extra_respect_dnd"
    }
}

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? BmiPulseApplication ?: return
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return

        when (intent.action) {
            ReminderScheduler.ACTION_REMINDER_TRIGGER -> {
                val reminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, 1L)
                val title = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_TITLE) ?: "BMI Pulse Check-In"
                val respectDnd = intent.getBooleanExtra(ReminderScheduler.EXTRA_RESPECT_DND, true)

                if (respectDnd &&
                    notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL &&
                    notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
                ) {
                    return
                }

                // RemoteInput action to log weight directly from the notification
                val remoteInput = RemoteInput.Builder(ReminderScheduler.KEY_QUICK_WEIGHT_INPUT)
                    .setLabel("Enter weight in kg (e.g. 63.0)")
                    .build()

                val replyIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                    action = ReminderScheduler.ACTION_QUICK_LOG_REPLY
                    putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
                }
                val replyPendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminderId.toInt() + 1000,
                    replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                val quickLogAction = NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_edit,
                    "Quick-Log Weight",
                    replyPendingIntent
                ).addRemoteInput(remoteInput).build()

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, BmiPulseApplication.CHANNEL_REMINDERS)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentText("Keep your logging streak alive! Tap Quick-Log to enter your weight directly.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(openPendingIntent)
                    .addAction(quickLogAction)
                    .setAutoCancel(true)
                    .build()

                try {
                    notificationManager.notify(reminderId.toInt(), notification)
                } catch (_: SecurityException) {
                    // Notification permission not granted yet
                }
            }

            ReminderScheduler.ACTION_QUICK_LOG_REPLY -> {
                val results = RemoteInput.getResultsFromIntent(intent)
                val weightText = results?.getCharSequence(ReminderScheduler.KEY_QUICK_WEIGHT_INPUT)?.toString()
                val parsedWeight = weightText?.toDoubleOrNull()
                val reminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, 1L)

                if (parsedWeight != null && parsedWeight in 15.0..400.0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val profiles = app.container.dao.getProfilesOnce()
                        val profile = profiles.firstOrNull()
                        if (profile != null) {
                            val bmi = BmiCalculatorEngine.calculateBmi(parsedWeight, profile.heightCm)
                            app.container.dao.insertWeightLog(
                                WeightLogEntity(
                                    profileId = profile.id,
                                    weightKg = parsedWeight,
                                    heightCm = profile.heightCm,
                                    bmi = bmi,
                                    bodyFatPct = 21.0,
                                    note = "Logged via Notification Quick-Reply",
                                    tagsCsv = "Notification,QuickLog"
                                )
                            )
                        }
                    }
                    val confirmed = NotificationCompat.Builder(context, BmiPulseApplication.CHANNEL_REMINDERS)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("Logged ${String.format(Locale.US, "%.1f", parsedWeight)} kg!")
                        .setContentText("Saved to your encrypted BMI Pulse history.")
                        .setAutoCancel(true)
                        .build()
                    try {
                        notificationManager.notify(reminderId.toInt(), confirmed)
                    } catch (_: SecurityException) {}
                }
            }
        }
    }
}

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as? BmiPulseApplication ?: return
            CoroutineScope(Dispatchers.IO).launch {
                val reminders = app.container.dao.getRemindersOnce()
                reminders.filter { it.isEnabled }.forEach { reminder ->
                    app.container.reminderScheduler.scheduleReminder(reminder)
                }
            }
        }
    }
}
