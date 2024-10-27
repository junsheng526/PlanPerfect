package com.example.planperfect.view.planning

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.R
import com.example.planperfect.data.model.Trip
import com.example.planperfect.databinding.ActivityTripInformationBinding
import com.example.planperfect.databinding.ActivityTripLocationBinding
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.TripViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TripLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripLocationBinding
    private val tripViewModel: TripViewModel by viewModels()
    private lateinit var authViewModel: AuthViewModel
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        auth = Firebase.auth
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        val startDate = intent.getStringExtra("startDate") ?: "N/A"
        val endDate = intent.getStringExtra("endDate") ?: "N/A"
        val tripName = intent.getStringExtra("tripName") ?: "N/A"

        binding.nextBtn.setOnClickListener {
            // Get the current Firebase user
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid

                // Get user inputs from EditText fields
                val homeCity = binding.editHomeCity.text.toString().trim()
                val destination = binding.editCountryToVisit.text.toString().trim()

                // Validate the input
                if (destination.isEmpty()) {
                    Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val trip = Trip(
                    id = UUID.randomUUID().toString(),
                    name = tripName,
                    homeCity = homeCity,
                    destination = destination,
                    startDate = startDate,
                    endDate = endDate,
                    userId = userId,
                    // default image
                    imageUrl = "https://www.visa.com.sg/dam/VCOM/regional/ap/images/travel-with-visa/paris/marquee-travel-paris-800x450.jpg"
                )

                lifecycleScope.launch {
                    val success: Boolean = tripViewModel.set(trip)
                    if (success) {
                        // Initialize the itinerary sub-collection
                        initializeItinerary(trip.id, startDate, endDate)
                        authViewModel.getCurrentUserId()
                            ?.let { it1 -> initializeCollaborator(trip.id, it1) }

                        Toast.makeText(this@TripLocationActivity, "Trip created successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@TripLocationActivity, TripDetailsActivity::class.java)
                        intent.putExtra("tripId", trip.id)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@TripLocationActivity, "Failed to create trip!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Handle the case where the user is not logged in
                Toast.makeText(this, "You are not logged in!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun initializeItinerary(tripId: String, startDate: String, endDate: String) {
        val itineraryCollection = Firebase.firestore.collection("trip")
            .document(tripId)
            .collection("itineraries")

        val duration = calculateTripDuration(startDate, endDate)

        // You can add a document for each day of the trip, for example 7 days.
        val defaultItineraries = (1..duration).map { day ->
            hashMapOf(
                "dayId" to "$tripId-Day-$day",  // Custom day ID format
                "places" to mutableListOf<String>() // Start with an empty list for places
            )
        }

        // Add each day's itinerary to Firestore with custom dayId
        for ((index, itinerary) in defaultItineraries.withIndex()) {
            val customDayId = "$tripId-Day-${index + 1}"  // Custom document ID
            itineraryCollection.document(customDayId).set(itinerary)
                .addOnSuccessListener {
                    Log.d("INITIALIZE_ITINERARY", "Successfully added day $customDayId")
                }
                .addOnFailureListener { e ->
                    Log.e("INITIALIZE_ITINERARY", "Error adding day $customDayId: ${e.message}")
                }
        }
    }

    private fun calculateTripDuration(startDate: String, endDate: String): Int {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val start = format.parse(startDate)
            val end = format.parse(endDate)
            if (start != null && end != null) {
                val diffInMillis = end.time - start.time
                (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1 // +1 to include the start day
            } else {
                0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private suspend fun initializeCollaborator(tripId: String, userId: String) {
        val col = Firebase.firestore.collection("trip")
            .document(tripId)
            .collection("collaborators")

        // Add the user as a collaborator with the role of "owner"
        val ownerData = hashMapOf(
            "userId" to userId,
            "role" to "owner" // Set the role as "owner"
        )

        col.document(userId).set(ownerData)
            .addOnSuccessListener {
                Log.d("INITIALIZE_COLLABORATOR", "Successfully added owner with userId: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("INITIALIZE_COLLABORATOR", "Error adding owner: ${e.message}")
            }
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(binding.toolbar)
        toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}