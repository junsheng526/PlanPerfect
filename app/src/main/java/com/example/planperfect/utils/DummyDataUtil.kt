package com.example.planperfect.utils

import com.example.planperfect.data.model.TouristPlace
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
class DummyDataUtil {
    suspend fun createDummyTouristPlaces() {
        val firestore = FirebaseFirestore.getInstance()
        val placesCollection = firestore.collection("tourist_places")

        // Define a list of dummy tourist places with new data fields
        val dummyPlaces = listOf(
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Eiffel Tower",
                description = "Iconic Parisian landmark",
                imageUrl = "https://example.com/eiffel_tower.jpg",  // Replace with actual image URLs
                category = "Landmark"
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Colosseum",
                description = "Ancient Roman gladiatorial arena",
                imageUrl = "https://example.com/colosseum.jpg",
                category = "Historical Site"
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Great Wall of China",
                description = "Ancient fortification in China",
                imageUrl = "https://example.com/great_wall.jpg",
                category = "Historical Site"
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Statue of Liberty",
                description = "Famous American symbol of freedom",
                imageUrl = "https://example.com/statue_of_liberty.jpg",
                category = "Monument"
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Sydney Opera House",
                description = "Iconic Australian performing arts center",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
                category = "Landmark"
            )
        )

        // Insert dummy places into Firestore
        try {
            for (place in dummyPlaces) {
                placesCollection.document(place.id).set(place).await()
                println("Added: ${place.name}")
            }
            println("Dummy tourist places added successfully.")
        } catch (e: Exception) {
            println("Error adding dummy places: ${e.message}")
        }
    }
}