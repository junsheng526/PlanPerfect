package com.example.planperfect.data.model

import com.google.firebase.firestore.DocumentId

data class Trip(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var homeCity: String = "",
    var destination: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var userId: String = "",
    val imageUrl: String = "",
)