package com.example.planperfect.view.recommendation

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.data.model.Recommendation
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.ActivityRecommendationBinding
import com.example.planperfect.view.planning.adapter.AddPlacesAdapter
import com.example.planperfect.viewmodel.PlacesViewModel
import kotlinx.coroutines.launch

class RecommendationActivity : AppCompatActivity() {
    private val placesViewModel: PlacesViewModel by viewModels()
    private lateinit var binding: ActivityRecommendationBinding
    private lateinit var placesAdapter: AddPlacesAdapter
    private val placesList = mutableListOf<TouristPlace>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Initialize the RecyclerView and adapter
        placesAdapter = AddPlacesAdapter(placesList, ({ place ->
            // Handle item click
            Log.d("RecommendationActivity", "Clicked on: ${place.name}")
            // You can start another activity or show details as needed
        }), false)

        binding.placesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.placesRecyclerView.adapter = placesAdapter

        // Get the recommendations from the intent
        val recommendations = intent.getParcelableArrayListExtra<Recommendation>("recommendations")

        recommendations?.let {
            for (recommendation in it) {
                Log.d(
                    "RecommendationActivity",
                    "Title: ${recommendation.title}, Description: ${recommendation.description}, Image URL: ${recommendation.image_url}"
                )

                // Fetch places by name from the ViewModel
                fetchPlacesByName(recommendation.title)  // Assuming the title is used as the place name
            }
        } ?: run {
            Log.e("RecommendationActivity", "No recommendations found")
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

    private fun fetchPlacesByName(name: String) {
        // Launch a coroutine to fetch places by name
        placesViewModel.viewModelScope.launch {
            val places = placesViewModel.getPlacesByName(name)

            if (places.isNotEmpty()) {
                // Instead of clearing the list, just add to it
                placesList.addAll(places) // Append new places
                placesAdapter.notifyDataSetChanged()
            } else {
                Log.e("RecommendationActivity", "No places found for the name: $name")
            }
        }
    }
}