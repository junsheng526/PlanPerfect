package com.example.planperfect.view.planning

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.ActivityAddDestinationBinding
import com.example.planperfect.viewmodel.PlacesViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddDestinationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDestinationBinding
    private lateinit var filteredAdapter: AddPlacesAdapter
    private val filteredList = mutableListOf<TouristPlace>()
    private lateinit var placesViewModel: PlacesViewModel
    private var searchQuery: String = ""
    private lateinit var tripId: String
    private lateinit var dayId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get tripId and dayId from Intent
        tripId = intent.getStringExtra("tripId") ?: return
        dayId = intent.getStringExtra("dayId") ?: return

        // Initialize PlacesViewModel
        placesViewModel = ViewModelProvider(this).get(PlacesViewModel::class.java)

        // Set up RecyclerView
        filteredAdapter = AddPlacesAdapter(filteredList) { place ->

            val intent = Intent(this, AddNewPlacesDetailsActivity::class.java).apply {
                putExtra("place", place)
                putExtra("tripId", tripId)
                putExtra("dayId", dayId)
            }
            startActivity(intent)
        }
        binding.placesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.placesRecyclerView.adapter = filteredAdapter

        // Observe placesLiveData from the ViewModel
        placesViewModel.placesLiveData.observe(this) { places ->
            // Show all places initially
            if (searchQuery.isEmpty()) {
                updatePlaceList(places)
            } else {
                filterList(searchQuery)
            }
        }

        // Set up search bar text change listener
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim()
                if (searchQuery.isEmpty()) {
                    // Show all places if the search query is cleared
                    placesViewModel.placesLiveData.value?.let { updatePlaceList(it) }
                } else {
                    // Show filtered results when typing
                    filterList(searchQuery)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePlaceList(places: List<TouristPlace>) {
        // Clear the current list and add all places
        filteredList.clear()
        filteredList.addAll(places)
        filteredAdapter.notifyDataSetChanged()

        // Show the RecyclerView with all places
        binding.placesRecyclerView.visibility = View.VISIBLE
        binding.noResultsImage.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterList(query: String) {
        // Filter tourist places based on query
        val filteredItems = placesViewModel.placesLiveData.value?.filter { place ->
            place.name.contains(query, ignoreCase = true) || place.category.contains(query, ignoreCase = true)
        } ?: listOf()

        // Update filtered list and notify adapter
        filteredList.clear()
        filteredList.addAll(filteredItems)
        filteredAdapter.notifyDataSetChanged()

        // Show or hide RecyclerView and noResultsImage based on filtered list
        if (filteredItems.isEmpty()) {
            binding.placesRecyclerView.visibility = View.GONE
            binding.noResultsImage.visibility = View.VISIBLE
        } else {
            binding.placesRecyclerView.visibility = View.VISIBLE
            binding.noResultsImage.visibility = View.GONE
        }
    }
}
