package com.H_Oussama.gymplanner.data.parser

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import java.lang.StringBuilder

/**
 * Parser that converts JSON workout plans to the text format expected by WorkoutPlanParser.
 * This allows importing workout plans from JSON files.
 */
object JsonWorkoutPlanParser {

    /**
     * Parse a JSON workout plan into the text format required by WorkoutPlanParser.
     * 
     * @param jsonString The JSON string containing the workout plan
     * @return A formatted text string that can be passed to WorkoutPlanParser
     * @throws JSONException if the JSON is malformed
     * @throws IllegalArgumentException if the JSON structure is invalid
     */
    fun parseJsonToTextFormat(jsonString: String): String {
        try {
            val json = JSONObject(jsonString)
            val sb = StringBuilder()

            // Extract plan name
            val planName = json.optString("Plan Name", "Imported Workout Plan")
            sb.appendLine("Plan Name: $planName")
            sb.appendLine()

            // Check if "Days" key exists
            if (!json.has("Days")) {
                throw IllegalArgumentException("JSON must contain a 'Days' array")
            }

            // Extract days
            val days = json.getJSONArray("Days")
            for (i in 0 until days.length()) {
                val day = days.getJSONObject(i)
                
                // Get day info
                val dayOfWeek = day.optString("Day", "Day ${i+1}")
                val label = day.optString("Label", "")
                
                // Extract target muscle groups (primary, secondary)
                val targetMuscles = StringBuilder()
                if (day.has("Target")) {
                    val targets = day.getJSONArray("Target")
                    if (targets.length() > 0) {
                        targetMuscles.append(" {")
                        for (j in 0 until targets.length()) {
                            if (j > 0) targetMuscles.append(", ")
                            targetMuscles.append(targets.getString(j))
                        }
                        targetMuscles.append("}")
                    }
                }
                
                // Convert day name to the expected format
                val weekdayShort = getWeekdayShortForm(dayOfWeek)
                sb.appendLine("Day ${i + 1}: [$weekdayShort] $label$targetMuscles")
                
                // Process exercises if available
                val exercises = day.optJSONArray("Exercises")
                if (exercises != null && exercises.length() > 0) {
                    for (j in 0 until exercises.length()) {
                        val exercise = exercises.getJSONObject(j)
                        
                        try {
                            val name = exercise.getString("name")
                            val sets = exercise.getInt("sets")
                            // Handle reps which might be a string or number
                            val reps = exercise.getString("reps")
                            val rest = exercise.optString("rest", "60s")
                            
                            // Format: - Exercise name | sets×reps | rest
                            val setsDescription = "$sets×$reps"
                            sb.appendLine("- $name | $setsDescription | $rest")
                        } catch (e: Exception) {
                            // Handle missing fields with a more specific error
                            val missingField = when {
                                !exercise.has("name") -> "name"
                                !exercise.has("sets") -> "sets"
                                !exercise.has("reps") -> "reps"
                                else -> "unknown field"
                            }
                            throw IllegalArgumentException("Exercise at index $j missing required field: $missingField")
                        }
                    }
                } else if (label.lowercase().contains("rest")) {
                    sb.appendLine("- Rest day")
                }
                
                sb.appendLine()
            }

            return sb.toString()
        } catch (e: JSONException) {
            throw IllegalArgumentException("Invalid JSON format: ${e.message}")
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Error parsing workout plan: ${e.message}")
        }
    }
    
    /**
     * Converts full weekday names to the short form expected by the WorkoutPlanParser
     */
    private fun getWeekdayShortForm(dayName: String): String {
        return when (dayName.lowercase()) {
            "monday" -> "Mon"
            "tuesday" -> "Tue"
            "wednesday" -> "Wed"
            "thursday" -> "Thu"
            "friday" -> "Fri"
            "saturday" -> "Sat"
            "sunday" -> "Sun"
            else -> dayName.take(3) // Take first 3 chars if unknown
        }
    }
} 