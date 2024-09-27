package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.data.model.Trip
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class TripViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("trip")
    val trips = MutableLiveData<List<Trip>>()

    private val _tripCountByYear = MutableLiveData<Map<Int, Int>>()
    val tripCountByYear: LiveData<Map<Int, Int>> get() = _tripCountByYear

    init {
        // Listen for real-time updates to trips collection
        col.addSnapshotListener { snap, _ ->
            val tripList = snap?.toObjects<Trip>() ?: emptyList()
            trips.value = tripList
            groupTripsByYear(tripList) // Group the trips by year
        }
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
                val querySnapshot =
                    col.whereEqualTo("destination", destination).limit(1).get().await()
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
            val snapshot = col.document(tripId)
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
            val documentRef = col.document(tripId)
                .collection("itineraries")
                .document(dayId)

            // Get the current places list from Firestore
            val snapshot = documentRef.get().await()
            val placesList =
                snapshot.get("places") as? MutableList<HashMap<String, Any>> ?: mutableListOf()

            // Find and remove the matching place from the list
            val placeToRemove =
                placesList.find { it["name"] == place.name && it["category"] == place.category }
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

    fun fetchTripsWithRoleFilter(userId: String) {
        // Use a coroutine for Firestore calls to avoid blocking the main thread
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch all trips at once
                val tripSnapshots = col.get().await()

                val tripList = mutableListOf<Trip>()

                // Loop through each trip and check collaborators
                for (document in tripSnapshots.documents) {
                    val trip = document.toObject(Trip::class.java)
                    val tripId = document.id

                    // Fetch collaborators of this trip and check if the user has access
                    val collaboratorSnapshots = col.document(tripId)
                        .collection("collaborators")
                        .whereEqualTo("userId", userId)
                        .get().await()

                    val collaborator = collaboratorSnapshots.documents.firstOrNull()

                    if (collaborator != null) {
                        val role = collaborator.getString("role")
                        // Check if the role is one that allows access (owner, editor, viewer)
                        if (role == "owner" || role == "editor" || role == "viewer") {
                            trip?.let { tripList.add(it) }
                        }
                    }
                }

                // Update LiveData on the main thread
                withContext(Dispatchers.Main) {
                    trips.value = tripList
                }

            } catch (e: Exception) {
                Log.e("TripViewModel", "Error fetching trips with role filter: $e")
            }
        }
    }

    private fun groupTripsByYear(tripList: List<Trip>) {
        // Define the date format that matches your startDate string
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val tripCountByYear = tripList.groupBy { trip ->
            // Try to parse the startDate to extract the year
            try {
                val date = dateFormat.parse(trip.startDate) // Parse the full date
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault()) // Year format
                date?.let { yearFormat.format(it).toInt() } // Extract and convert the year to an Int
            } catch (e: Exception) {
                Log.e("groupTripsByYear", "Error parsing date: ${trip.startDate}, error: $e")
                null
            }
        }
            .filterKeys { it != null } // Filter out null keys (failed date parsing)
            .mapKeys { it.key!! } // Safely convert to non-nullable Int keys
            .mapValues { (_, trips) -> trips.size } // Count the number of trips for each year

        // Update the LiveData
        _tripCountByYear.value = tripCountByYear
    }
}
