package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.di.AppContainer
import com.example.di.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class BmiPulseApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannels()
        applicationScope.launch {
            container.repository.ensureSeedDataPopulated()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java) ?: return
            val weighInChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Weigh-In & Measurement Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Smart reminders for daily weigh-ins, body measurements, and hydration with quick-log action."
            }
            val goalChannel = NotificationChannel(
                CHANNEL_MILESTONES,
                "Goal Milestones & Streak Celebrations",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you unlock BMI milestones, streaks, or safe-rate warnings."
            }
            manager.createNotificationChannels(listOf(weighInChannel, goalChannel))
        }
    }

    companion object {
        const val CHANNEL_REMINDERS = "bmi_pulse_reminders"
        const val CHANNEL_MILESTONES = "bmi_pulse_milestones"
    }
}
