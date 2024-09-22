package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planperfect.data.model.Day
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.data.model.Trip
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TripViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("trip")
    val trips = MutableLiveData<List<Trip>>()

    init {
        // Listen for real-time updates to trips collection
        col.addSnapshotListener { snap, _ -> trips.value = snap?.toObjects() }
    }

    // Fetch a specific trip by its ID
    suspend fun get(id: String): Trip? {
        return try {
            col.document(id).get().await().toObject<Trip>()
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching trip by ID: $e")
            null
        }
    }

    // Fetch a trip by a specific attribute (like destination)
    suspend fun getTripByDestination(destination: String): Trip? {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = col.whereEqualTo("destination", destination).limit(1).get().await()
                val document = querySnapshot.documents.firstOrNull()
                if (document != null && document.exists()) {
                    val trip = document.toObject<Trip>()
                    trip?.id = document.id
                    trip
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching trip by destination: $e")
                null
            }
        }
    }

    // Save or update a trip
    suspend fun set(trip: Trip): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                col.document(trip.id).set(trip)
                    .addOnSuccessListener {
                        Log.i("Firestore", "Trip saved successfully: $trip")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error saving trip: $e")
                    }
                    .await()
                true
            } catch (e: Exception) {
                Log.e("Firestore", "Firestore operation failed: $e")
                false
            }
        }
    }

    suspend fun getPlacesForDay(tripId: String, dayId: String): List<TouristPlace> {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("trip")
                .document(tripId)
                .collection("itineraries")
                .document(dayId)
                .get()
                .await()

            snapshot.get("places")?.let {
                (it as List<HashMap<String, Any>>).map { placeMap ->
                    TouristPlace(
                        name = placeMap["name"] as String,
                        category = placeMap["category"] as String,
                        latitude = placeMap["latitude"] as? Double,
                        longitude = placeMap["longitude"] as? Double
                        // Map other fields as necessary
                    )
                } ?: emptyList()
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching places for day: $e")
            emptyList()
        }
    }

    suspend fun removePlace(tripId: String, dayId: String, place: TouristPlace): Boolean {
        Log.d("removePlace :: ", tripId)
        Log.d("removePlace :: ", dayId)
        Log.d("removePlace :: ", place.toString())
        return try {
            // Ensure you're referencing a specific document inside the 'itineraries' collection.
            val documentRef = FirebaseFirestore.getInstance()
                .collection("trip")
                .document(tripId)
                .collection("itineraries")
                .document(dayId)

            // Get the current places list from Firestore
            val snapshot = documentRef.get().await()
            val placesList = snapshot.get("places") as? MutableList<HashMap<String, Any>> ?: mutableListOf()

            // Find and remove the matching place from the list
            val placeToRemove = placesList.find { it["name"] == place.name && it["category"] == place.category }
            placeToRemove?.let {
                placesList.remove(it)

                // Update the document with the modified places list
                documentRef.update("places", placesList).await()
            }

            true // Return true if the operation is successful
        } catch (e: Exception) {
            Log.e("Firestore", "Error removing place: $e")
            false
        }
    }

    // Helper function to convert TouristPlace to a Map for Firestore compatibility
    private fun TouristPlace.toMap(): Map<String, Any?> {
        return mapOf(
            "name" to this.name,
            "category" to this.category,
            "startTime" to this.startTime,
            "endTime" to this.endTime,
            "notes" to this.notes,
            "imageUrl" to this.imageUrl
        )
    }
}
