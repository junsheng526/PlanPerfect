package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planperfect.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("user")
    private val users = MutableLiveData<List<User>>()

    init {
        col.addSnapshotListener { snap, _ -> users.value = snap?.toObjects() }
    }

    suspend fun get(id: String): User? {
        return col.document(id).get().await().toObject<User>()
    }

    suspend fun getUserByEmail(email: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = col.whereEqualTo("email", email).limit(1).get().await()

                val document = querySnapshot.documents.firstOrNull()
                if (document != null && document.exists()) {
                    val user = document.toObject<User>()
                    user?.id = document.id
                    user
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("FireStore", "Error fetching user by email: $e")
                null
            }
        }
    }

    suspend fun set(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                col.document(user.id).set(user)
                    .addOnSuccessListener {
                        Log.i("FireStore", "User saved successfully: $user")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FireStore", "Error saving user: $e")
                    }
                    .await()
                true
            } catch (e: Exception) {
                Log.e("FireStore", "FireStore operation failed: $e")
                false
            }
        }
    }

    fun getCurrentUserId(): String? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser?.uid
    }
}