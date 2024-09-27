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
import com.google.firebase.auth.FirebaseAuth

class PlanningFragment : Fragment() {
    private lateinit var binding: FragmentPlanningBinding
    private lateinit var itineraryAdapter: ItineraryAdapter
    private val itineraryList = mutableListOf<Trip>()
    private lateinit var tripViewModel: TripViewModel
    private var searchQuery: String = ""

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlanningBinding.inflate(inflater, container, false)

        itineraryAdapter = ItineraryAdapter(itineraryList) { trip ->
            val intent = Intent(activity, TripDetailsActivity::class.java)
            intent.putExtra("tripId", trip.id)
            startActivity(intent)
        }

        binding.itineraryRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.itineraryRecyclerView.adapter = itineraryAdapter

        tripViewModel = ViewModelProvider(this).get(TripViewModel::class.java)

        val currentUserId = getCurrentUserId()

        if (currentUserId != null) {
            tripViewModel.fetchTripsWithRoleFilter(currentUserId)
        }

        tripViewModel.trips.observe(viewLifecycleOwner) { trips ->
            itineraryList.clear()
            if (searchQuery.isNotEmpty()) {
                filterList(searchQuery)
            } else {
                itineraryList.addAll(trips)
            }
            itineraryAdapter.notifyDataSetChanged()
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim()
                filterList(searchQuery)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.fabAddItinerary.setOnClickListener {
            val intent = Intent(activity, TripInformationActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterList(query: String) {
        val filteredItems = tripViewModel.trips.value?.filter { place ->
            place.name.contains(query, ignoreCase = true) ||
                    place.homeCity.contains(query, ignoreCase = true) ||
                    place.destination.contains(query, ignoreCase = true)
        } ?: listOf()

        itineraryList.clear()
        itineraryList.addAll(filteredItems)
        itineraryAdapter.notifyDataSetChanged()
    }

    private fun getCurrentUserId(): String? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser?.uid
    }
}