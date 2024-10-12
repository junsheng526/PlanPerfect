package com.example.planperfect.view.profile

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.R
import com.example.planperfect.data.model.Trip
import com.example.planperfect.databinding.ActivityTotalTravelDaysBinding
import com.example.planperfect.databinding.ActivityTotalTravelTripsBinding

class TotalTravelTripsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTotalTravelTripsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTotalTravelTripsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        // Retrieve the trip list from the intent
        val tripList = intent.getParcelableArrayListExtra<Trip>("tripList")

        if (tripList != null) {
            // Use the trip list to display the trips in the RecyclerView
            displayTrips(tripList)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }
    }

    private fun displayTrips(tripList: List<Trip>) {
        // Initialize the RecyclerView
        val recyclerView = binding.itineraryRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this) // Set layout manager

        // Create and set the adapter
        val adapter = TripsAdapter(tripList, false)
        recyclerView.adapter = adapter
    }
}