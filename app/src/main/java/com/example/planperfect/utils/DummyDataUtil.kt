package com.example.planperfect.utils

import com.example.planperfect.data.model.Itinerary
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

    suspend fun createDummyItineraries() {
        val firestore = FirebaseFirestore.getInstance()
        val itineraryCollection = firestore.collection("itinerary")

        // Define a list of dummy itineraries
        val dummyItineraries = listOf(
            Itinerary(
                id = UUID.randomUUID().toString(),
                name = "Paris Adventure",
                startDate = "2024-07-01",
                endDate = "2024-07-10",
                homeCity = "New York",
                countryToVisit = "France",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
            ),
            Itinerary(
                id = UUID.randomUUID().toString(),
                name = "Tokyo Excursion",
                startDate = "2024-09-15",
                endDate = "2024-09-25",
                homeCity = "Los Angeles",
                countryToVisit = "Japan",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
            ),
            Itinerary(
                id = UUID.randomUUID().toString(),
                name = "Sydney Explorer",
                startDate = "2024-11-05",
                endDate = "2024-11-15",
                homeCity = "London",
                countryToVisit = "Australia",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
            ),
            Itinerary(
                id = UUID.randomUUID().toString(),
                name = "Rome Getaway",
                startDate = "2024-05-20",
                endDate = "2024-05-30",
                homeCity = "Toronto",
                countryToVisit = "Italy",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
            ),
            Itinerary(
                id = UUID.randomUUID().toString(),
                name = "Beijing Historical Tour",
                startDate = "2024-06-01",
                endDate = "2024-06-10",
                homeCity = "Vancouver",
                countryToVisit = "China",
                imageUrl = "https://example.com/sydney_opera_house.jpg",
            )
        )

        // Insert dummy itineraries into Firestore
        try {
            for (itinerary in dummyItineraries) {
                itineraryCollection.document(itinerary.id).set(itinerary).await()
                println("Added: ${itinerary.name}")
            }
            println("Dummy itineraries added successfully.")
        } catch (e: Exception) {
            println("Error adding dummy itineraries: ${e.message}")
        }
    }
}