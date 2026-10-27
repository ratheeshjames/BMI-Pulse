package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.BmiPulseDao
import com.example.data.local.EncryptedStorageManager
import com.example.data.local.ReminderEntity
import com.example.data.local.SettingsDataStore
import com.example.data.local.UserProfileEntity
import com.example.data.local.WeightLogEntity
import com.example.domain.calculator.BmiCalculatorEngine
import com.example.domain.model.BiologicalSex
import com.example.integrations.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BmiPulseRepository(
    private val context: Context,
    private val dao: BmiPulseDao,
    val settingsDataStore: SettingsDataStore,
    val encryptedStorageManager: EncryptedStorageManager,
    private val reminderScheduler: ReminderScheduler
) {
    val profilesFlow: Flow<List<UserProfileEntity>> = dao.observeProfiles()
    val allLogsFlow: Flow<List<WeightLogEntity>> = dao.observeAllLogs()
    val remindersFlow: Flow<List<ReminderEntity>> = dao.observeReminders()

    fun observeLogsForProfile(profileId: Long): Flow<List<WeightLogEntity>> =
        dao.observeLogsForProfile(profileId)

    suspend fun ensureSeedDataPopulated() {
        val existingProfiles = dao.getProfilesOnce()
        if (existingProfiles.isNotEmpty()) return

        // Create 3 diverse profiles (Adult Female, Adult Athletic Male, Teen Child for LMS Percentile demo)
        val mayaId = dao.insertProfile(
            UserProfileEntity(
                name = "Maya Lin",
                avatarColorHex = "#0D9488",
                avatarEmoji = "🌿",
                ageYears = 29,
                sex = "FEMALE",
                heightCm = 166.0,
                wristCm = 15.5,
                activityLevel = "MODERATE",
                goalMode = "LOSE",
                targetWeightKg = 61.5,
                targetDays = 70,
                maintainToleranceKg = 1.0
            )
        )

        val alexId = dao.insertProfile(
            UserProfileEntity(
                name = "Alex Rivera",
                avatarColorHex = "#2563EB",
                avatarEmoji = "⚡",
                ageYears = 34,
                sex = "MALE",
                heightCm = 180.0,
                wristCm = 18.2,
                activityLevel = "ACTIVE",
                goalMode = "MAINTAIN",
                targetWeightKg = 79.0,
                targetDays = 60,
                maintainToleranceKg = 1.5
            )
        )

        val leoId = dao.insertProfile(
            UserProfileEntity(
                name = "Leo (Teen LMS)",
                avatarColorHex = "#D97706",
                avatarEmoji = "🚀",
                ageYears = 13,
                sex = "MALE",
                heightCm = 158.0,
                wristCm = 15.0,
                activityLevel = "ACTIVE",
                goalMode = "GAIN",
                targetWeightKg = 49.0,
                targetDays = 90,
                maintainToleranceKg = 1.2
            )
        )

        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        // 14 realistic historical entries for Maya showing steady progress, waist reduction & progress photos
        val mayaWeights = listOf(
            66.4 to 28,
            66.1 to 25,
            65.8 to 22,
            65.9 to 19,
            65.4 to 16,
            65.0 to 13,
            64.8 to 10,
            64.5 to 7,
            64.2 to 5,
            64.0 to 4,
            63.8 to 3,
            63.6 to 2,
            63.4 to 1,
            63.2 to 0
        )

        mayaWeights.forEachIndexed { idx, (weight, daysAgo) ->
            val waist = 77.5 - (idx * 0.25)
            val hip = 96.0 - (idx * 0.15)
            val neck = 33.0
            val bmi = BmiCalculatorEngine.calculateBmi(weight, 166.0)
            val bf = BmiCalculatorEngine.calculateUsNavyBodyFat(
                sex = BiologicalSex.FEMALE,
                heightCm = 166.0,
                waistCm = waist,
                neckCm = neck,
                hipCm = hip
            )
            dao.insertWeightLog(
                WeightLogEntity(
                    profileId = mayaId,
                    weightKg = weight,
                    heightCm = 166.0,
                    bmi = bmi,
                    bodyFatPct = bf,
                    neckCm = neck,
                    chestCm = 90.0 - (idx * 0.1),
                    waistCm = waist,
                    hipCm = hip,
                    armCm = 28.5,
                    thighCm = 54.0 - (idx * 0.1),
                    calfCm = 36.0,
                    note = when (idx) {
                        0 -> "Starting baseline weigh-in after morning hydration"
                        7 -> "Mid-month check-in; feeling stronger on 8k steps/day"
                        mayaWeights.lastIndex -> "Morning fasted check-in • Waist down 3.2 cm!"
                        else -> "Morning routine log"
                    },
                    tagsCsv = if (idx % 2 == 0) "Morning,Fasted" else "Morning,Workout",
                    photoLabel = when (idx) {
                        0 -> "Week 1 Baseline Posture"
                        7 -> "Week 3 Midpoint Check"
                        mayaWeights.lastIndex -> "Current Week Progress"
                        else -> null
                    },
                    timestampMillis = now - (daysAgo * dayMs)
                )
            )
        }

        // Seed logs for Alex
        listOf(80.4 to 14, 79.9 to 7, 79.5 to 2, 79.2 to 0).forEachIndexed { i, (w, daysAgo) ->
            val bmi = BmiCalculatorEngine.calculateBmi(w, 180.0)
            val bf = BmiCalculatorEngine.calculateUsNavyBodyFat(BiologicalSex.MALE, 180.0, 81.0 - i * 0.2, 39.5, 98.0)
            dao.insertWeightLog(
                WeightLogEntity(
                    profileId = alexId,
                    weightKg = w,
                    heightCm = 180.0,
                    bmi = bmi,
                    bodyFatPct = bf,
                    neckCm = 39.5,
                    chestCm = 104.0,
                    waistCm = 81.0 - i * 0.2,
                    hipCm = 98.0,
                    armCm = 36.5,
                    thighCm = 58.0,
                    calfCm = 39.0,
                    note = "Strength training block",
                    tagsCsv = "Gym,Morning",
                    timestampMillis = now - (daysAgo * dayMs)
                )
            )
        }

        // Seed logs for Leo (Teen)
        listOf(45.8 to 10, 46.4 to 0).forEach { (w, daysAgo) ->
            val bmi = BmiCalculatorEngine.calculateBmi(w, 158.0)
            dao.insertWeightLog(
                WeightLogEntity(
                    profileId = leoId,
                    weightKg = w,
                    heightCm = 158.0,
                    bmi = bmi,
                    bodyFatPct = 14.5,
                    neckCm = 31.0,
                    chestCm = 78.0,
                    waistCm = 66.0,
                    hipCm = 81.0,
                    armCm = 24.0,
                    thighCm = 45.0,
                    calfCm = 32.0,
                    note = "Pediatric growth check",
                    tagsCsv = "Growth,School",
                    timestampMillis = now - (daysAgo * dayMs)
                )
            )
        }

        // Seed Smart Reminders
        dao.insertReminder(
            ReminderEntity(
                title = "Morning Fasted Weigh-In",
                type = "DAILY_WEIGH_IN",
                hourOfDay = 7,
                minute = 30,
                daysOfWeekCsv = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                smartSkipIfLogged = true,
                respectDnd = true,
                isEnabled = true
            )
        )
        dao.insertReminder(
            ReminderEntity(
                title = "Sunday Tape Measurements",
                type = "WEEKLY_MEASUREMENT",
                hourOfDay = 9,
                minute = 0,
                daysOfWeekCsv = "Sun",
                smartSkipIfLogged = true,
                respectDnd = true,
                isEnabled = true
            )
        )
        dao.insertReminder(
            ReminderEntity(
                title = "Afternoon Hydration Check",
                type = "HYDRATION",
                hourOfDay = 14,
                minute = 0,
                daysOfWeekCsv = "Mon,Tue,Wed,Thu,Fri",
                smartSkipIfLogged = false,
                respectDnd = true,
                isEnabled = true
            )
        )
    }

    suspend fun saveProfile(profile: UserProfileEntity): Long = dao.insertProfile(profile)
    suspend fun updateProfile(profile: UserProfileEntity) = dao.updateProfile(profile)
    suspend fun deleteProfile(profile: UserProfileEntity) = dao.deleteProfile(profile)

    suspend fun addOrUpdateWeightLog(log: WeightLogEntity): Long = dao.insertWeightLog(log)
    suspend fun deleteWeightLog(id: Long) = dao.deleteWeightLogById(id)

    suspend fun saveReminder(reminder: ReminderEntity): Long {
        val id = dao.insertReminder(reminder)
        val saved = reminder.copy(id = id)
        if (saved.isEnabled) {
            reminderScheduler.scheduleReminder(saved)
        } else {
            reminderScheduler.cancelReminder(saved.id)
        }
        return id
    }

    suspend fun toggleReminder(reminder: ReminderEntity, enabled: Boolean) {
        val updated = reminder.copy(isEnabled = enabled)
        dao.updateReminder(updated)
        if (enabled) reminderScheduler.scheduleReminder(updated) else reminderScheduler.cancelReminder(updated.id)
    }

    suspend fun deleteReminder(id: Long) {
        reminderScheduler.cancelReminder(id)
        dao.deleteReminderById(id)
    }

    suspend fun deleteAllUserData() {
        dao.deleteAllLogs()
        dao.deleteAllProfiles()
        dao.deleteAllReminders()
        settingsDataStore.clearAllSettings()
        ensureSeedDataPopulated()
    }

    // --- Export & Backup: CSV, JSON, and PDF Clinical Summary Report ---

    suspend fun exportCsvFile(profile: UserProfileEntity): File {
        val logs = dao.getLogsForProfileOnce(profile.id)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "bmi_pulse_${profile.name.lowercase().replace(" ", "_")}.csv")
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

        val sb = StringBuilder()
        sb.appendLine("Date,Profile,Weight_kg,Height_cm,BMI,BodyFat_Pct,Waist_cm,Hip_cm,Neck_cm,Chest_cm,Arm_cm,Thigh_cm,Calf_cm,Tags,Notes")
        logs.forEach { log ->
            val dateStr = df.format(Date(log.timestampMillis))
            val safeNote = log.note.replace("\"", "'")
            sb.appendLine(
                "$dateStr,\"${profile.name}\",${log.weightKg},${log.heightCm},${String.format(Locale.US, "%.2f", log.bmi)},${String.format(Locale.US, "%.1f", log.bodyFatPct)},${log.waistCm},${log.hipCm},${log.neckCm},${log.chestCm},${log.armCm},${log.thighCm},${log.calfCm},\"${log.tagsCsv}\",\"$safeNote\""
            )
        }
        file.writeText(sb.toString())
        return file
    }

    suspend fun exportEncryptedJsonBackup(): File {
        val profiles = dao.getProfilesOnce()
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "bmi_pulse_backup.json")
        val sb = StringBuilder()
        sb.appendLine("{")
        sb.appendLine("  \"app\": \"BMI Pulse\",")
        sb.appendLine("  \"version\": 1,")
        sb.appendLine("  \"exportedAt\": ${System.currentTimeMillis()},")
        sb.appendLine("  \"encryption\": \"AES-256-GCM-Envelope\",")
        sb.appendLine("  \"profilesCount\": ${profiles.size},")
        val payloadBuilder = StringBuilder()
        profiles.forEach { p ->
            val logs = dao.getLogsForProfileOnce(p.id)
            payloadBuilder.append("PROFILE:${p.name}|${p.ageYears}|${p.sex}|${p.heightCm}|${p.targetWeightKg};LOGS:${logs.size}\n")
            logs.forEach { l ->
                payloadBuilder.append("LOG:${l.timestampMillis},${l.weightKg},${l.bmi},${l.bodyFatPct},${l.waistCm},${l.hipCm}\n")
            }
        }
        val cipherText = encryptedStorageManager.encryptPayload(payloadBuilder.toString())
        sb.appendLine("  \"encryptedPayload\": \"$cipherText\"")
        sb.appendLine("}")
        file.writeText(sb.toString())
        return file
    }

    suspend fun exportPdfReport(profile: UserProfileEntity): File {
        val logs = dao.getLogsForProfileOnce(profile.id)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "bmi_pulse_report_${profile.name.lowercase().replace(" ", "_")}.pdf")

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = AndroidColor.rgb(6, 78, 59)
            textSize = 20f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint().apply {
            color = AndroidColor.rgb(55, 65, 81)
            textSize = 12f
        }
        val bodyPaint = Paint().apply {
            color = AndroidColor.rgb(17, 24, 39)
            textSize = 11f
        }
        val linePaint = Paint().apply {
            color = AndroidColor.rgb(13, 148, 136)
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }

        canvas.drawText("BMI Pulse — Health & Body Composition Report", 40f, 50f, titlePaint)
        canvas.drawText("Profile: ${profile.name} (${profile.ageYears} yrs, ${profile.sex}, ${profile.heightCm} cm)", 40f, 74f, subtitlePaint)
        canvas.drawText("Target Weight: ${profile.targetWeightKg} kg • Generated ${SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())}", 40f, 92f, subtitlePaint)

        // Draw mini trend chart box
        canvas.drawRect(40f, 115f, 555f, 245f, Paint().apply {
            color = AndroidColor.rgb(240, 253, 244)
            style = Paint.Style.FILL
        })
        val chronological = logs.sortedBy { it.timestampMillis }
        if (chronological.size >= 2) {
            val minW = (chronological.minOf { it.weightKg } - 1.0)
            val maxW = (chronological.maxOf { it.weightKg } + 1.0)
            val span = (maxW - minW).coerceAtLeast(1.0)
            for (i in 0 until chronological.size - 1) {
                val x1 = 55f + (i.toFloat() / (chronological.size - 1)) * 485f
                val y1 = 230f - (((chronological[i].weightKg - minW) / span).toFloat() * 95f)
                val x2 = 55f + ((i + 1).toFloat() / (chronological.size - 1)) * 485f
                val y2 = 230f - (((chronological[i + 1].weightKg - minW) / span).toFloat() * 95f)
                canvas.drawLine(x1, y1, x2, y2, linePaint)
            }
        }

        var y = 275f
        canvas.drawText("Recent Weigh-In & Anthropometric History:", 40f, y, titlePaint.apply { textSize = 14f })
        y += 24f
        val df = SimpleDateFormat("MMM d, yyyy", Locale.US)
        logs.take(18).forEach { log ->
            val rowText = "${df.format(Date(log.timestampMillis))}  |  ${String.format(Locale.US, "%.1f", log.weightKg)} kg  |  BMI ${String.format(Locale.US, "%.1f", log.bmi)}  |  BF ${String.format(Locale.US, "%.1f", log.bodyFatPct)}%  |  Waist ${String.format(Locale.US, "%.1f", log.waistCm)} cm"
            canvas.drawText(rowText, 40f, y, bodyPaint)
            y += 20f
        }

        canvas.drawText("Medical Disclaimer: Informational wellness report only. Consult a healthcare professional.", 40f, 810f, subtitlePaint)
        pdfDocument.finishPage(page)
        FileOutputStream(file).use { pdfDocument.writeTo(it) }
        pdfDocument.close()
        return file
    }

    fun buildShareFileIntent(file: File, mimeType: String, subject: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
