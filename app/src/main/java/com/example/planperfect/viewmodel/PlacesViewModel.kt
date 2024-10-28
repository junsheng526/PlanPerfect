package com.example.planperfect.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.utils.DummyDataUtil
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.UUID

class PlacesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val placesCollection = Firebase.firestore.collection("tourist_places")
    val placesLiveData = MutableLiveData<List<TouristPlace>>()

    val favoritePlacesLiveData = MutableLiveData<List<TouristPlace>>()

    init {
        // Fetch places in real-time
        placesCollection.addSnapshotListener { snap, error ->
            if (error != null) {
                Log.e("Firestore", "Error fetching places: $error")
                return@addSnapshotListener
            }
            if (snap != null) {
                val places = snap.toObjects<TouristPlace>()
                placesLiveData.value = places
            }
        }
    }

    fun importTouristPlacesFromCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Access the raw resource
                val inputStream = context.resources.openRawResource(R.raw.data)
                val reader = BufferedReader(InputStreamReader(inputStream))

                // Skip the header line
                reader.readLine()

                // Read each line from the CSV
                reader.forEachLine { line ->
                    try {
                        // Split by comma, but handle quoted strings
                        val data = parseCsvLine(line)

                        if (data.size >= 7) { // Ensure we have at least 7 fields
                            val touristPlace = TouristPlace(
                                id = UUID.randomUUID().toString(),
                                name = data[1].trim(),
                                description = getFirstTwoSentences(data[2].trim()),
                                longDescription = data[2].trim(),
                                imageUrls = listOf(data[3].trim()),
                                category = data[4].trim(),
                                latitude = data[5].trim().toDouble(),
                                longitude = data[6].trim().toDouble(),
                                currencyCode = "MYR"
                            )

                            // Insert into Firestore
                            placesCollection.document(touristPlace.id).set(touristPlace)
                            Log.d("Firestore", "Added: ${touristPlace.name}")
                        } else {
                            Log.e("CSVError", "Line skipped, not enough data: $line")
                        }
                    } catch (e: Exception) {
                        Log.e("CSVError", "Error processing line: $line. Error: ${e.message}")
                    }
                }
                Log.d("Firestore", "All tourist places imported from CSV successfully.")
            } catch (e: Exception) {
                Log.e("Firestore", "Error importing tourist places from CSV: ${e.message}")
            }
        }
    }

    private fun getFirstTwoSentences(description: String): String {
        // Split the description by '.', '!', or '?' to get sentences
        val sentences = description.split(Regex("[.!?]"))
            .map { it.trim() } // Trim whitespace from each sentence
            .filter { it.isNotEmpty() } // Remove any empty sentences

        // Join the first two sentences and add a period at the end if they exist
        return sentences.take(2).joinToString(". ") + if (sentences.size > 2) "." else ""
    }

    private fun parseCsvLine(line: String): List<String> {
        val regex = Regex("\"([^\"]*)\"|([^,\"]+)")
        return regex.findAll(line).map { it.value.trim(' ', '"') }.toList()
    }

    suspend fun getPlacesByName(name: String): List<TouristPlace> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = placesCollection.whereEqualTo("name", name).get().await()
                querySnapshot.toObjects<TouristPlace>()
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching places by name: $e")
                emptyList()
            }
        }
    }

    suspend fun getPlaceByName(name: String): TouristPlace? {
        return withContext(Dispatchers.IO) {
            try {
                // Fetching documents where name matches the given name
                val querySnapshot = placesCollection.whereEqualTo("name", name).get().await()

                // Convert the first matching document to TouristPlace
                val places = querySnapshot.toObjects<TouristPlace>()
                places.firstOrNull() // Return the first place found, or null if no matches
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching places by name: $e")
                null // Return null if an error occurs
            }
        }
    }

    // Fetch specific place by ID (optional)
    suspend fun getPlaceById(id: String): TouristPlace? {
        return try {
            val document = placesCollection.document(id).get().await()
            document.toObject<TouristPlace>()
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching place by ID: $e")
            null
        }
    }

    // Manually fetch places if needed (not real-time)
    suspend fun fetchPlaces(): List<TouristPlace> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = placesCollection.get().await()
                snapshot.toObjects<TouristPlace>()
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching places: $e")
                emptyList()
            }
        }
    }

    suspend fun getFavoritePlaces(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch all favorite place IDs from the "favorites" collection
                val favoriteDocs = db.collection("user")
                    .document(userId)
                    .collection("favorites")
                    .get()
                    .await()

                // Extract place IDs from the favorites
                val favoritePlaceIds = favoriteDocs.documents.map { it.getString("id")!! }

                // Fetch TouristPlace details for each favorite place ID
                val favoritePlaces = favoritePlaceIds.mapNotNull { placeId ->
                    val placeDoc = placesCollection.document(placeId)
                        .get()
                        .await()
                    placeDoc.toObject(TouristPlace::class.java)
                }

                // Post the fetched places to LiveData
                withContext(Dispatchers.Main) {
                    favoritePlacesLiveData.value = favoritePlaces
                }
            } catch (e: Exception) {
                // Handle any errors
                withContext(Dispatchers.Main) {
                    favoritePlacesLiveData.value = emptyList()
                }
            }
        }
    }
}