package com.example.planperfect.data.model

import android.net.Uri
import com.google.firebase.firestore.DocumentId

data class TouristPlace(
    @DocumentId
    val id: String = "",  // Default values for no-argument constructor
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "", // URL
    val category: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "")
}