package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planperfect.data.model.Collaborator
import com.example.planperfect.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CollaboratorViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    val collaboratorsWithUserDetailsLiveData = MutableLiveData<List<Pair<User, String>>>()
    private val _collaboratorAdditionStatus = MutableLiveData<Boolean>()
    val collaboratorAdditionStatus: MutableLiveData<Boolean> get() = _collaboratorAdditionStatus

    // Fetch collaborators along with user details in real-time
    fun getCollaboratorsWithUserDetails(tripId: String) {
        db.collection("trip").document(tripId).collection("collaborators")
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
                    db.collection("trip").document(tripId).collection("collaborators")
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
}
