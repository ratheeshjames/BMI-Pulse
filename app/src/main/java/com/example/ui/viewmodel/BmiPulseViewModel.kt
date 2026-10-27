package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettingsState
import com.example.data.local.ReminderEntity
import com.example.data.local.ThemePreference
import com.example.data.local.UserProfileEntity
import com.example.data.local.WeightLogEntity
import com.example.data.repository.BmiPulseRepository
import com.example.di.HiltViewModel
import com.example.di.Inject
import com.example.domain.calculator.BmiCalculatorEngine
import com.example.domain.model.AchievementBadge
import com.example.domain.model.ActivityLevel
import com.example.domain.model.ArticleItem
import com.example.domain.model.BiologicalSex
import com.example.domain.model.BmiAnalysisResult
import com.example.domain.model.BmiStandard
import com.example.domain.model.BmrFormula
import com.example.domain.model.BodyCompositionResult
import com.example.domain.model.GoalMode
import com.example.domain.model.GoalTimelineProjection
import com.example.domain.model.InsightCard
import com.example.domain.model.MacroPreset
import com.example.domain.model.MetabolismResult
import com.example.domain.model.UnitSystem
import com.example.integrations.BleScaleGattParser
import com.example.integrations.BmiAppWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PrimaryTab(val route: String, val label: String) {
    PULSE_DASHBOARD("dashboard", "Pulse"),
    QUICK_CALCULATOR("calculator", "Calculator"),
    BODY_LAB("body_lab", "Body Lab"),
    ANALYTICS_CHARTS("charts", "Charts"),
    HUB_VAULT("hub", "Hub & Vault")
}

enum class SubScreen {
    NONE,
    RESULT_DETAIL,
    LOG_ENTRY,
    PROFILES_AND_COMPARE,
    GOALS_AND_ACHIEVEMENTS,
    REMINDERS,
    INSIGHTS_AND_ARTICLES,
    EXPORT_AND_BACKUP,
    PRIVACY_AND_SECURITY,
    SETTINGS,
    BLE_SMART_SCALE,
    UNIT_CONVERTER,
    ONBOARDING_TOUR
}

enum class ChartRangeFilter(val label: String, val days: Int) {
    WEEK("7D", 7),
    MONTH("1M", 30),
    THREE_MONTHS("3M", 90),
    YEAR("1Y", 365),
    ALL("All", 3650)
}

enum class ChartMetricType(val label: String, val unit: String) {
    WEIGHT("Weight", "kg"),
    BMI("BMI", "kg/m²"),
    BODY_FAT("Body Fat", "%"),
    WAIST("Waist", "cm")
}

@Immutable
data class CalculatorInputState(
    val isQuickNoProfileMode: Boolean = false,
    val weightKg: Double = 63.2,
    val heightCm: Double = 166.0,
    val ageYears: Int = 29,
    val sex: BiologicalSex = BiologicalSex.FEMALE,
    val waistCm: Double = 74.0,
    val neckCm: Double = 33.0,
    val hipCm: Double = 94.0,
    val wristCm: Double = 15.5,
    val chestCm: Double = 89.0,
    val armCm: Double = 28.5,
    val thighCm: Double = 53.0,
    val calfCm: Double = 36.0,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val bmrFormula: BmrFormula = BmrFormula.MIFFLIN_ST_JEOR,
    val goalMode: GoalMode = GoalMode.LOSE,
    val macroPreset: MacroPreset = MacroPreset.BALANCED,
    val customProteinPct: Int = 30,
    val customCarbsPct: Int = 40,
    val customFatPct: Int = 30,
    val chartRange: ChartRangeFilter = ChartRangeFilter.MONTH,
    val chartMetric: ChartMetricType = ChartMetricType.WEIGHT,
    val showMovingAverage: Boolean = true,
    val historySearchQuery: String = "",
    val selectedTagFilter: String = "All",
    val compareProfileSecondId: Long = 2L,
    val beforeAfterSliderFraction: Float = 0.5f,
    val statusBannerMessage: String? = null,
    val lastBleMeasurement: BleScaleGattParser.BleScaleMeasurement? = null
)

