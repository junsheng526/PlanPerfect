package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planperfect.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("user")
    private val users = MutableLiveData<List<User>>()
    private lateinit var auth: FirebaseAuth

    init {
        col.addSnapshotListener { snap, _ -> users.value = snap?.toObjects() }
    }

    suspend fun get(id: String): User? {
        return col.document(id).get().await().toObject<User>()
    }

    suspend fun getCurrencyCodeByUserId(userId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch the user document based on the user ID
                val userDocument = col.document(userId).get().await()

                // Check if the document exists
                if (userDocument.exists()) {
                    // Retrieve the currency code from the document
                    val currencyCode = userDocument.getString("currencyCode") // Adjust this key based on your Firestore structure
                    return@withContext currencyCode
                } else {
                    Log.e("Firestore", "User document does not exist for ID: $userId")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching currency code: $e")
                null
            }
        }
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

    fun validate(user: User): String {
        var e = ""

        e += if (user.name == "") "- Name is required.\n"
        else if (user.name.length < 3) "- Name is too short (at least 3 letters).\n"
        else ""

        e += if (user.phoneNumber == "") "- Phone Number is required.\n"
        else ""

        e += if (user.country == "") "- Country is required.\n"
        else ""

        return e
    }

    suspend fun update(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                auth = Firebase.auth
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val documentRef = col.document(currentUser.uid)
                    val updates = mutableMapOf<String, Any?>()

                    // Update specific fields of the user document
                    if (user.name.isNotEmpty()) {
                        updates["name"] = user.name
                    }
                    if (user.country.isNotEmpty()) {
                        updates["country"] = user.country
                    }
                    if (user.email.isNotEmpty()) {
                        updates["email"] = user.email
                    }
                    if (user.phoneNumber.isNotEmpty()) {
                        updates["phoneNumber"] = user.phoneNumber
                    }
                    // Update photo if available
                    if (user.photo != null) {
                        updates["photo"] = user.photo
                    }

                    // Perform the update with only specified fields
                    documentRef.update(updates)
                        .addOnSuccessListener {
                            Log.i("FireStore", "User fields updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FireStore", "Error updating user fields: $e")
                        }
                        .await()
                }
                true
            } catch (e: Exception) {
                Log.e("FireStore", "Firestore operation failed: $e")
                false
            }
        }
    }
}