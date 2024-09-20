package com.example.planperfect.view.planning

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.R
import com.example.planperfect.data.model.Trip
import com.example.planperfect.databinding.ActivityTripInformationBinding
import com.example.planperfect.databinding.ActivityTripLocationBinding
import com.example.planperfect.viewmodel.TripViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.util.UUID

class TripLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripLocationBinding
    private val tripViewModel: TripViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        auth = Firebase.auth

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
                        Toast.makeText(this@TripLocationActivity, "Trip created successfully!", Toast.LENGTH_SHORT).show()
                        // Navigate or perform any additional action
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

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(binding.toolbar)
        toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}