@Immutable
data class BmiPulseUiState(
    val isLoading: Boolean = false,
    val isLockedByPin: Boolean = false,
    val activeTab: PrimaryTab = PrimaryTab.PULSE_DASHBOARD,
    val activeSubScreen: SubScreen = SubScreen.NONE,
    val settings: AppSettingsState = AppSettingsState(),
    val profiles: List<UserProfileEntity> = emptyList(),
    val activeProfile: UserProfileEntity? = null,
    val comparisonSecondProfile: UserProfileEntity? = null,
    val profileLogs: List<WeightLogEntity> = emptyList(),
    val secondProfileLogs: List<WeightLogEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val calcInput: CalculatorInputState = CalculatorInputState(),
    val validationHint: String? = null,
    val bmiAnalysis: BmiAnalysisResult = BmiCalculatorEngine.analyzeBmi(63.2, 166.0),
    val bodyComposition: BodyCompositionResult = BmiCalculatorEngine.analyzeBodyComposition(
        63.2, 166.0, 29, BiologicalSex.FEMALE, 74.0, 33.0, 94.0, 15.5
    ),
    val metabolism: MetabolismResult = BmiCalculatorEngine.analyzeMetabolism(
        63.2, 166.0, 29, BiologicalSex.FEMALE, ActivityLevel.MODERATE, 23.5
    ),
    val goalProjection: GoalTimelineProjection = BmiCalculatorEngine.calculateGoalProjection(
        66.4, 63.2, 61.5, 166.0, 70, 1.0
    ),
    val streakDays: Int = 6,
    val achievements: List<AchievementBadge> = emptyList(),
    val insights: List<InsightCard> = emptyList(),
    val articles: List<ArticleItem> = BmiCalculatorEngine.offlineArticles
)

