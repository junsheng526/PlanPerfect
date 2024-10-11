package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.data.model.Trip
import com.example.planperfect.data.model.TripStatistics
import com.google.firebase.auth.FirebaseAuth
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

    private val _tripStatistics = MutableLiveData<TripStatistics>()
    val tripStatistics: LiveData<TripStatistics> get() = _tripStatistics

    val userId: String? = FirebaseAuth.getInstance().currentUser?.uid

    init {
        // Listen for real-time updates to the trips collection
        col.addSnapshotListener { snap, error ->
            if (error != null) {
                Log.e("Firestore", "Listen failed: $error")
                return@addSnapshotListener
            }

            val tripList = snap?.toObjects<Trip>() ?: emptyList()
            trips.value = tripList

            // Launch a coroutine to handle the groupTripsByYear since it's a suspending function
            viewModelScope.launch(Dispatchers.IO) {
                userId?.let { // Ensure userId is not null
                    groupTripsByYear(tripList, it)
                }
            }
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
                        val status = collaborator.getString("status")

                        val validRole = listOf("editor", "viewer")
                        // Check if the role is one that allows access (owner, editor, viewer)
                        if ((role in validRole && status == "accept") || role == "owner") {
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

    private suspend fun groupTripsByYear(tripList: List<Trip>, userId: String) {
        // Define the date format that matches your startDate string
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Use a map to store the number of trips for each year
        val tripCountByYear = mutableMapOf<Int, Int>()

        // Loop through each trip in the list
        for (trip in tripList) {
            val tripId = trip.id

            Log.d("groupTripsByYear", "Processing trip with ID: $tripId")

            try {
                // Fetch collaborators for the current trip
                val collaboratorSnapshot = col.document(tripId)
                    .collection("collaborators")
                    .get()
                    .await()

                // Log the size of the collaborator snapshot for debugging
                Log.d("groupTripsByYear", "Collaborators fetched: ${collaboratorSnapshot.size()}")

                // Check if the current user is a collaborator
                val userIsCollaborator = collaboratorSnapshot.documents.any { doc ->
                    val collaboratorUserId = doc.getString("userId")
                    val status = doc.getString("status")
                    Log.d("groupTripsByYear", "Collaborator user ID: $collaboratorUserId, Status: $status")
                    collaboratorUserId == userId && status != "pending" && status != "reject"
                }

                // Log whether the user is a collaborator
                Log.d("groupTripsByYear", "User is collaborator: $userIsCollaborator")

                // If the user is a collaborator, get the year from the trip's start date
                if (userIsCollaborator) {
                    val date = dateFormat.parse(trip.startDate) // Parse the full date
                    Log.d("groupTripsByYear", "Trip start date: ${trip.startDate}")

                    date?.let {
                        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault()) // Year format
                        val year = yearFormat.format(it).toInt()

                        // Log the year extracted from the trip's start date
                        Log.d("groupTripsByYear", "Trip year: $year")

                        // Increment the count for the year
                        val newCount = tripCountByYear.getOrDefault(year, 0) + 1
                        tripCountByYear[year] = newCount

                        // Log the updated count for this year
                        Log.d("groupTripsByYear", "Updated trip count for year $year: $newCount")
                    }
                }
            } catch (e: Exception) {
                Log.e("groupTripsByYear", "Error processing trip ID: $tripId, error: $e")
            }
        }

        // Log the final trip count by year map before posting to LiveData
        Log.d("groupTripsByYear", "Final trip count by year: $tripCountByYear")

        // Update the LiveData on the main thread
        withContext(Dispatchers.Main) {
            _tripCountByYear.value = tripCountByYear
        }
    }

    suspend fun getUserTripYears(userId: String): List<String> {
        val yearsSet = mutableSetOf<String>() // To store unique years

        // Ensure trips are available
        trips.value?.forEach { trip ->
            val tripId = trip.id // Assuming trip has a unique ID
            try {
                // Fetch collaborators for the current trip
                val collaboratorSnapshot =
                    col.document(tripId).collection("collaborators").get().await()

                // Check if the current user is a collaborator
                val userIsCollaborator = collaboratorSnapshot.documents.any { doc ->
                    val collaboratorUserId = doc.getString("userId")
                    Log.d("collaboratorSnapshot", "User ID: $collaboratorUserId")
                    collaboratorUserId == userId // Check for current user
                }

                // If user is a collaborator, get the trip's start date
                if (userIsCollaborator) {
                    // Assuming the startDate is in the format "dd/MM/yyyy"
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = dateFormat.parse(trip.startDate)
                    val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

                    date?.let {
                        yearsSet.add(yearFormat.format(it)) // Add the year to the set
                    }
                }
            } catch (e: Exception) {
                Log.e("getUserTripYears", "Error processing trip ID: $tripId, error: $e")
            }
        }

        return yearsSet.toList() // Convert the set to a list and return it
    }

    private suspend fun fetchPlacesCountForTrip(tripId: String): Int {
        var placesCount = 0

        try {
            // Get all itineraries for this trip
            val itinerariesSnapshot = col.document(tripId).collection("itineraries").get().await()

            for (day in itinerariesSnapshot.documents) {
                // Each day document contains an array of places
                val places = day.get("places") as? List<Trip> ?: emptyList()
                placesCount += places.size
            }
        } catch (e: Exception) {
            Log.e("TripViewModel", "Error fetching places for trip ID: $tripId, error: $e")
        }

        return placesCount
    }

    // New method to calculate statistics asynchronously
    fun calculateStatisticsForYear(year: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tripsForYear = trips.value?.filter { trip ->
                val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                try {
                    val startDate =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(trip.startDate)
                    val yearOfTrip = dateFormat.format(startDate!!)
                    yearOfTrip == year
                } catch (e: Exception) {
                    false
                }
            } ?: emptyList()

            val totalTrips = tripsForYear.size
            val totalTravelDays = tripsForYear.sumBy { trip ->
                calculateTravelDays(trip.startDate, trip.endDate)
            }

            // Calculate total places visited asynchronously
            var totalPlacesVisited = 0
            for (trip in tripsForYear) {
                totalPlacesVisited += fetchPlacesCountForTrip(trip.id)
            }

            // Post results to LiveData
            withContext(Dispatchers.Main) {
                _tripStatistics.value =
                    TripStatistics(totalTrips, totalTravelDays, totalPlacesVisited)
            }
        }
    }

    private fun calculateTravelDays(startDate: String, endDate: String): Int {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val start = dateFormat.parse(startDate) ?: return 0
            val end = dateFormat.parse(endDate) ?: return 0
            // Calculate the difference in days
            val diff = end.time - start.time
            (diff / (1000 * 60 * 60 * 24)).toInt() + 1 // Add 1 to include the start day
        } catch (e: Exception) {
            Log.e("calculateTravelDays", "Error parsing dates: $e")
            0 // Return 0 in case of error
        }
    }
}
