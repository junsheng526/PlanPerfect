package com.example.planperfect.view.planning

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.data.model.Itinerary
import com.example.planperfect.data.model.Trip
import com.example.planperfect.databinding.FragmentPlanningBinding
import com.example.planperfect.viewmodel.PlanningViewModel
import com.example.planperfect.viewmodel.TripViewModel

class PlanningFragment : Fragment() {
    private lateinit var binding: FragmentPlanningBinding
    private lateinit var itineraryAdapter: ItineraryAdapter
    private val itineraryList = mutableListOf<Trip>()
    private lateinit var tripViewModel: TripViewModel
    private var searchQuery: String = "" // Track the current search query

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlanningBinding.inflate(inflater, container, false)

        // Set up RecyclerView with ItineraryAdapter (Vertical List)
        itineraryAdapter = ItineraryAdapter(itineraryList)
        binding.itineraryRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.itineraryRecyclerView.adapter = itineraryAdapter

        // Initialize PlanningViewModel
        tripViewModel = ViewModelProvider(this).get(TripViewModel::class.java)

        // Observe the itineraryLiveData and update UI when data changes
        tripViewModel.trips.observe(viewLifecycleOwner) { trips ->
            // Only update the list if there's an active search query
            if (searchQuery.isNotEmpty()) {
                filterList(searchQuery)
            } else {
                // Show all itineraries initially
                itineraryList.clear()
                itineraryList.addAll(trips)
                itineraryAdapter.notifyDataSetChanged()
            }
        }

        // Handle search bar text change
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim() // Update the current query
                if (searchQuery.isEmpty()) {
                    // Show all itineraries initially
                    itineraryList.clear()
                    itineraryList.addAll(tripViewModel.trips.value ?: emptyList())
                    itineraryAdapter.notifyDataSetChanged()
                } else {
                    // Filter itineraries
                    filterList(searchQuery)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Handle filter button click
        binding.filterButton.setOnClickListener {
            // Implement filter logic or show filter options
        }

        binding.fabAddItinerary.setOnClickListener {
            val intent = Intent(activity, TripInformationActivity::class.java)
            startActivity(intent) // Start the activity
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterList(query: String) {
        // Filter itineraries based on query
        val filteredItems = tripViewModel.trips.value?.filter { place ->
            place.name.contains(query, ignoreCase = true) ||
                    place.homeCity.contains(query, ignoreCase = true) ||
                    place.destination.contains(query, ignoreCase = true)
        } ?: listOf()

        // Update filtered list and notify adapter
        itineraryList.clear()
        itineraryList.addAll(filteredItems)
        itineraryAdapter.notifyDataSetChanged()
    }
}