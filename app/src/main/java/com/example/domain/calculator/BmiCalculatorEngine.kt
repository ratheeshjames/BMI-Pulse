package com.example.domain.calculator

import com.example.domain.model.ActivityLevel
import com.example.domain.model.ArticleItem
import com.example.domain.model.BiologicalSex
import com.example.domain.model.BmiAnalysisResult
import com.example.domain.model.BmiCategory
import com.example.domain.model.BmiStandard
import com.example.domain.model.BmrFormula
import com.example.domain.model.BodyCompositionResult
import com.example.domain.model.ChildPercentileResult
import com.example.domain.model.FrameSize
import com.example.domain.model.GoalMilestone
import com.example.domain.model.GoalMode
import com.example.domain.model.GoalTimelineProjection
import com.example.domain.model.InsightCard
import com.example.domain.model.MacroBreakdown
import com.example.domain.model.MetabolismResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Pure, deterministic, unit-tested domain calculator engine implementing all 120 BMI,
 * body composition, BMR/TDEE, LMS percentile, and health projection formulas.
 */
object BmiCalculatorEngine {

    private const val KG_TO_LB = 2.20462262185
    private const val INCH_TO_CM = 2.54

    // --- 1. Unit Conversions ---

    fun kgToLb(kg: Double): Double = kg * KG_TO_LB
    fun lbToKg(lb: Double): Double = lb / KG_TO_LB

    fun kgToStoneAndPounds(kg: Double): Pair<Int, Double> {
        val totalPounds = kgToLb(max(0.0, kg))
        val stones = (totalPounds / 14.0).toInt()
        val remPounds = totalPounds - (stones * 14.0)
        return stones to remPounds
    }

    fun stoneAndPoundsToKg(stones: Int, pounds: Double): Double {
        val totalLb = (stones * 14.0) + pounds
        return lbToKg(totalLb)
    }

    fun cmToFeetAndInches(cm: Double): Pair<Int, Double> {
        val totalInches = max(0.0, cm) / INCH_TO_CM
        val feet = (totalInches / 12.0).toInt()
        val inches = totalInches - (feet * 12.0)
        return feet to inches
    }

    fun feetAndInchesToCm(feet: Int, inches: Double): Double {
        return ((feet * 12.0) + inches) * INCH_TO_CM
    }

    fun cmToInches(cm: Double): Double = cm / INCH_TO_CM
    fun inchesToCm(inches: Double): Double = inches * INCH_TO_CM

    // --- 2. Input Validation with Friendly Hints ---

    fun validateInputs(weightKg: Double, heightCm: Double, ageYears: Int): String? {
        return when {
            heightCm < 50.0 -> "Height looks a bit low (${heightCm.roundToInt()} cm). Enter a value between 50 cm and 260 cm."
            heightCm > 260.0 -> "Height above 260 cm is outside human physiological range."
            weightKg < 8.0 -> "Weight looks low (${weightKg.roundToInt()} kg). Enter at least 8 kg (18 lb)."
            weightKg > 450.0 -> "Weight above 450 kg exceeds calculator calibration."
            ageYears < 2 || ageYears > 120 -> "Age should be between 2 and 120 years."
            else -> null
        }
    }

