package com.example

import com.example.domain.calculator.BmiCalculatorEngine
import com.example.domain.model.ActivityLevel
import com.example.domain.model.BiologicalSex
import com.example.domain.model.BmiCategory
import com.example.domain.model.BmiStandard
import com.example.domain.model.BmrFormula
import com.example.domain.model.FrameSize
import com.example.domain.model.GoalMode
import com.example.integrations.BleScaleGattParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `bmi calculation and ponderal index known values`() {
        // 70 kg at 175 cm (1.75 m) => BMI = 70 / 3.0625 = 22.8571
        val bmi = BmiCalculatorEngine.calculateBmi(70.0, 175.0)
        assertEquals(22.857, bmi, 0.01)

        // Ponderal index = 70 / (1.75^3) = 70 / 5.359375 = 13.061
        val ponderal = BmiCalculatorEngine.calculatePonderalIndex(70.0, 175.0)
        assertEquals(13.061, ponderal, 0.01)
    }

    @Test
    fun `who vs asian pacific cutoffs classify bmi 23_8 differently`() {
        val whoResult = BmiCalculatorEngine.analyzeBmi(
            weightKg = 68.0,
            heightCm = 169.0, // BMI ~ 23.81
            standard = BmiStandard.WHO_STANDARD
        )
        val asianResult = BmiCalculatorEngine.analyzeBmi(
            weightKg = 68.0,
            heightCm = 169.0,
            standard = BmiStandard.ASIAN_PACIFIC
        )
        assertEquals(BmiCategory.NORMAL, whoResult.category)
        assertEquals(BmiCategory.OVERWEIGHT, asianResult.category)
        assertEquals(23.81 / 25.0, whoResult.bmiPrime, 0.01)
        assertEquals(23.81 / 23.0, asianResult.bmiPrime, 0.01)
    }

    @Test
    fun `child teen lms percentile calculation returns valid zscore and percentile`() {
        val childRes = BmiCalculatorEngine.calculateChildPercentile(
            bmi = 17.8,
            ageMonths = 144, // 12 years old
            sex = BiologicalSex.MALE,
            useWhoTable = false
        )
        // At 144m CDC boys median M is ~17.808, so Z-score should be near 0 and percentile near 50%
        assertEquals(0.0, childRes.zScore, 0.05)
        assertEquals(50.0, childRes.percentile, 1.5)
    }

    @Test
    fun `unit conversions metric imperial and mixed roundtrip accurately`() {
        val lb = BmiCalculatorEngine.kgToLb(70.0)
        assertEquals(154.323, lb, 0.02)
        assertEquals(70.0, BmiCalculatorEngine.lbToKg(lb), 0.01)

        val (st, remLb) = BmiCalculatorEngine.kgToStoneAndPounds(70.0)
        assertEquals(11, st)
        assertEquals(70.0, BmiCalculatorEngine.stoneAndPoundsToKg(st, remLb), 0.01)

        val (ft, inches) = BmiCalculatorEngine.cmToFeetAndInches(180.0)
        assertEquals(5, ft)
        assertEquals(180.0, BmiCalculatorEngine.feetAndInchesToCm(ft, inches), 0.01)
    }

    @Test
    fun `us navy body fat ideal weight and waist ratios match clinical formulas`() {
        val comp = BmiCalculatorEngine.analyzeBodyComposition(
            weightKg = 80.0,
            heightCm = 180.0,
            ageYears = 32,
            sex = BiologicalSex.MALE,
            waistCm = 84.0,
            neckCm = 39.0,
            hipCm = 98.0,
            wristCm = 17.5
        )
        assertTrue(comp.navyBodyFatPct in 14.0..19.0)
        assertEquals(84.0 / 180.0, comp.waistToHeightRatio, 0.001)
        assertEquals(84.0 / 98.0, comp.waistToHipRatio, 0.001)
        assertEquals(FrameSize.MEDIUM, comp.frameSize)
        assertTrue(comp.idealWeightDevineKg in 70.0..78.0)
    }

    @Test
    fun `bmr mifflin harris katch and tdee macro splits calculate accurately`() {
        val met = BmiCalculatorEngine.analyzeMetabolism(
            weightKg = 70.0,
            heightCm = 175.0,
            ageYears = 30,
            sex = BiologicalSex.MALE,
            activityLevel = ActivityLevel.MODERATE,
            bodyFatPct = 15.0,
            selectedBmrFormula = BmrFormula.MIFFLIN_ST_JEOR,
            goalMode = GoalMode.MAINTAIN,
            proteinRatioPct = 30,
            carbsRatioPct = 40,
            fatRatioPct = 30
        )
        // Mifflin Male: 10*70 + 6.25*175 - 5*30 + 5 = 700 + 1093.75 - 150 + 5 = 1649
        assertEquals(1649, met.bmrMifflin)
        assertEquals(1649 * 1.55, met.tdeeCalories.toDouble(), 2.0)
        assertTrue(met.macroBreakdown.dailyWaterMl >= 2450)
    }

    @Test
    fun `goal timeline safe rate warning triggers when weekly rate exceeds 1kg`() {
        val unsafeGoal = BmiCalculatorEngine.calculateGoalProjection(
            startWeightKg = 85.0,
            currentWeightKg = 85.0,
            targetWeightKg = 70.0, // 15 kg in 28 days (4 weeks = 3.75 kg/wk)
            heightCm = 175.0,
            targetDaysFromNow = 28
        )
        assertTrue(unsafeGoal.isUnsafeRate)
        assertNotNull(unsafeGoal.safetyWarningMessage)

        val safeGoal = BmiCalculatorEngine.calculateGoalProjection(
            startWeightKg = 75.0,
            currentWeightKg = 73.0,
            targetWeightKg = 70.0,
            heightCm = 175.0,
            targetDaysFromNow = 70
        )
        assertNull(safeGoal.safetyWarningMessage)
    }

    @Test
    fun `ble smart scale gatt 0x2a9d packet parser decodes weight and body fat`() {
        val packet = BleScaleGattParser.buildSampleGattPacket(targetWeightKg = 64.25, bodyFatPct = 21.6)
        val parsed = BleScaleGattParser.parseWeightMeasurementPacket(packet, "Test Scale")
        assertEquals(64.25, parsed.weightKg, 0.01)
        assertEquals(21.6, parsed.bodyFatPct ?: 0.0, 0.1)
    }
}
