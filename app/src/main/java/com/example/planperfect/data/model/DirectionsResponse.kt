package com.example.planperfect.data.model

data class DirectionsResponse(
    val type: String,
    val features: List<Feature>,
    val bbox: List<Double>
)

data class Feature(
    val type: String,
    val properties: Properties,
    val geometry: Geometry
)

data class Properties(
    val segments: List<Segment>,
    val summary: Summary
)

data class Segment(
    val distance: Double,
    val duration: Double,
    val steps: List<Step>
)

data class Step(
    val distance: Double,
    val duration: Double,
    val instruction: String,
    val name: String,
    val way_points: List<Int>
)

data class Geometry(
    val type: String,
    val coordinates: List<List<Double>>
)

data class Summary(
    val distance: Double,
    val duration: Double
)