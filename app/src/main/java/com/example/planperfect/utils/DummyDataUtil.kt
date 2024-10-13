package com.example.planperfect.utils

import com.example.planperfect.data.model.Itinerary
import com.example.planperfect.data.model.TouristPlace
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.util.*
object  DummyDataUtil {

    suspend fun importTouristPlacesFromCsv(filePath: String) {
        val firestore = FirebaseFirestore.getInstance()
        val placesCollection = firestore.collection("tourist_places")

        // Read the CSV file
        val file = File(filePath)
        val csvParser = CSVParser.parse(file, Charsets.UTF_8, CSVFormat.DEFAULT.withHeader())

        // Insert places into Firestore
        try {
            for (record in csvParser) {
                // Map CSV fields to the TouristPlace data class
                val place = TouristPlace(
                    id = UUID.randomUUID().toString(),
                    name = record["title"], // Map title
                    description = record["description"], // Map description
                    imageUrls = listOf(record["image_url"]), // Map image_url as single element list
                    category = record["category"], // Map category
                    latitude = record["latitude"].toDouble(), // Convert latitude to Double
                    longitude = record["longitude"].toDouble() // Convert longitude to Double
                )

                // Insert the place into Firestore
                placesCollection.document(place.id).set(place).await()
                println("Added: ${place.name}")
            }
            println("Tourist places imported successfully.")
        } catch (e: Exception) {
            println("Error adding places: ${e.message}")
        } finally {
            csvParser.close()
        }
    }

    suspend fun createDummyMalaysiaTouristPlaces() {
        val firestore = FirebaseFirestore.getInstance()
        val placesCollection = firestore.collection("tourist_places")

        // Define a list of dummy tourist places in Malaysia with coordinates and multiple images
        val dummyPlaces = listOf(
            TouristPlace(
                id = UUID.randomUUID().toString(),
                name = "Petronas Twin Towers",
                description = "Iconic skyscrapers in Kuala Lumpur, known for their sky bridge and observation deck.",
                imageUrls = listOf(
                    "https://www.malaysiavisa.ae/blog/wp-content/uploads/2019/06/Petronas-Twin-Towers-Malaysia.jpg",
                    "https://images.locationscout.net/2018/05/klcc-park-at-petronas-twin-towers-malaysia.webp?h=1400&q=80",
                    "https://plus.unsplash.com/premium_photo-1700955569542-735a654503bf?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGV0cm9uYXMlMjB0d2luJTIwdG93ZXJzJTJDJTIwa3VhbGElMjBsdW1wdXIlMkMlMjBtYWxheXNpYXxlbnwwfHwwfHx8MA%3D%3D"
                ),
                category = "Landmark",
                latitude = 3.1570,
                longitude = 101.7115
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