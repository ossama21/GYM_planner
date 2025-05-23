package com.H_Oussama.gymplanner.utils.calculators

/**
 * Enum representing BMI categories
 */
enum class BmiCategory {
    UNDERWEIGHT,
    NORMAL,
    OVERWEIGHT,
    OBESE,
    SEVERELY_OBESE;

    override fun toString(): String {
        return when (this) {
            UNDERWEIGHT -> "Underweight"
            NORMAL -> "Normal Weight"
            OVERWEIGHT -> "Overweight"
            OBESE -> "Obese"
            SEVERELY_OBESE -> "Severely Obese"
        }
    }

    companion object {
        fun fromBmi(bmi: Double): BmiCategory {
            return when {
                bmi < 18.5 -> UNDERWEIGHT
                bmi < 25.0 -> NORMAL
                bmi < 30.0 -> OVERWEIGHT
                bmi < 35.0 -> OBESE
                else -> SEVERELY_OBESE
            }
        }
    }
}

/**
 * Enum representing Body Fat categories
 */
enum class BodyFatCategory {
    ESSENTIAL_FAT,
    ATHLETES,
    FITNESS,
    AVERAGE,
    OBESE;

    override fun toString(): String {
        return when (this) {
            ESSENTIAL_FAT -> "Essential Fat"
            ATHLETES -> "Athletic"
            FITNESS -> "Fitness"
            AVERAGE -> "Average"
            OBESE -> "Obese"
        }
    }

    companion object {
        fun fromBodyFat(bodyFat: Double, isMale: Boolean): BodyFatCategory {
            return if (isMale) {
                // Male category thresholds
                when {
                    bodyFat < 5.0 -> ESSENTIAL_FAT
                    bodyFat < 13.0 -> ATHLETES
                    bodyFat < 17.0 -> FITNESS
                    bodyFat < 25.0 -> AVERAGE
                    else -> OBESE
                }
            } else {
                // Female category thresholds
                when {
                    bodyFat < 13.0 -> ESSENTIAL_FAT
                    bodyFat < 20.0 -> ATHLETES
                    bodyFat < 24.0 -> FITNESS
                    bodyFat < 32.0 -> AVERAGE
                    else -> OBESE
                }
            }
        }
    }
}

/**
 * Enum representing gender
 */
enum class Gender {
    MALE,
    FEMALE;
}

/**
 * Enum representing body fat calculation methods
 */
enum class BodyFatMethod {
    NAVY,
    BMI;

    val displayName: String
        get() = when (this) {
            NAVY -> "U.S. Navy Method"
            BMI -> "BMI Method"
        }

    val description: String
        get() = when (this) {
            NAVY -> "Uses circumference measurements of neck, waist and hips"
            BMI -> "Estimates body fat based on BMI, age, and gender"
        }
}

/**
 * Enum representing TDEE calculation formulas
 */
enum class TdeeFormula {
    MIFFLIN_ST_JEOR,
    HARRIS_BENEDICT,
    KATCH_MCARDLE;

    val displayName: String
        get() = when (this) {
            MIFFLIN_ST_JEOR -> "Mifflin-St Jeor"
            HARRIS_BENEDICT -> "Harris-Benedict"
            KATCH_MCARDLE -> "Katch-McArdle"
        }
}

/**
 * Extension to add display name to ActivityLevel
 */
val ActivityLevel.displayName: String
    get() = when (this) {
        ActivityLevel.SEDENTARY -> "Sedentary (little or no exercise)"
        ActivityLevel.LIGHTLY_ACTIVE -> "Lightly Active (light exercise 1-3 days/week)"
        ActivityLevel.MODERATELY_ACTIVE -> "Moderately Active (moderate exercise 3-5 days/week)"
        ActivityLevel.VERY_ACTIVE -> "Very Active (hard exercise 6-7 days/week)"
        ActivityLevel.EXTRA_ACTIVE -> "Extra Active (very hard exercise & physical job)"
    } 
 
 