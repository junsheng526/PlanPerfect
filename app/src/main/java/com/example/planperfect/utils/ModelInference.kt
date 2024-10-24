package com.example.planperfect.utils

import ModelUtils
import android.content.Context
import org.tensorflow.lite.Interpreter

class ModelInference(private val context: Context) {

    private lateinit var interpreter: Interpreter
    private lateinit var labelEncoder: Map<Int, String>

    // Load the model and label encoder
    init {
        val modelUtils = ModelUtils(context)
        val modelFile = modelUtils.loadModelFile()
        interpreter = Interpreter(modelFile)
        labelEncoder = modelUtils.loadLabelEncoder()
    }

    // Method to run inference on the model
    fun predictCategory(categoryIndex: Int, latitude: Double, longitude: Double): String {
        // Preprocess the input
        val preprocessingUtils = PreprocessingUtils()
        val normalizedCoordinates = preprocessingUtils.normalizeCoordinates(latitude, longitude)
        val oneHotEncodedCategory = preprocessingUtils.oneHotEncodeCategory(categoryIndex)

        // Create the input array (1x10 shape: 8 for category, 2 for lat/lon)
        val input = FloatArray(10)
        oneHotEncodedCategory.copyInto(input, 0, 0, 8) // Copy one-hot category encoding
        input[8] = normalizedCoordinates.first.toFloat()  // Normalized latitude
        input[9] = normalizedCoordinates.second.toFloat() // Normalized longitude

        // Prepare output array (this should match the number of output labels)
        val output = Array(1) { FloatArray(labelEncoder.size) }

        // Run inference
        interpreter.run(input, output)

        // Get the predicted label index (index of the highest probability)
        val predictedIndex = output[0].indexOfMax()

        // Map the predicted index back to the label using labelEncoder
        return labelEncoder[predictedIndex] ?: "Unknown"
    }

    // Helper function to find the index of the max value in a FloatArray
    private fun FloatArray.indexOfMax(): Int {
        return this.withIndex().maxByOrNull { it.value }?.index ?: -1
    }
}
