package com.example.planperfect.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planperfect.data.model.Itinerary
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PlanningViewModel : ViewModel() {
    private val itineraryCollection = Firebase.firestore.collection("itinerary")
    val itineraryLiveData = MutableLiveData<List<Itinerary>>() // Change to your model class if needed

    init {
        // Fetch itineraries in real-time
        itineraryCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("Firestore", "Error fetching itineraries: $error")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val itineraries = snapshot.toObjects<Itinerary>()
                itineraryLiveData.value = itineraries
            }
        }
    }

    // Fetch itineraries manually (not real-time)
    suspend fun fetchItineraries(): List<Itinerary> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = itineraryCollection.get().await()
                snapshot.toObjects<Itinerary>() // Ensure TouristPlace matches your Firestore data structure
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching itineraries: $e")
                emptyList()
            }
        }
    }

    // Optionally, if you need to fetch a specific itinerary by ID
    suspend fun getItineraryById(id: String): Itinerary? {
        return try {
            val document = itineraryCollection.document(id).get().await()
            document.toObject<Itinerary>() // Ensure TouristPlace matches your Firestore data structure
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching itinerary by ID: $e")
            null
        }
    }
}
