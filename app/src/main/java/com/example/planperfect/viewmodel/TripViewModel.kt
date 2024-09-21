package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planperfect.data.model.Day
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.data.model.Trip
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
                        category = placeMap["category"] as String
                        // Map other fields as necessary
                    )
                } ?: emptyList()
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching places for day: $e")
            emptyList()
        }
    }
}
