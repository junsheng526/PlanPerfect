package com.example.planperfect.data.model

import com.google.firebase.firestore.DocumentId

data class UserRating(
    @DocumentId
    var userId: String = "",
    var placeId: String = "", // Add this field to associate the review with a place
    var rating: Float = 0f,
    var review: String = ""
)