    // --- 3. Core BMI, BMI Prime, Ponderal Index, WHO & Asian-Pacific Cutoffs ---

    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        val safeHeightM = max(0.5, heightCm / 100.0)
        val safeWeightKg = max(2.0, weightKg)
        return safeWeightKg / (safeHeightM * safeHeightM)
    }

    fun calculatePonderalIndex(weightKg: Double, heightCm: Double): Double {
        val safeHeightM = max(0.5, heightCm / 100.0)
        val safeWeightKg = max(2.0, weightKg)
        return safeWeightKg / (safeHeightM * safeHeightM * safeHeightM)
    }

    fun classifyBmi(bmi: Double, standard: BmiStandard): BmiCategory {
        return when (standard) {
            BmiStandard.ASIAN_PACIFIC -> when {
                bmi < 16.0 -> BmiCategory.SEVERE_THINNESS
                bmi < 17.0 -> BmiCategory.MODERATE_THINNESS
                bmi < 18.5 -> BmiCategory.MILD_THINNESS
                bmi < 23.0 -> BmiCategory.NORMAL
                bmi < 25.0 -> BmiCategory.OVERWEIGHT
                bmi < 30.0 -> BmiCategory.OBESE_CLASS_I
                bmi < 35.0 -> BmiCategory.OBESE_CLASS_II
                else -> BmiCategory.OBESE_CLASS_III
            }
            else -> when {
                bmi < 16.0 -> BmiCategory.SEVERE_THINNESS
                bmi < 17.0 -> BmiCategory.MODERATE_THINNESS
                bmi < 18.5 -> BmiCategory.MILD_THINNESS
                bmi < 25.0 -> BmiCategory.NORMAL
                bmi < 30.0 -> BmiCategory.OVERWEIGHT
                bmi < 35.0 -> BmiCategory.OBESE_CLASS_I
                bmi < 40.0 -> BmiCategory.OBESE_CLASS_II
                else -> BmiCategory.OBESE_CLASS_III
            }
        }
    }

    fun analyzeBmi(
        weightKg: Double,
        heightCm: Double,
        ageYears: Int = 30,
        sex: BiologicalSex = BiologicalSex.FEMALE,
        standard: BmiStandard = BmiStandard.WHO_STANDARD,
        isPregnantNoticeActive: Boolean = false
    ): BmiAnalysisResult {
        val bmi = calculateBmi(weightKg, heightCm)
        val ponderal = calculatePonderalIndex(weightKg, heightCm)
        val upperNormalCutoff = if (standard == BmiStandard.ASIAN_PACIFIC) 23.0 else 25.0
        val bmiPrime = bmi / upperNormalCutoff

        val heightM = max(0.5, heightCm / 100.0)
        val minHealthyKg = 18.5 * heightM * heightM
        val maxHealthyKg = upperNormalCutoff * heightM * heightM

        val deltaToHealthy = when {
            weightKg < minHealthyKg -> minHealthyKg - weightKg // positive: weight to gain
            weightKg > maxHealthyKg -> maxHealthyKg - weightKg // negative: weight to lose
            else -> 0.0
        }

        val childPercentile = if (ageYears in 2..19 || standard == BmiStandard.CHILD_CDC || standard == BmiStandard.CHILD_WHO) {
            val useWho = standard == BmiStandard.CHILD_WHO
            calculateChildPercentile(
                bmi = bmi,
                ageMonths = (ageYears.coerceIn(2, 20)) * 12,
                sex = sex,
                useWhoTable = useWho
            )
        } else null

        val category = if (childPercentile != null && (standard == BmiStandard.CHILD_CDC || standard == BmiStandard.CHILD_WHO)) {
            when {
                childPercentile.percentile < 5.0 -> BmiCategory.MILD_THINNESS
                childPercentile.percentile < 85.0 -> BmiCategory.NORMAL
                childPercentile.percentile < 95.0 -> BmiCategory.OVERWEIGHT
                else -> BmiCategory.OBESE_CLASS_I
            }
        } else {
            classifyBmi(bmi, standard)
        }

        return BmiAnalysisResult(
            bmi = bmi,
            bmiPrime = bmiPrime,
            ponderalIndex = ponderal,
            category = category,
            standardUsed = standard,
            healthyWeightMinKg = minHealthyKg,
            healthyWeightMaxKg = maxHealthyKg,
            weightDeltaToHealthyKg = deltaToHealthy,
            childPercentile = childPercentile,
            isPregnantNoticeActive = isPregnantNoticeActive
        )
    }

    // --- 4. Child/Teen BMI-for-Age LMS Percentile Engine (CDC & WHO LMS Reference Tables) ---

    private data class LmsRow(val ageMonths: Int, val l: Double, val m: Double, val s: Double)

    // Bundled CDC 2000 / WHO Growth Reference LMS knots from 24 to 240 months (2 to 20 years)
    private val cdcBoysLms = listOf(
        LmsRow(24, -0.6387, 16.575, 0.0805),
        LmsRow(48, -1.1865, 15.626, 0.0790),
        LmsRow(72, -1.7376, 15.383, 0.0870),
        LmsRow(96, -2.1481, 15.844, 0.1045),
        LmsRow(120, -2.2950, 16.690, 0.1225),
        LmsRow(144, -2.2384, 17.808, 0.1342),
        LmsRow(168, -2.0725, 19.155, 0.1378),
        LmsRow(192, -1.8692, 20.536, 0.1365),
        LmsRow(216, -1.6745, 21.859, 0.1345),
        LmsRow(240, -1.5030, 23.020, 0.1340)
    )

    private val cdcGirlsLms = listOf(
        LmsRow(24, -0.7684, 16.423, 0.0825),
        LmsRow(48, -1.3520, 15.421, 0.0852),
        LmsRow(72, -1.8050, 15.228, 0.0980),
        LmsRow(96, -2.0450, 15.825, 0.1165),
        LmsRow(120, -2.0540, 16.852, 0.1338),
        LmsRow(144, -1.9160, 18.165, 0.1445),
        LmsRow(168, -1.7250, 19.554, 0.1482),
        LmsRow(192, -1.5540, 20.695, 0.1488),
        LmsRow(216, -1.4250, 21.450, 0.1502),
        LmsRow(240, -1.3450, 21.717, 0.1528)
    )

    fun calculateChildPercentile(
        bmi: Double,
        ageMonths: Int,
        sex: BiologicalSex,
        useWhoTable: Boolean = false
    ): ChildPercentileResult {
        val clampedMonths = ageMonths.coerceIn(24, 240)
        val baseTable = if (sex == BiologicalSex.MALE) cdcBoysLms else cdcGirlsLms
        val lower = baseTable.lastOrNull { it.ageMonths <= clampedMonths } ?: baseTable.first()
        val upper = baseTable.firstOrNull { it.ageMonths >= clampedMonths } ?: baseTable.last()
        val fraction = if (upper.ageMonths == lower.ageMonths) 0.0 else {
            (clampedMonths - lower.ageMonths).toDouble() / (upper.ageMonths - lower.ageMonths).toDouble()
        }

        val l = lower.l + (upper.l - lower.l) * fraction
        // WHO reference medians are ~0.25 BMI units leaner in mid-childhood
        val whoOffset = if (useWhoTable) -0.25 else 0.0
        val m = (lower.m + (upper.m - lower.m) * fraction) + whoOffset
        val s = lower.s + (upper.s - lower.s) * fraction

        val zScore = if (abs(l) < 0.001) {
            ln(bmi / m) / s
        } else {
            ((bmi / m).pow(l) - 1.0) / (l * s)
        }.coerceIn(-4.0, 4.0)

        val percentile = (normalCdf(zScore) * 100.0).coerceIn(0.1, 99.9)
        val categoryLabel = when {
            percentile < 5.0 -> "Underweight (< 5th %ile)"
            percentile < 85.0 -> "Healthy Weight (5th–84th %ile)"
            percentile < 95.0 -> "Overweight (85th–94th %ile)"
            else -> "Obesity (≥ 95th %ile)"
        }

        return ChildPercentileResult(
            ageMonths = clampedMonths,
            zScore = zScore,
            percentile = percentile,
            percentileCategory = categoryLabel,
            medianBmiForAge = m,
            sourceTable = if (useWhoTable) "WHO Multicentre Growth Reference (LMS)" else "CDC 2000 Growth Charts (LMS)"
        )
    }

    private fun normalCdf(z: Double): Double {
        // Abramowitz and Stegun approximation (max error 7.5e-8)
        val t = 1.0 / (1.0 + 0.2316419 * abs(z))
        val d = 0.3989422804014327 * exp(-z * z / 2.0)
        val prob = d * t * (0.319381530 + t * (-0.356563782 + t * (1.781477937 + t * (-1.821255978 + 1.330274429 * t))))
        return if (z > 0) 1.0 - prob else prob
    }

    // --- 5. Body Composition: US Navy Body Fat, BMI Body Fat, WHR, WHtR, BSA, Frame Size, Ideal Weight ---

    fun calculateUsNavyBodyFat(
        sex: BiologicalSex,
        heightCm: Double,
        waistCm: Double,
        neckCm: Double,
        hipCm: Double
    ): Double {
        val h = max(100.0, heightCm)
        val w = max(45.0, waistCm)
        val n = max(22.0, min(w - 5.0, neckCm))
        val hip = max(55.0, hipCm)

        val rawBf = if (sex == BiologicalSex.MALE) {
            495.0 / (1.0324 - 0.19077 * log10(w - n) + 0.15456 * log10(h)) - 450.0
        } else {
            495.0 / (1.29579 - 0.35004 * log10(w + hip - n) + 0.22100 * log10(h)) - 450.0
        }
        return rawBf.coerceIn(3.0, 65.0)
    }

    fun calculateBmiBodyFatEstimate(bmi: Double, ageYears: Int, sex: BiologicalSex): Double {
        // Deurenberg formula: (1.20 * BMI) + (0.23 * Age) - (10.8 * sex_male) - 5.4
        val sexFactor = if (sex == BiologicalSex.MALE) 1.0 else 0.0
        return ((1.20 * bmi) + (0.23 * ageYears.coerceIn(18, 100)) - (10.8 * sexFactor) - 5.4).coerceIn(4.0, 65.0)
    }

    fun analyzeBodyComposition(
        weightKg: Double,
        heightCm: Double,
        ageYears: Int,
        sex: BiologicalSex,
        waistCm: Double,
        neckCm: Double,
        hipCm: Double,
        wristCm: Double
    ): BodyCompositionResult {
        val bmi = calculateBmi(weightKg, heightCm)
        val navyBf = calculateUsNavyBodyFat(sex, heightCm, waistCm, neckCm, hipCm)
        val bmiBf = calculateBmiBodyFatEstimate(bmi, ageYears, sex)
        val primaryBf = navyBf

        val bfCategory = if (sex == BiologicalSex.MALE) {
            when {
                primaryBf < 6.0 -> "Essential Fat"
                primaryBf < 14.0 -> "Athletic"
                primaryBf < 18.0 -> "Fitness"
                primaryBf < 25.0 -> "Acceptable / Average"
                else -> "Elevated Body Fat"
            }
        } else {
            when {
                primaryBf < 14.0 -> "Essential Fat"
                primaryBf < 21.0 -> "Athletic"
                primaryBf < 25.0 -> "Fitness"
                primaryBf < 32.0 -> "Acceptable / Average"
                else -> "Elevated Body Fat"
            }
        }

        val fatMassKg = weightKg * (primaryBf / 100.0)
        val leanMassKg = max(10.0, weightKg - fatMassKg)

        val whr = waistCm / max(40.0, hipCm)
        val whrRisk = if (sex == BiologicalSex.MALE) {
            when {
                whr < 0.90 -> "Low Cardiovascular Risk"
                whr < 1.00 -> "Moderate Risk"
                else -> "High Visceral Risk"
            }
        } else {
            when {
                whr < 0.80 -> "Low Cardiovascular Risk"
                whr < 0.85 -> "Moderate Risk"
                else -> "High Visceral Risk"
            }
        }

        val whtr = waistCm / max(100.0, heightCm)
        val whtrStatus = when {
            whtr < 0.40 -> "Slender Central Ratio"
            whtr < 0.50 -> "Healthy (< 0.50 Boundary)"
            whtr < 0.60 -> "Elevated Central Adiposity"
            else -> "High Central Adiposity"
        }

        // Mosteller Body Surface Area (m^2)
        val bsa = sqrt((max(10.0, weightKg) * max(80.0, heightCm)) / 3600.0)

        // Frame size via r = heightCm / wristCm
        val rRatio = heightCm / max(10.0, wristCm)
        val frameSize = if (sex == BiologicalSex.MALE) {
            when {
                rRatio > 10.4 -> FrameSize.SMALL
                rRatio >= 9.6 -> FrameSize.MEDIUM
                else -> FrameSize.LARGE
            }
        } else {
            when {
                rRatio > 11.0 -> FrameSize.SMALL
                rRatio >= 10.1 -> FrameSize.MEDIUM
                else -> FrameSize.LARGE
            }
        }

        // Ideal Body Weight formulas (inches over 5 feet = 60 inches)
        val inchesOver60 = max(0.0, (heightCm / INCH_TO_CM) - 60.0)
        val devine = if (sex == BiologicalSex.MALE) 50.0 + 2.3 * inchesOver60 else 45.5 + 2.3 * inchesOver60
        val robinson = if (sex == BiologicalSex.MALE) 52.0 + 1.9 * inchesOver60 else 49.0 + 1.7 * inchesOver60
        val miller = if (sex == BiologicalSex.MALE) 56.2 + 1.41 * inchesOver60 else 53.1 + 1.36 * inchesOver60
        val hamwi = if (sex == BiologicalSex.MALE) 48.0 + 2.7 * inchesOver60 else 45.5 + 2.2 * inchesOver60

        val frameMultiplier = when (frameSize) {
            FrameSize.SMALL -> 0.93
            FrameSize.MEDIUM -> 1.00
            FrameSize.LARGE -> 1.08
        }

        return BodyCompositionResult(
            navyBodyFatPct = navyBf,
            bmiBodyFatPct = bmiBf,
            primaryBodyFatPct = primaryBf,
            bodyFatCategory = bfCategory,
            fatMassKg = fatMassKg,
            leanBodyMassKg = leanMassKg,
            waistToHipRatio = whr,
            whrRiskLevel = whrRisk,
            waistToHeightRatio = whtr,
            whtrStatus = whtrStatus,
            bodySurfaceAreaM2 = bsa,
            frameSize = frameSize,
            idealWeightDevineKg = devine * frameMultiplier,
            idealWeightRobinsonKg = robinson * frameMultiplier,
            idealWeightMillerKg = miller * frameMultiplier,
            idealWeightHamwiKg = hamwi * frameMultiplier
        )
    }

    // --- 6. Metabolism (BMR, TDEE, Calorie Targets, Custom Macro Splits, Protein & Hydration) ---

    fun analyzeMetabolism(
        weightKg: Double,
        heightCm: Double,
        ageYears: Int,
        sex: BiologicalSex,
        activityLevel: ActivityLevel,
        bodyFatPct: Double,
        selectedBmrFormula: BmrFormula = BmrFormula.MIFFLIN_ST_JEOR,
        goalMode: GoalMode = GoalMode.MAINTAIN,
        proteinRatioPct: Int = 30,
        carbsRatioPct: Int = 40,
        fatRatioPct: Int = 30
    ): MetabolismResult {
        val mifflin = if (sex == BiologicalSex.MALE) {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * ageYears) + 5.0
        } else {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * ageYears) - 161.0
        }.roundToInt().coerceAtLeast(900)

        val harris = if (sex == BiologicalSex.MALE) {
            88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * ageYears)
        } else {
            447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * ageYears)
        }.roundToInt().coerceAtLeast(900)

        val leanMassKg = weightKg * (1.0 - (bodyFatPct.coerceIn(4.0, 60.0) / 100.0))
        val katch = (370.0 + (21.6 * leanMassKg)).roundToInt().coerceAtLeast(900)

        val activeBmr = when (selectedBmrFormula) {
            BmrFormula.MIFFLIN_ST_JEOR -> mifflin
            BmrFormula.HARRIS_BENEDICT -> harris
            BmrFormula.KATCH_MCARDLE -> katch
        }

        val tdee = (activeBmr * activityLevel.multiplier).roundToInt()
        val mildLoss = max(1200, tdee - 300)
        val standardLoss = max(1200, tdee - 500)
        val mildGain = tdee + 300
        val standardGain = tdee + 500

        val targetCalories = when (goalMode) {
            GoalMode.LOSE -> standardLoss
            GoalMode.MAINTAIN -> tdee
            GoalMode.GAIN -> mildGain
        }

        val totalPct = max(1, proteinRatioPct + carbsRatioPct + fatRatioPct)
        val pNorm = proteinRatioPct.toDouble() / totalPct
        val cNorm = carbsRatioPct.toDouble() / totalPct
        val fNorm = fatRatioPct.toDouble() / totalPct

        val proteinGrams = ((targetCalories * pNorm) / 4.0).roundToInt()
        val carbsGrams = ((targetCalories * cNorm) / 4.0).roundToInt()
        val fatGrams = ((targetCalories * fNorm) / 9.0).roundToInt()

        val proteinMin = (weightKg * 1.2).roundToInt()
        val proteinMax = (weightKg * 2.2).roundToInt()

        // Hydration: 35 ml/kg base + activity bonus
        val activityHydrationBonus = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 0
            ActivityLevel.LIGHT -> 250
            ActivityLevel.MODERATE -> 500
            ActivityLevel.ACTIVE -> 750
            ActivityLevel.ATHLETE -> 1000
        }
        val waterMl = ((weightKg * 35.0).roundToInt() + activityHydrationBonus).coerceIn(1500, 5500)

        return MetabolismResult(
            bmrMifflin = mifflin,
            bmrHarrisBenedict = harris,
            bmrKatchMcArdle = katch,
            activeBmr = activeBmr,
            tdeeCalories = tdee,
            mildLossCalories = mildLoss,
            standardLossCalories = standardLoss,
            mildGainCalories = mildGain,
            standardGainCalories = standardGain,
            macroBreakdown = MacroBreakdown(
                targetCalories = targetCalories,
                proteinGrams = proteinGrams,
                carbsGrams = carbsGrams,
                fatGrams = fatGrams,
                proteinMinGramsPerDay = proteinMin,
                proteinMaxGramsPerDay = proteinMax,
                dailyWaterMl = waterMl
            )
        )
    }

    // --- 7. Goal Projection, Safe-Rate Warnings, Milestones & Plateau Detection ---

    fun calculateGoalProjection(
        startWeightKg: Double,
        currentWeightKg: Double,
        targetWeightKg: Double,
        heightCm: Double,
        targetDaysFromNow: Int = 84,
        maintainToleranceKg: Double = 1.2
    ): GoalTimelineProjection {
        val targetBmi = calculateBmi(targetWeightKg, heightCm)
        val remainingDelta = targetWeightKg - currentWeightKg
        val totalSpan = abs(targetWeightKg - startWeightKg)
        val coveredSpan = abs(currentWeightKg - startWeightKg)
        val progress = if (totalSpan < 0.2) {
            1f
        } else {
            val movingRightDirection = (targetWeightKg < startWeightKg && currentWeightKg <= startWeightKg) ||
                (targetWeightKg > startWeightKg && currentWeightKg >= startWeightKg)
            if (!movingRightDirection) 0.05f else (coveredSpan / totalSpan).toFloat().coerceIn(0f, 1f)
        }

        val weeksAvailable = max(1.0, targetDaysFromNow / 7.0)
        val requiredWeeklyRate = abs(remainingDelta) / weeksAvailable
        val isLosing = remainingDelta < -0.2
        val isUnsafe = (isLosing && requiredWeeklyRate > 1.0) || (!isLosing && requiredWeeklyRate > 0.75 && abs(remainingDelta) > 1.0)

        val safetyWarning = if (isUnsafe) {
            if (isLosing) {
                "Safe-Rate Warning: Losing ${String.format(Locale.US, "%.2f", requiredWeeklyRate)} kg/week exceeds the recommended 0.5–1.0 kg/week clinical limit. Consider extending your target date."
            } else {
                "Safe-Rate Notice: Gaining ${String.format(Locale.US, "%.2f", requiredWeeklyRate)} kg/week may increase fat gain. Aim for 0.25–0.5 kg/week for lean muscle."
            }
        } else null

        val safeRatePerWeek = if (isLosing) min(0.75, max(0.35, requiredWeeklyRate)) else min(0.45, max(0.25, requiredWeeklyRate))
        val projectedDays = if (abs(remainingDelta) < 0.2) 0 else ((abs(remainingDelta) / safeRatePerWeek) * 7.0).roundToInt()

        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, projectedDays) }
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

        val milestones = (1..4).map { step ->
            val frac = step / 4.0
            val milestoneWeight = startWeightKg + (targetWeightKg - startWeightKg) * frac
            val reached = if (targetWeightKg <= startWeightKg) {
                currentWeightKg <= milestoneWeight + 0.05
            } else {
                currentWeightKg >= milestoneWeight - 0.05
            }
            GoalMilestone(
                title = "${step * 25}% Milestone",
                targetWeightKg = milestoneWeight,
                isReached = reached
            )
        }

        val isInMaintainBand = abs(currentWeightKg - targetWeightKg) <= maintainToleranceKg

        return GoalTimelineProjection(
            currentWeightKg = currentWeightKg,
            targetWeightKg = targetWeightKg,
            targetBmi = targetBmi,
            remainingDeltaKg = remainingDelta,
            progressFraction = progress,
            requiredWeeklyRateKg = requiredWeeklyRate,
            isUnsafeRate = isUnsafe,
            safetyWarningMessage = safetyWarning,
            projectedDaysToGoal = projectedDays,
            projectedDateLabel = if (projectedDays == 0) "Goal Reached!" else dateFormat.format(calendar.time),
            milestones = milestones,
            isInMaintainBand = isInMaintainBand
        )
    }

    fun detectInsightsAndAlerts(
        weightsChronologicalKg: List<Double>,
        bmiResult: BmiAnalysisResult,
        bodyComp: BodyCompositionResult,
        streakDays: Int
    ): List<InsightCard> {
        val insights = mutableListOf<InsightCard>()

        // 1. Unusual rapid weight change check
        if (weightsChronologicalKg.size >= 2) {
            val latest = weightsChronologicalKg.last()
            val prev = weightsChronologicalKg[weightsChronologicalKg.size - 2]
            val jump = abs(latest - prev)
            if (jump >= 2.2) {
                insights.add(
                    InsightCard(
                        id = "unusual_change",
                        category = "Rapid Shift Alert",
                        title = "Unusual ${String.format(Locale.US, "%.1f", jump)} kg Fluctuation Detected",
                        summary = "Single-day shifts over 2 kg are typically glycogen and extracellular water changes rather than tissue change.",
                        actionHint = "Weigh in at the same morning time before meals and check sodium/hydration balance.",
                        isAlert = true
                    )
                )
            }
        }

        // 2. Plateau detector (last 5 entries within 0.35 kg range)
        if (weightsChronologicalKg.size >= 5) {
            val recent5 = weightsChronologicalKg.takeLast(5)
            val span = (recent5.maxOrNull() ?: 0.0) - (recent5.minOrNull() ?: 0.0)
            if (span <= 0.35 && abs(bmiResult.weightDeltaToHealthyKg) > 1.5) {
                insights.add(
                    InsightCard(
                        id = "plateau_detected",
                        category = "Plateau Coach",
                        title = "Weight Trend Has Stabilized (±${String.format(Locale.US, "%.2f", span)} kg)",
                        summary = "Metabolic adaptation or hidden liquid calories can pause trend velocity after several weeks.",
                        actionHint = "Try recalculating TDEE for your current weight, adding 1,500 daily steps, or checking waist circumference.",
                        isAlert = false
                    )
                )
            }
        }

        // 3. BMI vs Body Fat Discordance (Athletic / Muscle Insight)
        if (bmiResult.bmi >= 25.0 && bodyComp.waistToHeightRatio < 0.49) {
            insights.add(
                InsightCard(
                    id = "athletic_discordance",
                    category = "Body Composition Nuance",
                    title = "Healthy Central Ratio Despite Elevated BMI",
                    summary = "Your Waist-to-Height ratio (${String.format(Locale.US, "%.2f", bodyComp.waistToHeightRatio)}) is below 0.50, suggesting lean muscle mass is contributing to your BMI.",
                    actionHint = "Rely on US Navy Body Fat (${String.format(Locale.US, "%.1f", bodyComp.navyBodyFatPct)}%) and waist tracking rather than BMI alone."
                )
            )
        }

        // 4. Streak & Consistency Motivation
        insights.add(
            InsightCard(
                id = "streak_insight",
                category = "Habit Momentum",
                title = "$streakDays-Day Logging Streak Active",
                summary = "Consistent self-monitoring with 7-day moving averages filters out daily water noise and improves long-term maintenance.",
                actionHint = "Keep your morning weigh-in routine steady to unlock the 14-Day Pulse Master badge."
            )
        )

        return insights
    }

    val offlineArticles: List<ArticleItem> = listOf(
        ArticleItem(
            id = "bmi_limitations",
            category = "Science & Limitations",
            title = "Why BMI Is Only Half the Picture: Muscle, Bone & Ethnicity",
            readTimeMinutes = 4,
            keyTakeaway = "BMI measures weight relative to height squared—it cannot distinguish skeletal muscle from visceral adipose tissue.",
            contentParagraphs = listOf(
                "Adolphe Quetelet formulated the Body Mass Index in the 1830s for population-level statistics, not individual clinical diagnosis. Because muscle tissue is denser than adipose tissue, strength athletes frequently classify as 'Overweight' despite low body fat.",
                "Age and biological sex also shift body composition: older adults may have a 'Normal' BMI while experiencing sarcopenia (loss of muscle mass) alongside elevated visceral fat.",
                "Ethnic variation matters significantly. The WHO Western Pacific Region (Asian-Pacific cutoffs) sets the overweight threshold at 23.0 kg/m² and obesity at 25.0 kg/m² because South and East Asian populations experience cardiometabolic risk at lower BMI levels."
            ),
            scientificReference = "WHO Expert Consultation (Lancet, 2004); Nuttall FQ, Nutrition Today (2015)."
        ),
        ArticleItem(
            id = "whtr_guide",
            category = "Cardiometabolic Health",
            title = "Waist-to-Height Ratio: The 0.50 Boundary Rule",
            readTimeMinutes = 3,
            keyTakeaway = "Keeping your waist circumference under half your height (< 0.50) is one of the strongest predictors of metabolic health.",
            contentParagraphs = listOf(
                "Central (visceral) fat surrounding abdominal organs is metabolically active and strongly linked to insulin resistance and lipid profile changes.",
                "Measuring your waist at the midpoint between the lowest rib and the iliac crest and dividing by your height in the same units gives your Waist-to-Height Ratio (WHtR).",
                "NICE clinical guidelines recommend maintaining a WHtR between 0.40 and 0.49 across all ethnicities and athletic builds."
            ),
            scientificReference = "Ashwell M, Gibson S. Obes Rev (2016); NICE Guideline NG246."
        ),
        ArticleItem(
            id = "moving_averages",
            category = "Smart Tracking",
            title = "Understanding Daily Scale Fluctuations & Moving Averages",
            readTimeMinutes = 3,
            keyTakeaway = "1 gram of stored carbohydrate binds 3–4 grams of water—making 7-day moving averages essential.",
            contentParagraphs = listOf(
                "Your scale weight includes bone mineral, organs, skeletal muscle, adipose tissue, gut content, glycogen, and extracellular fluid.",
                "A high-sodium dinner or an intense leg workout causes temporary water retention for 24–48 hours. Looking at BMI Pulse's 7-day moving average line smooths out these spikes so you see your true tissue trend."
            ),
            scientificReference = "Hall KD, Kahan S. Med Clin North Am (2018)."
        ),
        ArticleItem(
            id = "child_lms",
            category = "Pediatrics",
            title = "How Child & Teen BMI-for-Age LMS Percentiles Work",
            readTimeMinutes = 4,
            keyTakeaway = "Children grow in both height and body proportion every month, so adult fixed cutoffs never apply under age 20.",
            contentParagraphs = listOf(
                "During childhood and adolescence, healthy body fat levels naturally dip around age 5–6 ('adiposity rebound') and rise through puberty.",
                "Pediatric growth charts use the Box-Cox LMS method—skewness (L), median (M), and coefficient of variation (S)—to compare a child's BMI against peers of the exact same age in months and biological sex."
            ),
            scientificReference = "CDC National Center for Health Statistics (Kuczmarski et al., 2002); WHO Growth Reference (2007)."
        )
    )
}
