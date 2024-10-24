package com.example.planperfect.utils

class PreprocessingUtils {

    // Example values for mean and std (these should come from your Python training code)
    private val meanLatitude = 37.7749   // Replace with actual mean
    private val stdLatitude = 0.1        // Replace with actual std
    private val meanLongitude = -122.419 // Replace with actual mean
    private val stdLongitude = 0.1       // Replace with actual std

    // Normalizing the latitude and longitude
    fun normalizeCoordinates(latitude: Double, longitude: Double): Pair<Double, Double> {
        val normalizedLat = (latitude - meanLatitude) / stdLatitude
        val normalizedLon = (longitude - meanLongitude) / stdLongitude
        return Pair(normalizedLat, normalizedLon)
    }

    // One-hot encoding for category (assume 8 categories as in your training code)
    fun oneHotEncodeCategory(categoryIndex: Int): FloatArray {
        val oneHotVector = FloatArray(8) // Assuming 8 categories
        oneHotVector[categoryIndex] = 1.0f
        return oneHotVector
    }
}
