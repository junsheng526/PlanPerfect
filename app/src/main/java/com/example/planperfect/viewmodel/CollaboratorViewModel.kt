package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planperfect.data.model.Collaborator
import com.example.planperfect.data.model.CollaboratorWithUserDetails
import com.example.planperfect.data.model.Trip
import com.example.planperfect.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CollaboratorViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val col = FirebaseFirestore.getInstance().collection("trip")
    val collaboratorsWithUserDetailsLiveData = MutableLiveData<List<Pair<User, String>>>()
    private val _collaboratorAdditionStatus = MutableLiveData<Boolean>()
    val collaboratorAdditionStatus: MutableLiveData<Boolean> get() = _collaboratorAdditionStatus
    val pendingInvitationsLiveData = MutableLiveData<List<Pair<Trip, CollaboratorWithUserDetails>>>()

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
                            val role = doc.getString("role") ?: "viewer"
                            val user = authViewModel.get(userId) // Fetch the user by userId
                            if (user != null) {
                                Pair(user, role) // Pair user details with role
                            } else {
                                null
                            }
                        }
                        withContext(Dispatchers.Main) {
                            collaboratorsWithUserDetailsLiveData.value = collaboratorsWithDetails
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
                val document = col.document(tripId).collection("collaborators").document(userId).get().await()
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
                val pendingInvitations = mutableListOf<Pair<Trip, CollaboratorWithUserDetails>>() // Updated to include user details

                // Iterate over each trip document
                for (tripDoc in tripSnapshot.documents) {
                    val tripId = tripDoc.id

                    // Fetch all collaborators for the trip in a single query
                    val collaboratorSnapshot = col.document(tripId).collection("collaborators").get().await()

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
                        val user = authViewModel.get(owner.userId) // Assuming this returns user details

                        val collaboratorWithUserDetails = CollaboratorWithUserDetails(owner, user) // Custom data class to hold both collaborator and user info

                        pendingInvitations.add(Pair(tripDoc.toObject(Trip::class.java)!!, collaboratorWithUserDetails))  // Add trip and collaborator with user details as a pair
                    }
                }

                // Post the result to LiveData in the Main thread
                withContext(Dispatchers.Main) {
                    pendingInvitationsLiveData.value = pendingInvitations
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("CollaboratorViewModel", "Failed to fetch pending invitations: ${exception.message}")
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
                            Log.i("CollaboratorViewModel", "Collaboration status updated to $newStatus")
                        }
                        .addOnFailureListener {
                            Log.e("CollaboratorViewModel", "Error updating status", it)
                        }
                }
            }
    }
}