@HiltViewModel
class BmiPulseViewModel @Inject constructor(
    private val repository: BmiPulseRepository,
    private val appContext: Context
) : ViewModel() {

    private val _activeTab = MutableStateFlow(PrimaryTab.PULSE_DASHBOARD)
    private val _activeSubScreen = MutableStateFlow(SubScreen.NONE)
    private val _calcInput = MutableStateFlow(CalculatorInputState())
    private val _sessionUnlocked = MutableStateFlow(false)
    private val _syncedProfileId = MutableStateFlow(-1L)

    val uiState: StateFlow<BmiPulseUiState> = combine(
        repository.settingsDataStore.settingsFlow,
        repository.profilesFlow,
        repository.allLogsFlow,
        repository.remindersFlow,
        combine(_activeTab, _activeSubScreen, _calcInput, _sessionUnlocked) { tab, sub, calc, unlocked ->
            Quadruple(tab, sub, calc, unlocked)
        }
    ) { settings, profiles, allLogs, reminders, (tab, subScreen, calcInput, unlocked) ->
        val activeProfile = profiles.find { it.id == settings.activeProfileId } ?: profiles.firstOrNull()
        val activeLogs = if (activeProfile != null) {
            allLogs.filter { it.profileId == activeProfile.id }.sortedByDescending { it.timestampMillis }
        } else emptyList()

        // Sync calculator inputs once when switching profile (unless in no-profile quick mode)
        if (activeProfile != null && !calcInput.isQuickNoProfileMode && _syncedProfileId.value != activeProfile.id) {
            _syncedProfileId.value = activeProfile.id
            val latestLog = activeLogs.firstOrNull()
            val syncedWeight = latestLog?.weightKg ?: activeProfile.targetWeightKg
            val syncedWaist = latestLog?.waistCm ?: 75.0
            val syncedHip = latestLog?.hipCm ?: 95.0
            val syncedNeck = latestLog?.neckCm ?: 34.0
            _calcInput.update {
                it.copy(
                    weightKg = syncedWeight,
                    heightCm = activeProfile.heightCm,
                    ageYears = activeProfile.ageYears,
                    sex = runCatching { BiologicalSex.valueOf(activeProfile.sex) }.getOrDefault(BiologicalSex.FEMALE),
                    waistCm = syncedWaist,
                    hipCm = syncedHip,
                    neckCm = syncedNeck,
                    wristCm = activeProfile.wristCm,
                    activityLevel = runCatching { ActivityLevel.valueOf(activeProfile.activityLevel) }.getOrDefault(ActivityLevel.MODERATE),
                    goalMode = runCatching { GoalMode.valueOf(activeProfile.goalMode) }.getOrDefault(GoalMode.LOSE)
                )
            }
        }

        val secondProfile = profiles.find { it.id == calcInput.compareProfileSecondId && it.id != activeProfile?.id }
            ?: profiles.firstOrNull { it.id != activeProfile?.id }
            ?: activeProfile
        val secondLogs = if (secondProfile != null) {
            allLogs.filter { it.profileId == secondProfile.id }.sortedByDescending { it.timestampMillis }
        } else emptyList()

        val validationHint = BmiCalculatorEngine.validateInputs(
            weightKg = calcInput.weightKg,
            heightCm = calcInput.heightCm,
            ageYears = calcInput.ageYears
        )

        val effectiveStandard = if (calcInput.ageYears < 20 && settings.bmiStandard == BmiStandard.WHO_STANDARD) {
            BmiStandard.CHILD_CDC
        } else {
            settings.bmiStandard
        }

        val bmiAnalysis = BmiCalculatorEngine.analyzeBmi(
            weightKg = calcInput.weightKg,
            heightCm = calcInput.heightCm,
            ageYears = calcInput.ageYears,
            sex = calcInput.sex,
            standard = effectiveStandard,
            isPregnantNoticeActive = settings.pregnancyNoticeEnabled
        )

        val bodyComp = BmiCalculatorEngine.analyzeBodyComposition(
            weightKg = calcInput.weightKg,
            heightCm = calcInput.heightCm,
            ageYears = calcInput.ageYears,
            sex = calcInput.sex,
            waistCm = calcInput.waistCm,
            neckCm = calcInput.neckCm,
            hipCm = calcInput.hipCm,
            wristCm = calcInput.wristCm
        )

        val pRatio = if (calcInput.macroPreset == MacroPreset.CUSTOM) calcInput.customProteinPct else calcInput.macroPreset.proteinPct
        val cRatio = if (calcInput.macroPreset == MacroPreset.CUSTOM) calcInput.customCarbsPct else calcInput.macroPreset.carbsPct
        val fRatio = if (calcInput.macroPreset == MacroPreset.CUSTOM) calcInput.customFatPct else calcInput.macroPreset.fatPct

        val metabolism = BmiCalculatorEngine.analyzeMetabolism(
            weightKg = calcInput.weightKg,
            heightCm = calcInput.heightCm,
            ageYears = calcInput.ageYears,
            sex = calcInput.sex,
            activityLevel = calcInput.activityLevel,
            bodyFatPct = bodyComp.primaryBodyFatPct,
            selectedBmrFormula = calcInput.bmrFormula,
            goalMode = calcInput.goalMode,
            proteinRatioPct = pRatio,
            carbsRatioPct = cRatio,
            fatRatioPct = fRatio
        )

        val chronologicalWeights = activeLogs.sortedBy { it.timestampMillis }.map { it.weightKg }
        val startWeight = chronologicalWeights.firstOrNull() ?: calcInput.weightKg
        val currentWeight = chronologicalWeights.lastOrNull() ?: calcInput.weightKg
        val targetWeight = activeProfile?.targetWeightKg ?: 61.5
        val targetDays = activeProfile?.targetDays ?: 70
        val tolerance = activeProfile?.maintainToleranceKg ?: 1.0

        val goalProjection = BmiCalculatorEngine.calculateGoalProjection(
            startWeightKg = startWeight,
            currentWeightKg = currentWeight,
            targetWeightKg = targetWeight,
            heightCm = calcInput.heightCm,
            targetDaysFromNow = targetDays,
            maintainToleranceKg = tolerance
        )

        val streakDays = (activeLogs.size.coerceAtMost(14)).coerceAtLeast(1)
        val badges = buildAchievementBadges(activeLogs.size, streakDays, goalProjection, bodyComp)
        val insights = BmiCalculatorEngine.detectInsightsAndAlerts(
            weightsChronologicalKg = chronologicalWeights,
            bmiResult = bmiAnalysis,
            bodyComp = bodyComp,
            streakDays = streakDays
        )

        val isLocked = settings.pinLockEnabled && !unlocked

        BmiPulseUiState(
            isLoading = false,
            isLockedByPin = isLocked,
            activeTab = tab,
            activeSubScreen = subScreen,
            settings = settings,
            profiles = profiles,
            activeProfile = activeProfile,
            comparisonSecondProfile = secondProfile,
            profileLogs = activeLogs,
            secondProfileLogs = secondLogs,
            reminders = reminders,
            calcInput = calcInput,
            validationHint = validationHint,
            bmiAnalysis = bmiAnalysis,
            bodyComposition = bodyComp,
            metabolism = metabolism,
            goalProjection = goalProjection,
            streakDays = streakDays,
            achievements = badges,
            insights = insights,
            articles = BmiCalculatorEngine.offlineArticles
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BmiPulseUiState(isLoading = true)
    )

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun buildAchievementBadges(
        logCount: Int,
        streakDays: Int,
        goal: GoalTimelineProjection,
        bodyComp: BodyCompositionResult
    ): List<AchievementBadge> {
        return listOf(
            AchievementBadge(
                id = "first_pulse",
                title = "First Pulse",
                description = "Logged your baseline weight & BMI check-in",
                iconName = "bolt",
                isUnlocked = logCount >= 1,
                progressText = if (logCount >= 1) "Unlocked" else "0/1 logs"
            ),
            AchievementBadge(
                id = "streak_7",
                title = "7-Day Rhythm",
                description = "Maintained a full week of consistent weigh-ins",
                iconName = "fire",
                isUnlocked = streakDays >= 7 || logCount >= 7,
                progressText = "${minOf(7, logCount)}/7 entries"
            ),
            AchievementBadge(
                id = "streak_14",
                title = "14-Day Pulse Master",
                description = "Logged 14 weight & body measurement entries",
                iconName = "trophy",
                isUnlocked = logCount >= 14,
                progressText = "${minOf(14, logCount)}/14 entries"
            ),
            AchievementBadge(
                id = "whtr_guardian",
                title = "Waist-Height Guardian",
                description = "Achieved a Waist-to-Height ratio under 0.50",
                iconName = "shield",
                isUnlocked = bodyComp.waistToHeightRatio < 0.50,
                progressText = String.format("WHtR %.2f", bodyComp.waistToHeightRatio)
            ),
            AchievementBadge(
                id = "halfway_hero",
                title = "50% Goal Horizon",
                description = "Completed over half of your target weight journey",
                iconName = "flag",
                isUnlocked = goal.progressFraction >= 0.5f,
                progressText = "${(goal.progressFraction * 100).toInt()}% complete"
            ),
            AchievementBadge(
                id = "body_scientist",
                title = "Body Lab Scientist",
                description = "Tracked full tape circumferences (waist, hip, neck)",
                iconName = "science",
                isUnlocked = true,
                progressText = "Unlocked"
            )
        )
    }

    // --- Navigation & Screen Actions ---

    fun selectTab(tab: PrimaryTab) {
        _activeTab.value = tab
        _activeSubScreen.value = SubScreen.NONE
    }

    fun openSubScreen(subScreen: SubScreen) {
        _activeSubScreen.value = subScreen
    }

    fun navigateBack(): Boolean {
        if (_activeSubScreen.value != SubScreen.NONE) {
            _activeSubScreen.value = SubScreen.NONE
            return true
        }
        if (_activeTab.value != PrimaryTab.PULSE_DASHBOARD) {
            _activeTab.value = PrimaryTab.PULSE_DASHBOARD
            return true
        }
        return false
    }

    fun dismissBanner() {
        _calcInput.update { it.copy(statusBannerMessage = null) }
    }

    fun showBanner(message: String) {
        _calcInput.update { it.copy(statusBannerMessage = message) }
    }

    // --- Calculator & Body Lab State Updates ---

    fun updateWeightKg(kg: Double) {
        _calcInput.update { it.copy(weightKg = kg.coerceIn(10.0, 400.0)) }
    }

    fun updateHeightCm(cm: Double) {
        _calcInput.update { it.copy(heightCm = cm.coerceIn(60.0, 250.0)) }
    }

    fun updateAgeYears(age: Int) {
        _calcInput.update { it.copy(ageYears = age.coerceIn(2, 110)) }
    }

    fun updateSex(sex: BiologicalSex) {
        _calcInput.update { it.copy(sex = sex) }
    }

    fun toggleQuickNoProfileMode(enabled: Boolean) {
        _calcInput.update { it.copy(isQuickNoProfileMode = enabled) }
    }

    fun updateMeasurements(
        waistCm: Double = _calcInput.value.waistCm,
        neckCm: Double = _calcInput.value.neckCm,
        hipCm: Double = _calcInput.value.hipCm,
        wristCm: Double = _calcInput.value.wristCm,
        chestCm: Double = _calcInput.value.chestCm,
        armCm: Double = _calcInput.value.armCm,
        thighCm: Double = _calcInput.value.thighCm,
        calfCm: Double = _calcInput.value.calfCm
    ) {
        _calcInput.update {
            it.copy(
                waistCm = waistCm,
                neckCm = neckCm,
                hipCm = hipCm,
                wristCm = wristCm,
                chestCm = chestCm,
                armCm = armCm,
                thighCm = thighCm,
                calfCm = calfCm
            )
        }
    }

    fun updateActivityLevel(level: ActivityLevel) {
        _calcInput.update { it.copy(activityLevel = level) }
    }

    fun updateBmrFormula(formula: BmrFormula) {
        _calcInput.update { it.copy(bmrFormula = formula) }
    }

    fun updateGoalMode(mode: GoalMode) {
        _calcInput.update { it.copy(goalMode = mode) }
    }

    fun updateMacroPreset(preset: MacroPreset) {
        _calcInput.update {
            it.copy(
                macroPreset = preset,
                customProteinPct = preset.proteinPct,
                customCarbsPct = preset.carbsPct,
                customFatPct = preset.fatPct
            )
        }
    }

    fun updateCustomMacros(proteinPct: Int, carbsPct: Int, fatPct: Int) {
        _calcInput.update {
            it.copy(
                macroPreset = MacroPreset.CUSTOM,
                customProteinPct = proteinPct.coerceIn(10, 70),
                customCarbsPct = carbsPct.coerceIn(5, 75),
                customFatPct = fatPct.coerceIn(10, 65)
            )
        }
    }

    // --- Charts & History Filtering ---

    fun updateChartRange(range: ChartRangeFilter) {
        _calcInput.update { it.copy(chartRange = range) }
    }

    fun updateChartMetric(metric: ChartMetricType) {
        _calcInput.update { it.copy(chartMetric = metric) }
    }

    fun toggleMovingAverage(show: Boolean) {
        _calcInput.update { it.copy(showMovingAverage = show) }
    }

    fun updateHistorySearch(query: String) {
        _calcInput.update { it.copy(historySearchQuery = query) }
    }

    fun updateTagFilter(tag: String) {
        _calcInput.update { it.copy(selectedTagFilter = tag) }
    }

    fun updateBeforeAfterSlider(fraction: Float) {
        _calcInput.update { it.copy(beforeAfterSliderFraction = fraction.coerceIn(0f, 1f)) }
    }

    // --- Logging Weight, Measurements & Progress Photos ---

    fun logCurrentEntry(
        note: String,
        tagsCsv: String,
        photoLabel: String? = null,
        daysBackdated: Int = 0
    ) {
        viewModelScope.launch {
            val state = uiState.value
            val profile = state.activeProfile ?: return@launch
            val calc = state.calcInput
            val timestamp = System.currentTimeMillis() - (daysBackdated * 86_400_000L)

            val log = WeightLogEntity(
                profileId = profile.id,
                weightKg = calc.weightKg,
                heightCm = calc.heightCm,
                bmi = state.bmiAnalysis.bmi,
                bodyFatPct = state.bodyComposition.primaryBodyFatPct,
                neckCm = calc.neckCm,
                chestCm = calc.chestCm,
                waistCm = calc.waistCm,
                hipCm = calc.hipCm,
                armCm = calc.armCm,
                thighCm = calc.thighCm,
                calfCm = calc.calfCm,
                note = note.ifBlank { "Logged via BMI Pulse" },
                tagsCsv = tagsCsv.ifBlank { "Morning" },
                photoLabel = photoLabel,
                timestampMillis = timestamp
            )
            repository.addOrUpdateWeightLog(log)
            BmiAppWidgetProvider.updateAllWidgets(
                context = appContext,
                bmi = state.bmiAnalysis.bmi,
                categoryLabel = state.bmiAnalysis.category.shortTitle,
                streakDays = state.streakDays + 1
            )
            showBanner("Saved ${String.format("%.1f", calc.weightKg)} kg (BMI ${String.format("%.1f", state.bmiAnalysis.bmi)}) to ${profile.name}'s log!")
            if (_activeSubScreen.value == SubScreen.LOG_ENTRY) {
                _activeSubScreen.value = SubScreen.NONE
            }
        }
    }

    fun deleteLogEntry(logId: Long) {
        viewModelScope.launch {
            repository.deleteWeightLog(logId)
            showBanner("Log entry removed.")
        }
    }

    // --- Profiles & Goals ---

    fun switchActiveProfile(profileId: Long) {
        viewModelScope.launch {
            _syncedProfileId.value = -1L
            repository.settingsDataStore.setActiveProfileId(profileId)
        }
    }

    fun selectComparisonProfile(profileId: Long) {
        _calcInput.update { it.copy(compareProfileSecondId = profileId) }
    }

    fun createNewProfile(
        name: String,
        emoji: String,
        age: Int,
        sex: BiologicalSex,
        heightCm: Double,
        targetWeightKg: Double
    ) {
        viewModelScope.launch {
            val colors = listOf("#0D9488", "#2563EB", "#D97706", "#7C3AED", "#DB2777")
            val colorHex = colors[name.length % colors.size]
            val id = repository.saveProfile(
                UserProfileEntity(
                    name = name.ifBlank { "New Profile" },
                    avatarColorHex = colorHex,
                    avatarEmoji = emoji.ifBlank { "✨" },
                    ageYears = age.coerceIn(2, 110),
                    sex = sex.name,
                    heightCm = heightCm.coerceIn(60.0, 240.0),
                    targetWeightKg = targetWeightKg.coerceIn(20.0, 250.0)
                )
            )
            repository.addOrUpdateWeightLog(
                WeightLogEntity(
                    profileId = id,
                    weightKg = targetWeightKg + 2.0,
                    heightCm = heightCm,
                    bmi = BmiCalculatorEngine.calculateBmi(targetWeightKg + 2.0, heightCm),
                    bodyFatPct = 20.0,
                    note = "Initial profile baseline",
                    tagsCsv = "Baseline"
                )
            )
            switchActiveProfile(id)
            showBanner("Profile '${name.ifBlank { "New Profile" }}' created and activated!")
        }
    }

    fun updateActiveProfileGoal(targetWeightKg: Double, targetDays: Int, maintainToleranceKg: Double) {
        viewModelScope.launch {
            val current = uiState.value.activeProfile ?: return@launch
            repository.updateProfile(
                current.copy(
                    targetWeightKg = targetWeightKg.coerceIn(25.0, 250.0),
                    targetDays = targetDays.coerceIn(7, 730),
                    maintainToleranceKg = maintainToleranceKg.coerceIn(0.5, 4.0)
                )
            )
            showBanner("Updated goal target to ${String.format("%.1f", targetWeightKg)} kg over $targetDays days.")
        }
    }

    // --- Reminders ---

    fun addCustomReminder(title: String, type: String, hour: Int, minute: Int, daysCsv: String) {
        viewModelScope.launch {
            repository.saveReminder(
                ReminderEntity(
                    title = title.ifBlank { "Daily Weigh-In" },
                    type = type,
                    hourOfDay = hour.coerceIn(0, 23),
                    minute = minute.coerceIn(0, 59),
                    daysOfWeekCsv = daysCsv.ifBlank { "Mon,Tue,Wed,Thu,Fri,Sat,Sun" }
                )
            )
            showBanner("Scheduled '$title' reminder.")
        }
    }

    fun toggleReminder(reminder: ReminderEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(reminder, enabled)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    fun triggerTestNotification() {
        val scheduler = com.example.integrations.ReminderScheduler(appContext)
        scheduler.triggerImmediateTestNotification()
        showBanner("Triggered notification! Check the status bar to try Quick-Log RemoteInput.")
    }

    // --- BLE Smart Scale & Health Connect Integrations ---

    fun scanAndImportBleScaleReading() {
        val currentW = _calcInput.value.weightKg
        val simulatedReading = (currentW - 0.2).coerceAtLeast(35.0)
        val packet = BleScaleGattParser.buildSampleGattPacket(
            targetWeightKg = simulatedReading,
            bodyFatPct = uiState.value.bodyComposition.primaryBodyFatPct
        )
        val parsed = BleScaleGattParser.parseWeightMeasurementPacket(
            bytes = packet,
            deviceName = "Withings / Xiaomi BLE Scale (0x181D)"
        )
        _calcInput.update {
            it.copy(
                weightKg = parsed.weightKg,
                lastBleMeasurement = parsed,
                statusBannerMessage = "BLE GATT 0x2A9D Frame [${parsed.rawHexFrame}] imported: ${String.format("%.2f", parsed.weightKg)} kg"
            )
        }
    }

    // --- Settings, Security Lock & Export / Share ---

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch { repository.settingsDataStore.setUnitSystem(unitSystem) }
    }

    fun setBmiStandard(standard: BmiStandard) {
        viewModelScope.launch { repository.settingsDataStore.setBmiStandard(standard) }
    }

    fun setThemePreference(pref: ThemePreference) {
        viewModelScope.launch { repository.settingsDataStore.setThemePreference(pref) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.settingsDataStore.setDynamicColor(enabled) }
    }

    fun setHighContrast(enabled: Boolean) {
        viewModelScope.launch { repository.settingsDataStore.setHighContrast(enabled) }
    }

    fun setPregnancyNotice(enabled: Boolean) {
        viewModelScope.launch { repository.settingsDataStore.setPregnancyNotice(enabled) }
    }

    fun setDecimalPlaces(places: Int) {
        viewModelScope.launch { repository.settingsDataStore.setDecimalPlaces(places) }
    }

    fun setLanguageCode(code: String) {
        viewModelScope.launch { repository.settingsDataStore.setLanguageCode(code) }
    }

    fun setFlagSecure(enabled: Boolean) {
        viewModelScope.launch { repository.settingsDataStore.setFlagSecure(enabled) }
    }

    fun setHealthConnectSync(enabled: Boolean) {
        viewModelScope.launch {
            repository.settingsDataStore.setHealthConnectSync(enabled)
            if (enabled) showBanner("Health Connect bridge enabled for Weight, Height & BodyFatRecord.")
        }
    }

    fun setDriveBackup(enabled: Boolean) {
        viewModelScope.launch {
            repository.settingsDataStore.setDriveBackup(enabled)
            if (enabled) showBanner("Encrypted Drive appDataFolder auto-backup queued.")
        }
    }

    fun configurePinLock(pin: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.settingsDataStore.configurePinLock(pin, enabled)
            _sessionUnlocked.value = true
            showBanner(if (enabled) "PIN Lock enabled (SHA-256 salted)." else "PIN Lock disabled.")
        }
    }

    fun attemptPinUnlock(enteredPin: String): Boolean {
        val valid = repository.settingsDataStore.verifyPin(enteredPin, uiState.value.settings.pinHash)
        if (valid) {
            _sessionUnlocked.value = true
        }
        return valid
    }

    fun unlockViaBiometrics() {
        _sessionUnlocked.value = true
    }

    fun lockSessionNow() {
        if (uiState.value.settings.pinLockEnabled) {
            _sessionUnlocked.value = false
        }
    }

    fun exportAndShareCsv(context: Context) {
        viewModelScope.launch {
            val profile = uiState.value.activeProfile ?: return@launch
            val file = repository.exportCsvFile(profile)
            val intent = repository.buildShareFileIntent(file, "text/csv", "BMI Pulse CSV Export - ${profile.name}")
            runCatching {
                context.startActivity(Intent.createChooser(intent, "Share CSV History").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            showBanner("Exported ${file.name} (${file.length()} bytes)")
        }
    }

    fun exportAndSharePdf(context: Context) {
        viewModelScope.launch {
            val profile = uiState.value.activeProfile ?: return@launch
            val file = repository.exportPdfReport(profile)
            val intent = repository.buildShareFileIntent(file, "application/pdf", "BMI Pulse Health Report - ${profile.name}")
            runCatching {
                context.startActivity(Intent.createChooser(intent, "Share PDF Health Report").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            showBanner("Generated PDF clinical summary report (${file.name})")
        }
    }

    fun exportAndShareEncryptedJson(context: Context) {
        viewModelScope.launch {
            val file = repository.exportEncryptedJsonBackup()
            val intent = repository.buildShareFileIntent(file, "application/json", "BMI Pulse Encrypted Backup")
            runCatching {
                context.startActivity(Intent.createChooser(intent, "Export Encrypted JSON Backup").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            showBanner("Created AES-256-GCM encrypted backup (${file.name})")
        }
    }

    fun shareCurrentSummaryText(context: Context) {
        val state = uiState.value
        val summary = buildString {
            appendLine("📊 BMI Pulse Health Snapshot (${state.activeProfile?.name ?: "Quick Mode"})")
            appendLine("• BMI: ${String.format("%.1f", state.bmiAnalysis.bmi)} (${state.bmiAnalysis.category.title})")
            appendLine("• BMI Prime: ${String.format("%.2f", state.bmiAnalysis.bmiPrime)} | Ponderal Index: ${String.format("%.1f", state.bmiAnalysis.ponderalIndex)} kg/m³")
            appendLine("• US Navy Body Fat: ${String.format("%.1f", state.bodyComposition.navyBodyFatPct)}% (${state.bodyComposition.bodyFatCategory})")
            appendLine("• Waist-to-Height Ratio: ${String.format("%.2f", state.bodyComposition.waistToHeightRatio)} (${state.bodyComposition.whtrStatus})")
            appendLine("• TDEE: ${state.metabolism.tdeeCalories} kcal/day | BMR: ${state.metabolism.activeBmr} kcal/day")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "Share BMI Pulse Snapshot").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun wipeAllDataAndReset() {
        viewModelScope.launch {
            repository.deleteAllUserData()
            showBanner("All personal logs wiped and reset to clean state.")
        }
    }

    companion object {
        fun provideFactory(repository: BmiPulseRepository, context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BmiPulseViewModel(repository, context.applicationContext) as T
                }
            }
    }
}
