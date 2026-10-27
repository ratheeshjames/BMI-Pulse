package com.example.data.local

import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.domain.model.BmiStandard
import com.example.domain.model.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemePreference(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Expressive Light"),
    DARK("Expressive Dark")
}

@Immutable
data class AppSettingsState(
    val activeProfileId: Long = 1L,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val dynamicColorEnabled: Boolean = false,
    val highContrastEnabled: Boolean = false,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val bmiStandard: BmiStandard = BmiStandard.WHO_STANDARD,
    val pregnancyNoticeEnabled: Boolean = false,
    val decimalPlaces: Int = 1,
    val dateFormatPattern: String = "MMM d, yyyy",
    val firstDayOfWeek: String = "Monday",
    val hapticsEnabled: Boolean = true,
    val appLanguageCode: String = "en",
    val pinLockEnabled: Boolean = false,
    val pinHash: String = "",
    val biometricUnlockEnabled: Boolean = false,
    val flagSecureHideInRecents: Boolean = false,
    val onboardingCompleted: Boolean = true,
    val healthConnectSyncEnabled: Boolean = false,
    val driveBackupEnabled: Boolean = false
)

class SettingsDataStore(
    private val dataStore: DataStore<Preferences>,
    private val encryptedStorageManager: EncryptedStorageManager
) {
    private object Keys {
        val ACTIVE_PROFILE_ID = longPreferencesKey("active_profile_id")
        val THEME_PREF = stringPreferencesKey("theme_pref")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val BMI_STANDARD = stringPreferencesKey("bmi_standard")
        val PREGNANCY_NOTICE = booleanPreferencesKey("pregnancy_notice")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
        val HAPTICS = booleanPreferencesKey("haptics")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val PIN_LOCK_ENABLED = booleanPreferencesKey("pin_lock_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val FLAG_SECURE = booleanPreferencesKey("flag_secure")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val HEALTH_CONNECT = booleanPreferencesKey("health_connect")
        val DRIVE_BACKUP = booleanPreferencesKey("drive_backup")
    }

    val settingsFlow: Flow<AppSettingsState> = dataStore.data.map { prefs ->
        AppSettingsState(
            activeProfileId = prefs[Keys.ACTIVE_PROFILE_ID] ?: 1L,
            themePreference = prefs[Keys.THEME_PREF]?.let {
                runCatching { ThemePreference.valueOf(it) }.getOrNull()
            } ?: ThemePreference.SYSTEM,
            dynamicColorEnabled = prefs[Keys.DYNAMIC_COLOR] ?: false,
            highContrastEnabled = prefs[Keys.HIGH_CONTRAST] ?: false,
            unitSystem = prefs[Keys.UNIT_SYSTEM]?.let {
                runCatching { UnitSystem.valueOf(it) }.getOrNull()
            } ?: UnitSystem.METRIC,
            bmiStandard = prefs[Keys.BMI_STANDARD]?.let {
                runCatching { BmiStandard.valueOf(it) }.getOrNull()
            } ?: BmiStandard.WHO_STANDARD,
            pregnancyNoticeEnabled = prefs[Keys.PREGNANCY_NOTICE] ?: false,
            decimalPlaces = prefs[Keys.DECIMAL_PLACES] ?: 1,
            dateFormatPattern = prefs[Keys.DATE_FORMAT] ?: "MMM d, yyyy",
            firstDayOfWeek = prefs[Keys.FIRST_DAY_OF_WEEK] ?: "Monday",
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            appLanguageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            pinLockEnabled = prefs[Keys.PIN_LOCK_ENABLED] ?: false,
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            biometricUnlockEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            flagSecureHideInRecents = prefs[Keys.FLAG_SECURE] ?: false,
            onboardingCompleted = prefs[Keys.ONBOARDING_DONE] ?: true,
            healthConnectSyncEnabled = prefs[Keys.HEALTH_CONNECT] ?: false,
            driveBackupEnabled = prefs[Keys.DRIVE_BACKUP] ?: false
        )
    }

    suspend fun setActiveProfileId(id: Long) {
        dataStore.edit { it[Keys.ACTIVE_PROFILE_ID] = id }
    }

    suspend fun setThemePreference(pref: ThemePreference) {
        dataStore.edit { it[Keys.THEME_PREF] = pref.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        dataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }

    suspend fun setUnitSystem(unitSystem: UnitSystem) {
        dataStore.edit { it[Keys.UNIT_SYSTEM] = unitSystem.name }
    }

    suspend fun setBmiStandard(standard: BmiStandard) {
        dataStore.edit { it[Keys.BMI_STANDARD] = standard.name }
    }

    suspend fun setPregnancyNotice(enabled: Boolean) {
        dataStore.edit { it[Keys.PREGNANCY_NOTICE] = enabled }
    }

    suspend fun setDecimalPlaces(places: Int) {
        dataStore.edit { it[Keys.DECIMAL_PLACES] = places.coerceIn(1, 2) }
    }

    suspend fun setDateFormat(pattern: String) {
        dataStore.edit { it[Keys.DATE_FORMAT] = pattern }
    }

    suspend fun setFirstDayOfWeek(day: String) {
        dataStore.edit { it[Keys.FIRST_DAY_OF_WEEK] = day }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTICS] = enabled }
    }

    suspend fun setLanguageCode(code: String) {
        dataStore.edit { it[Keys.LANGUAGE_CODE] = code }
    }

    suspend fun configurePinLock(pin: String?, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.PIN_LOCK_ENABLED] = enabled
            if (pin != null && pin.length >= 4) {
                prefs[Keys.PIN_HASH] = encryptedStorageManager.hashPin(pin)
            }
        }
    }

    fun verifyPin(enteredPin: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return true
        return encryptedStorageManager.hashPin(enteredPin) == storedHash
    }

    suspend fun setBiometricUnlock(enabled: Boolean) {
        dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setFlagSecure(enabled: Boolean) {
        dataStore.edit { it[Keys.FLAG_SECURE] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_DONE] = completed }
    }

    suspend fun setHealthConnectSync(enabled: Boolean) {
        dataStore.edit { it[Keys.HEALTH_CONNECT] = enabled }
    }

    suspend fun setDriveBackup(enabled: Boolean) {
        dataStore.edit { it[Keys.DRIVE_BACKUP] = enabled }
    }

    suspend fun clearAllSettings() {
        dataStore.edit { it.clear() }
    }
}
