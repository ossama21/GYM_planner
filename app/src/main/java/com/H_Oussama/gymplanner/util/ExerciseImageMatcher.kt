package com.H_Oussama.gymplanner.util

import android.content.Context
import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.min

/**
 * Utility class for matching exercise names to available exercise images
 * using fuzzy matching and fallback mechanisms.
 */
class ExerciseImageMatcher(private val context: Context) {
    // Cache of available image identifiers (without file extensions)
    private var availableImageIdentifiersCache: List<String>? = null
    
    // Common exercise name mappings - add more as needed
    private val commonNameMappings = mapOf(
        "squat" to "barbell_squats",
        "squats" to "barbell_squats",
        "barbell squat" to "barbell_squats",
        "bench" to "bench-press",
        "bench press" to "bench-press",
        "bp" to "bench-press",
        "row" to "barbbell_rows",
        "barbell row" to "barbbell_rows",
        "rows" to "barbbell_rows",
        "pull up" to "pull-ups",
        "pullup" to "pull-ups",
        "pullups" to "pull-ups",
        "dead lift" to "deadlift-howto",
        "dl" to "deadlift-howto",
        "push up" to "push-ups",
        "pushup" to "push-ups",
        "pushups" to "push-ups",
        "shoulder press" to "dumbbell-shoulder-press",
        "ohp" to "dumbbell-shoulder-press",
        "overhead press" to "dumbbell-shoulder-press",
        "incline press" to "incline-dumbbell-bench-press",
        "incline bench press" to "incline-dumbbell-bench-press",
        "leg press" to "leg-press",
        "rdl" to "romanian-deadlift",
        "romanian" to "romanian-deadlift",
        "lateral raise" to "cable-lateral-raise",
        "side raise" to "cable-lateral-raise",
        "cable fly" to "cable-fly",
        "flies" to "cable-fly",
        "flyes" to "cable-fly",
        "curls" to "barbell-curl",
        "hammer curls" to "hammer-curl",
        "tricep extension" to "lying-tricep-extension",
        "skull crusher" to "lying-tricep-extension",
        "lat pull" to "lat-pulldown",
        "lat pulldown" to "lat-pulldown",
        "cable row" to "seated-cable-row",
        "seated row" to "seated-cable-row",
        "calf raise" to "standing-calf-raise"
    )
    
    // Cache initialization
    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (availableImageIdentifiersCache == null) {
            availableImageIdentifiersCache = getAvailableImageIdentifiers()
        }
    }
    
    /**
     * Find the best matching image identifier for an exercise name or ID.
     * Uses a multi-step approach:
     * 1. Direct match (after normalization)
     * 2. Common name mapping lookup
     * 3. Keyword matching
     * 4. String similarity (Levenshtein distance)
     * 5. Fallback to default if no match found
     */
    suspend fun findBestMatchingImageIdentifier(exerciseNameOrId: String): String {
        initialize()
        
        val availableIdentifiers = availableImageIdentifiersCache ?: getAvailableImageIdentifiers()
        if (availableIdentifiers.isEmpty()) return ""
        
        val normalizedInput = normalizeString(exerciseNameOrId)
        
        // Step 1: Check for direct match
        val directMatch = availableIdentifiers.firstOrNull { 
            normalizeString(it) == normalizedInput 
        }
        if (directMatch != null) return directMatch
        
        // Step 2: Check common name mappings
        val mappedName = commonNameMappings[normalizedInput]
        if (mappedName != null && availableIdentifiers.any { normalizeString(it) == normalizeString(mappedName) }) {
            return mappedName
        }
        
        // Step 3: Keyword matching - check if any part of the exercise name matches an identifier
        val keywords = normalizedInput.split(" ", "-", "_")
        for (keyword in keywords) {
            if (keyword.length <= 2) continue // Skip very short keywords
            
            val keywordMatch = availableIdentifiers.firstOrNull {
                normalizeString(it).contains(keyword)
            }
            if (keywordMatch != null) return keywordMatch
        }
        
        // Step 4: Find closest match using Levenshtein distance
        var bestMatch = ""
        var bestDistance = Int.MAX_VALUE
        
        for (identifier in availableIdentifiers) {
            val normalizedIdentifier = normalizeString(identifier)
            val distance = levenshteinDistance(normalizedInput, normalizedIdentifier)
            
            if (distance < bestDistance) {
                bestDistance = distance
                bestMatch = identifier
            }
        }
        
        // Only use best match if it's reasonably close
        if (bestDistance <= normalizedInput.length / 2) {
            return bestMatch
        }
        
        // Step 5: Fallback to a sensible default
        val commonExercises = listOf("bench-press", "barbell_squats", "deadlift-howto", "pull-ups")
        return commonExercises.firstOrNull { identifier -> 
            availableIdentifiers.any { it == identifier }
        } ?: availableIdentifiers.first()
    }
    
    /**
     * Get a list of all available image identifiers from assets.
     */
    private suspend fun getAvailableImageIdentifiers(): List<String> = withContext(Dispatchers.IO) {
        try {
            context.assets.list("exercise_images")?.map { filename ->
                // Remove the file extension
                filename.substringBeforeLast(".")
            } ?: emptyList()
        } catch (e: IOException) {
            emptyList()
        }
    }
    
    /**
     * Normalize a string for comparison.
     * Converts to lowercase, replaces separators with spaces, and removes duplicate spaces.
     */
    private fun normalizeString(input: String): String {
        return input.lowercase()
            .replace("[_\\-.]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
    
    /**
     * Calculate the Levenshtein distance between two strings.
     * This measures how many single-character changes are needed to convert one string to another.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) {
            dp[i][0] = i
        }
        
        for (j in 0..s2.length) {
            dp[0][j] = j
        }
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        
        return dp[s1.length][s2.length]
    }
} 