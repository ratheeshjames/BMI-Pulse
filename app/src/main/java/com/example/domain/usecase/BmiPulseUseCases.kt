package com.example.domain.usecase

import com.example.data.local.ReminderEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.WeightLogEntity
import com.example.data.repository.BmiPulseRepository
import com.example.di.Inject
import com.example.domain.calculator.BmiCalculatorEngine
import com.example.domain.model.ActivityLevel
import com.example.domain.model.BiologicalSex
import com.example.domain.model.BmiAnalysisResult
import com.example.domain.model.BmiStandard
import com.example.domain.model.BmrFormula
import com.example.domain.model.BodyCompositionResult
import com.example.domain.model.GoalMode
import com.example.domain.model.GoalTimelineProjection
import com.example.domain.model.MetabolismResult

/**
 * Clean Architecture Use Cases encapsulating domain calculations and repository operations.
 */
class EvaluateFullHealthProfileUseCase @Inject constructor() {

    data class FullEvaluation(
        val bmiAnalysis: BmiAnalysisResult,
        val bodyComposition: BodyCompositionResult,
        val metabolism: MetabolismResult,
        val goalProjection: GoalTimelineProjection
    )

    operator fun invoke(
        weightKg: Double,
        heightCm: Double,
        ageYears: Int,
        sex: BiologicalSex,
        standard: BmiStandard,
        isPregnantNoticeActive: Boolean,
        waistCm: Double,
        neckCm: Double,
        hipCm: Double,
        wristCm: Double,
        activityLevel: ActivityLevel,
        bmrFormula: BmrFormula,
        goalMode: GoalMode,
        proteinPct: Int,
        carbsPct: Int,
        fatPct: Int,
        startWeightKg: Double,
        targetWeightKg: Double,
        targetDays: Int,
        maintainToleranceKg: Double
    ): FullEvaluation {
        val bmiResult = BmiCalculatorEngine.analyzeBmi(
            weightKg = weightKg,
            heightCm = heightCm,
            ageYears = ageYears,
            sex = sex,
            standard = standard,
            isPregnantNoticeActive = isPregnantNoticeActive
        )

        val bodyComp = BmiCalculatorEngine.analyzeBodyComposition(
            weightKg = weightKg,
            heightCm = heightCm,
            ageYears = ageYears,
            sex = sex,
            waistCm = waistCm,
            neckCm = neckCm,
            hipCm = hipCm,
            wristCm = wristCm
        )

        val metabolism = BmiCalculatorEngine.analyzeMetabolism(
            weightKg = weightKg,
            heightCm = heightCm,
            ageYears = ageYears,
            sex = sex,
            activityLevel = activityLevel,
            bodyFatPct = bodyComp.primaryBodyFatPct,
            selectedBmrFormula = bmrFormula,
            goalMode = goalMode,
            proteinRatioPct = proteinPct,
            carbsRatioPct = carbsPct,
            fatRatioPct = fatPct
        )

        val goalProjection = BmiCalculatorEngine.calculateGoalProjection(
            startWeightKg = startWeightKg,
            currentWeightKg = weightKg,
            targetWeightKg = targetWeightKg,
            heightCm = heightCm,
            targetDaysFromNow = targetDays,
            maintainToleranceKg = maintainToleranceKg
        )

        return FullEvaluation(
            bmiAnalysis = bmiResult,
            bodyComposition = bodyComp,
            metabolism = metabolism,
            goalProjection = goalProjection
        )
    }
}

class LogHealthEntryUseCase @Inject constructor(
    private val repository: BmiPulseRepository
) {
    suspend operator fun invoke(log: WeightLogEntity): Long {
        return repository.addOrUpdateWeightLog(log)
    }
}

class ManageProfilesUseCase @Inject constructor(
    private val repository: BmiPulseRepository
) {
    suspend fun saveProfile(profile: UserProfileEntity): Long = repository.saveProfile(profile)
    suspend fun updateProfile(profile: UserProfileEntity) = repository.updateProfile(profile)
    suspend fun deleteProfile(profile: UserProfileEntity) = repository.deleteProfile(profile)
}

class ManageRemindersUseCase @Inject constructor(
    private val repository: BmiPulseRepository
) {
    suspend fun saveReminder(reminder: ReminderEntity): Long = repository.saveReminder(reminder)
    suspend fun toggleReminder(reminder: ReminderEntity, enabled: Boolean) =
        repository.toggleReminder(reminder, enabled)
    suspend fun deleteReminder(id: Long) = repository.deleteReminder(id)
}
