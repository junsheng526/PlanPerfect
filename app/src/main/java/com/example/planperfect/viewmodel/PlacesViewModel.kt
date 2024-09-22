package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.utils.DummyDataUtil
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
class PlacesViewModel : ViewModel() {
    private val placesCollection = Firebase.firestore.collection("tourist_places")
    val placesLiveData = MutableLiveData<List<TouristPlace>>()

    init {
        // Fetch places in real-time
        placesCollection.addSnapshotListener { snap, error ->
            if (error != null) {
                Log.e("Firestore", "Error fetching places: $error")
                return@addSnapshotListener
            }
            if (snap != null) {
                val places = snap.toObjects<TouristPlace>()
                placesLiveData.value = places
            }
        }
    }

    // Fetch specific place by ID (optional)
    suspend fun getPlaceById(id: String): TouristPlace? {
        return try {
            val document = placesCollection.document(id).get().await()
            document.toObject<TouristPlace>()
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching place by ID: $e")
            null
        }
    }

    // Manually fetch places if needed (not real-time)
    suspend fun fetchPlaces(): List<TouristPlace> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = placesCollection.get().await()
                snapshot.toObjects<TouristPlace>()
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching places: $e")
                emptyList()
            }
        }
    }
}