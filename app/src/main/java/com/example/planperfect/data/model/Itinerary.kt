package com.example.planperfect.data.model

import com.google.firebase.firestore.DocumentId

data class Itinerary(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val homeCity: String = "",
    val countryToVisit: String = "",
    val imageUrl: String = ""
)