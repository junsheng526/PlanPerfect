package com.example.planperfect.utils

import com.example.planperfect.data.model.Itinerary
import com.example.planperfect.data.model.TouristPlace
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
object  DummyDataUtil {

    suspend fun createDummyMalaysiaTouristPlaces() {
        val firestore = FirebaseFirestore.getInstance()
        val placesCollection = firestore.collection("tourist_places")

        // Define a list of dummy tourist places in Malaysia with coordinates
        val dummyPlaces = listOf(
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Petronas Twin Towers",
                description = "Iconic skyscrapers in Kuala Lumpur, known for their sky bridge and observation deck.",
                imageUrl = "https://www.malaysiavisa.ae/blog/wp-content/uploads/2019/06/Petronas-Twin-Towers-Malaysia.jpg",
                category = "Landmark",
                latitude = 3.1570, // Latitude for Petronas Twin Towers
                longitude = 101.7115 // Longitude for Petronas Twin Towers
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Batu Caves",
                description = "A limestone hill featuring a series of caves and cave temples, known for its large statue of Lord Murugan.",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRk9azULZKTyouv3nwgNOLssEuhWo4OGvE0sg&s",
                category = "Cultural Site",
                latitude = 3.2389, // Latitude for Batu Caves
                longitude = 101.6820 // Longitude for Batu Caves
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "George Town",
                description = "Capital of the state of Penang, famous for its well-preserved colonial buildings and street food.",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQrS0sRxNuXqv4vvMbL8_ysofuLI2YYXJIPFQ&s",
                category = "Historical Site",
                latitude = 5.4141, // Latitude for George Town
                longitude = 100.3288 // Longitude for George Town
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Langkawi",
                description = "An archipelago known for its stunning beaches, clear waters, and lush rainforests.",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT5K5KZ3svdI31zieDyvpq1GfLHT2hami9GLw&s",
                category = "Nature",
                latitude = 6.3754, // Latitude for Langkawi
                longitude = 99.6769 // Longitude for Langkawi
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Mount Kinabalu",
                description = "The highest peak in Southeast Asia, offering challenging hikes and breathtaking views.",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ8vNn_sXror3wQQtbKI-lPg8kxLZLp-bCagQ&s",
                category = "Adventure",
                latitude = 5.9751, // Latitude for Mount Kinabalu
                longitude = 116.5583 // Longitude for Mount Kinabalu
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Putrajaya",
                description = "The federal administrative center of Malaysia, known for its modern architecture and beautiful gardens.",
                imageUrl = "https://ik.imagekit.io/tvlk/blog/2022/07/du-lich-putrajaya-1.jpg?tr=c-at_max",
                category = "Government",
                latitude = 2.9252, // Latitude for Putrajaya
                longitude = 101.6760 // Longitude for Putrajaya
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Kuala Lumpur Bird Park",
                description = "Home to a wide variety of bird species, this park is a popular attraction for nature lovers.",
                imageUrl = "https://minio.havehalalwilltravel.com/hhwt-upload/original_images/16012023014457_1596689175418_DSC02427.JPG",
                category = "Nature",
                latitude = 3.1561, // Latitude for Kuala Lumpur Bird Park
                longitude = 101.6869 // Longitude for Kuala Lumpur Bird Park
            ),
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Perhentian Islands",
                description = "A group of islands known for their crystal clear waters, ideal for snorkeling and diving.",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQZgGZda5kUlFrw-DfCXOyP5krzb8jC7eYdNA&s",
                category = "Beach",
                latitude = 5.9100, // Latitude for Perhentian Islands
                longitude = 102.7493 // Longitude for Perhentian Islands
            )
        )

        // Insert dummy places into Firestore
        try {
            for (place in dummyPlaces) {
                placesCollection.document(place.id).set(place).await()
                println("Added: ${place.name}")
            }
            println("Dummy Malaysia tourist places added successfully.")
        } catch (e: Exception) {
            println("Error adding dummy places: ${e.message}")
        }
    }
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