package com.example.planperfect.data.model

data class Day(
    val dayId: String = "",  // Identifier for the day (e.g., "tripId-Day-1")
    val places: List<TouristPlace> = emptyList()  // List of places for this day
)