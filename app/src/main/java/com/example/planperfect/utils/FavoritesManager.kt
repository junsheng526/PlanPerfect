package com.example.planperfect.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FavoritesManager(private val userId: String) {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun addFavorite(touristPlaceId: String) {
        db.collection("user")
            .document(userId)
            .collection("favorites")
            .document(touristPlaceId)
            .set(mapOf("id" to touristPlaceId))
            .await()
    }

    suspend fun removeFavorite(touristPlaceId: String) {
        db.collection("user")
            .document(userId)
            .collection("favorites")
            .document(touristPlaceId)
            .delete()
            .await()
    }

    suspend fun isFavorite(touristPlaceId: String): Boolean {
        val doc = db.collection("user")
            .document(userId)
            .collection("favorites")
            .document(touristPlaceId)
            .get()
            .await()
        return doc.exists()
    }
}