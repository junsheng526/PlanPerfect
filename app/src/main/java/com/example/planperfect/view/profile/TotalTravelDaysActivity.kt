package com.example.planperfect.view.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.data.model.Trip
import com.example.planperfect.databinding.ActivityTotalTravelDaysBinding
import com.example.planperfect.view.planning.TripDetailsActivity

class TotalTravelDaysActivity : AppCompatActivity(), TripsAdapter.OnTripClickListener {

    private lateinit var binding: ActivityTotalTravelDaysBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTotalTravelDaysBinding.inflate(layoutInflater)
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
        val adapter = TripsAdapter(tripList, isTravelDays = true, onTripClickListener = this)
        recyclerView.adapter = adapter
    }

    override fun onTripClick(trip: Trip) {
        val intent = Intent(this, TripDetailsActivity::class.java)
        intent.putExtra("tripId", trip.id)
        startActivity(intent)
    }
}