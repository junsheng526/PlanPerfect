package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planperfect.data.model.UserRating
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ReviewViewModel : ViewModel() {
    private val reviewsCollection = FirebaseFirestore.getInstance().collection("reviews")
    val reviewsLiveData = MutableLiveData<List<UserRating>>()

    // Fetch reviews for a specific place by place ID
    fun fetchReviewsByPlaceId(placeId: String) {
        reviewsCollection.whereEqualTo("placeId", placeId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val reviews = snapshot.toObjects<UserRating>()
                    reviewsLiveData.value = reviews
                } else {
                    reviewsLiveData.value = emptyList()
                }
            }
    }

    // Add a new review
    fun addReview(userRating: UserRating, onReviewAdded: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                reviewsCollection.add(userRating).await()
                onReviewAdded(true)
            } catch (e: Exception) {
                Log.e("Firestore", "Error adding review: $e")
                onReviewAdded(false)
            }
        }
    }

    // Optionally fetch all reviews if needed
    suspend fun fetchAllReviews(): List<UserRating> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = reviewsCollection.get().await()
                snapshot.toObjects<UserRating>()
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching all reviews: $e")
                emptyList()
            }
        }
    }

    fun getAverageRating(reviews: List<UserRating>): Float {
        return if (reviews.isNotEmpty()) {
            reviews.map { it.rating }.average().toFloat()
        } else {
            0f
        }
    }

    fun getReviewCount(reviews: List<UserRating>): Int {
        return reviews.size
    }
}