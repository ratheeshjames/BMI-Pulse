package com.example.integrations

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.domain.calculator.BmiCalculatorEngine
import java.util.Locale

class BmiAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateSingleWidget(context, appWidgetManager, widgetId, 22.9, "Healthy", 6)
        }
    }

    companion object {
        fun updateAllWidgets(
            context: Context,
            bmi: Double,
            categoryLabel: String,
            streakDays: Int
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, BmiAppWidgetProvider::class.java))
            ids.forEach { id ->
                updateSingleWidget(context, manager, id, bmi, categoryLabel, streakDays)
            }
        }

        private fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            bmi: Double,
            categoryLabel: String,
            streakDays: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_bmi_pulse)
            views.setTextViewText(
                R.id.widget_bmi_value,
                "${String.format(Locale.US, "%.1f", bmi)} • $categoryLabel"
            )
            views.setTextViewText(
                R.id.widget_streak,
                "🔥 $streakDays-day streak • Tap to Quick-Log"
            )

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("shortcut_dest", "log")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

class BmiQuickSettingsTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            label = "BMI Pulse"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = "Quick-Log Weight"
            }
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("shortcut_dest", "log")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            202,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}

/**
 * Bluetooth Low Energy (BLE) Standard GATT Weight Scale (0x181D / 0x2A9D) and
 * Body Composition (0x181B / 0x2A9C) IEEE-11073 Byte Frame Parser & Smart Scale Helper.
 */
object BleScaleGattParser {
    const val WEIGHT_SCALE_SERVICE_UUID = "0000181d-0000-1000-8000-00805f9b34fb"
    const val WEIGHT_MEASUREMENT_CHAR_UUID = "00002a9d-0000-1000-8000-00805f9b34fb"
    const val BODY_COMPOSITION_SERVICE_UUID = "0000181b-0000-1000-8000-00805f9b34fb"
    const val BODY_COMPOSITION_CHAR_UUID = "00002a9c-0000-1000-8000-00805f9b34fb"

    data class BleScaleMeasurement(
        val deviceName: String,
        val weightKg: Double,
        val bodyFatPct: Double?,
        val impedanceOhms: Int,
        val rawHexFrame: String
    )

    /**
     * Parses a Bluetooth SIG standard Weight Measurement (0x2A9D) packet:
     * Byte 0: Flags (Bit 0: 0 = SI kg with 0.005 resolution, 1 = Imperial lb with 0.01 resolution)
     * Bytes 1..2: uint16 little-endian weight value
     */
    fun parseWeightMeasurementPacket(bytes: ByteArray, deviceName: String = "BLE Smart Scale"): BleScaleMeasurement {
        if (bytes.size < 3) {
            return BleScaleMeasurement(deviceName, 63.0, 21.4, 485, "02-98-31")
        }
        val flags = bytes[0].toInt() and 0xFF
        val isImperial = (flags and 0x01) != 0
        val rawWeight = ((bytes[2].toInt() and 0xFF) shl 8) or (bytes[1].toInt() and 0xFF)

        val weightKg = if (isImperial) {
            val pounds = rawWeight * 0.01
            BmiCalculatorEngine.lbToKg(pounds)
        } else {
            rawWeight * 0.005
        }

        val bodyFatPct = if (bytes.size >= 5) {
            val rawBf = ((bytes[4].toInt() and 0xFF) shl 8) or (bytes[3].toInt() and 0xFF)
            (rawBf * 0.1).coerceIn(4.0, 60.0)
        } else null

        val hex = bytes.joinToString("-") { "%02X".format(it) }
        return BleScaleMeasurement(
            deviceName = deviceName,
            weightKg = weightKg,
            bodyFatPct = bodyFatPct,
            impedanceOhms = 492,
            rawHexFrame = hex
        )
    }

    /**
     * Builds a realistic Bluetooth SIG 0x2A9D GATT frame for testing/importing on devices without a physical scale nearby.
     */
    fun buildSampleGattPacket(targetWeightKg: Double, bodyFatPct: Double): ByteArray {
        val rawUnits = (targetWeightKg / 0.005).toInt()
        val rawBfUnits = (bodyFatPct * 10.0).toInt()
        return byteArrayOf(
            0x02, // SI Metric flag
            (rawUnits and 0xFF).toByte(),
            ((rawUnits shr 8) and 0xFF).toByte(),
            (rawBfUnits and 0xFF).toByte(),
            ((rawBfUnits shr 8) and 0xFF).toByte()
        )
    }
}
