package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planperfect.data.model.Collaborator
import com.example.planperfect.data.model.CollaboratorWithUserDetails
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.data.model.Trip
import com.example.planperfect.data.model.TripStatistics
import com.example.planperfect.data.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class CollaboratorViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val col = FirebaseFirestore.getInstance().collection("trip")
    val collaboratorsWithUserDetailsLiveData = MutableLiveData<List<Pair<User, String>>>()
    private val _collaboratorAdditionStatus = MutableLiveData<Boolean>()
    val collaboratorAdditionStatus: MutableLiveData<Boolean> get() = _collaboratorAdditionStatus
    val pendingInvitationsLiveData =
        MutableLiveData<List<Pair<Trip, CollaboratorWithUserDetails>>>()


    private val _collaboratorStatisticsLiveData = MutableLiveData<Map<String, Int>>()
    val collaboratorStatisticsLiveData: LiveData<Map<String, Int>> get() = _collaboratorStatisticsLiveData

    private val _calculatedTripsLiveData = MutableLiveData<List<Trip>>()
    val calculatedTripsLiveData: LiveData<List<Trip>> get() = _calculatedTripsLiveData

    private val _tripStatistics = MutableLiveData<TripStatistics>()
    val tripStatistics: LiveData<TripStatistics> get() = _tripStatistics

    private val _visitedPlacesLiveData = MutableLiveData<List<TouristPlace>>()
    val visitedPlacesLiveData: LiveData<List<TouristPlace>> get() = _visitedPlacesLiveData

    // Fetch collaborators along with user details in real-time
    fun getCollaboratorsWithUserDetails(tripId: String) {
        col.document(tripId).collection("collaborators")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error fetching collaborators: $error")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val collaboratorsWithDetails = snapshot.documents.mapNotNull { doc ->
                            val userId = doc.getString("userId") ?: return@mapNotNull null
                            val role = doc.getString("role")
                            val status = doc.getString("status")
                            val user = authViewModel.get(userId)
                            if (user != null && role != null) {
                                when {
                                    role == "owner" -> Pair(user, role)
                                    (role == "editor" || role == "viewer") && status == "accept" -> Pair(user, role)
                                    else -> null
                                }
                            } else {
                                null
                            }
                        }

                        val sortedCollaboratorsWithDetails = collaboratorsWithDetails.sortedBy {
                            when (it.second) {
                                "owner" -> 1
                                "editor" -> 2
                                "viewer" -> 3
                                else -> 4
                            }
                        }

                        withContext(Dispatchers.Main) {
                            collaboratorsWithUserDetailsLiveData.value = sortedCollaboratorsWithDetails
                        }
                    }
                }
            }
    }

    // Function to add a collaborator
    fun addCollaborator(email: String, role: String, tripId: String) {
        viewModelScope.launch {
            val user = authViewModel.getUserByEmail(email)

            if (user != null) {
                try {
                    // Create a new Collaborator object
                    val collaborator = Collaborator(user.id, role, "pending")

                    // Add the collaborator to Firestore
                    col.document(tripId).collection("collaborators")
                        .document(user.id) // Use user ID as the document ID
                        .set(collaborator)
                        .await() // Wait for the operation to complete

                    _collaboratorAdditionStatus.postValue(true) // Success
                } catch (e: Exception) {
                    Log.e("CollaboratorViewModel", "Failed to add collaborator: $e")
                    _collaboratorAdditionStatus.postValue(false) // Failure
                }
            } else {
                _collaboratorAdditionStatus.postValue(false) // User not found
            }
        }
    }

    suspend fun getUserRole(userId: String, tripId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val document =
                    col.document(tripId).collection("collaborators").document(userId).get().await()
                if (document.exists()) {
                    document.getString("role")
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("CollaboratorViewModel", "Failed to get user role: $e")
                null // Return null on failure
            }
        }
    }

    fun getPendingInvitations(userId: String) {
        Log.d("getPendingInvitations", "Fetching Invitation for userId: $userId")

        // Get all trips
        col.get().addOnSuccessListener { tripSnapshot ->
            viewModelScope.launch(Dispatchers.IO) {
                val pendingInvitations =
                    mutableListOf<Pair<Trip, CollaboratorWithUserDetails>>() // Updated to include user details

                // Iterate over each trip document
                for (tripDoc in tripSnapshot.documents) {
                    val tripId = tripDoc.id

                    // Fetch all collaborators for the trip in a single query
                    val collaboratorSnapshot =
                        col.document(tripId).collection("collaborators").get().await()

                    val owner = collaboratorSnapshot.documents.firstOrNull {
                        it.getString("role") == "owner"
                    }?.toObject(Collaborator::class.java)

                    // Fetch the collaborator that matches the provided userId and has a status of 'pending'
                    val pendingCollaborator = collaboratorSnapshot.documents.firstOrNull {
                        it.getString("userId") == userId && it.getString("status") == "pending"
                    }?.toObject(Collaborator::class.java)

                    // If both the trip and the owner are found
                    if (tripDoc != null && owner != null && pendingCollaborator != null) {
                        // Fetch user details using authViewModel for the owner
                        val user =
                            authViewModel.get(owner.userId) // Assuming this returns user details

                        val collaboratorWithUserDetails = CollaboratorWithUserDetails(
                            owner,
                            user
                        ) // Custom data class to hold both collaborator and user info

                        pendingInvitations.add(
                            Pair(
                                tripDoc.toObject(Trip::class.java)!!,
                                collaboratorWithUserDetails
                            )
                        )  // Add trip and collaborator with user details as a pair
                    }
                }

                // Post the result to LiveData in the Main thread
                withContext(Dispatchers.Main) {
                    pendingInvitationsLiveData.value = pendingInvitations
                }
            }
        }.addOnFailureListener { exception ->
            Log.e(
                "CollaboratorViewModel",
                "Failed to fetch pending invitations: ${exception.message}"
            )
        }
    }

    fun updateCollaborationStatus(tripId: String, userId: String, newStatus: String) {
        col.document(tripId).collection("collaborators")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val documentId = snapshot.documents[0].id
                    col.document(tripId).collection("collaborators")
                        .document(documentId)
                        .update("status", newStatus)
                        .addOnSuccessListener {
                            Log.i(
                                "CollaboratorViewModel",
                                "Collaboration status updated to $newStatus"
                            )
                        }
                        .addOnFailureListener {
                            Log.e("CollaboratorViewModel", "Error updating status", it)
                        }
                }
            }
    }

    fun calculateStatisticsForYear(year: String, userId: String, tripList: List<Trip>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("TripStatistics", "Calculating statistics for year: $year, userId: $userId")

                var totalTrips = 0
                var totalTravelDays = 0
                var totalPlacesVisited = 0

                // List to store trips that meet the criteria
                val tripsInStatistics = mutableListOf<Trip>()
                val uniquePlacesVisited = mutableSetOf<TouristPlace>()

                // Iterate through all trips
                for (trip in tripList) {
                    val tripId = trip.id
                    Log.d("TripStatistics", "Processing trip with ID: $tripId")

                    // Fetch the collaborators for this trip to check if the user is a collaborator
                    val acceptedCollaboratorsSnapshot = col.document(tripId).collection("collaborators")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("status", "accept")
                        .get().await()

                    // Query for trip owners
                    val ownerCollaboratorsSnapshot = col.document(tripId).collection("collaborators")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("role", "owner")
                        .get().await()

                    // Check if user exists in collaborators
                    if (acceptedCollaboratorsSnapshot.isEmpty && ownerCollaboratorsSnapshot.isEmpty) {
                        Log.d("TripStatistics", "User $userId is neither an accepted collaborator nor the owner for trip $tripId")
                        continue
                    }

                    // Check if the trip is in the specified year
                    if (isTripInYear(trip, year)) {
                        totalTrips++
                        tripsInStatistics.add(trip) // Add the trip to the list

                        Log.d("TripStatistics", "Trip $tripId is in year $year. Total trips so far: $totalTrips")

                        // Fetch the itineraries for the trip
                        val itinerarySnapshot = col.document(tripId).collection("itineraries").get().await()
                        Log.d("TripStatistics", "Fetched ${itinerarySnapshot.size()} itineraries for trip ID: $tripId")

                        // Count travel days and places only if the user is a collaborator or owner
                        for (itineraryDoc in itinerarySnapshot.documents) {

                            totalTravelDays++
                            val places = itineraryDoc.get("places") as? List<Map<String, Any?>> ?: emptyList()
                            val placesCount = places?.size ?: 0
                            totalPlacesVisited += placesCount
                            Log.d("TripStatistics", "Raw places data: $places")
                            if (!places.isNullOrEmpty()) {
                                for (placeData in places) {
                                    val touristPlace = TouristPlace(
                                        id = placeData["id"] as? String ?: "",
                                        name = placeData["name"] as? String ?: "",
                                        description = placeData["description"] as? String ?: "",
                                        imageUrls = placeData["imageUrls"] as? List<String> ?: emptyList(),
                                        category = placeData["category"] as? String ?: "",
                                        startTime = placeData["startTime"] as? String,
                                        endTime = placeData["endTime"] as? String,
                                        notes = placeData["notes"] as? String,
                                        latitude = placeData["latitude"] as? Double,
                                        longitude = placeData["longitude"] as? Double,
                                        longDescription = placeData["longDescription"] as? String ?: "",
                                        isFavorite = placeData["isFavorite"] as? Boolean ?: false,
                                        currencyCode = placeData["currencyCode"] as? String
                                    )
                                    if (!uniquePlacesVisited.any { it.name == touristPlace.name }) {
                                        uniquePlacesVisited.add(touristPlace)
                                    }
                                }
                            }
                            Log.d("TripStatistics", "Trip $tripId, Itinerary: ${itineraryDoc.id}, Places visited: $placesCount")
                        }
                    } else {
                        Log.d("TripStatistics", "Trip $tripId is not in year $year")
                    }
                }

                // Log the final statistics
                Log.d("TripStatistics", "Final statistics for year $year -> Total trips: $totalTrips, Total travel days: $totalTravelDays, Total places visited: $totalPlacesVisited")

                // Update LiveData with statistics
                val statistics = TripStatistics(totalTrips, totalTravelDays, totalPlacesVisited)
                _tripStatistics.postValue(statistics)

                // Post the list of trips used in the statistics
                _calculatedTripsLiveData.postValue(tripsInStatistics)

                _visitedPlacesLiveData.postValue(uniquePlacesVisited.toList())

            } catch (e: Exception) {
                Log.e("TripStatistics", "Failed to calculate statistics: $e")
            }
        }
    }

    // Helper function to determine if a trip falls within a specific year
    private fun isTripInYear(trip: Trip, year: String): Boolean {
        // Get the start date as a string
        val startDate = trip.startDate // Ensure this field is present

        // Check if startDate is in the expected format
        return if (startDate != null && startDate.length == 10) {
            // Extract the year from the start date
            val tripYear = startDate.substring(6, 10) // Get the substring for the year (characters 6 to 9)
            year == "All" || tripYear == year // Check against the specified year
        } else {
            false // Return false if the date format is incorrect
        }
    }
}
