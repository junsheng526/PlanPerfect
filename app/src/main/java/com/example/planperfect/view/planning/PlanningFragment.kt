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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.R
import com.example.planperfect.data.model.Trip
import com.example.planperfect.data.model.User
import com.example.planperfect.databinding.FragmentPlanningBinding
import com.example.planperfect.utils.toBitmap
import com.example.planperfect.view.planning.adapter.ItineraryAdapter
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.TripViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class PlanningFragment : Fragment() {
    private lateinit var binding: FragmentPlanningBinding
    private lateinit var itineraryAdapter: ItineraryAdapter
    private val itineraryList = mutableListOf<Trip>()
    private lateinit var tripViewModel: TripViewModel
    private var searchQuery: String = ""
    private val authViewModel: AuthViewModel by viewModels()

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

        loadUserData()

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

    private fun loadUserData() {
        val userId = authViewModel.getCurrentUserId()

        if (!userId.isNullOrBlank()) {
            lifecycleScope.launch {
                val user = authViewModel.get(userId) // Fetch user data from ViewModel
                user?.let {
                    populateUserData(it) // Populate the UI with user data
                }
            }
        }
    }

    private fun populateUserData(user: User) {
        binding.apply {
            // Populate profile picture
            if (user.photo.toBitmap() != null) {
                headerProfile.setImageBitmap(user.photo.toBitmap())
                binding.letterOverlayTv.visibility = View.GONE
            } else {
                headerProfile.setImageResource(R.drawable.profile_bg)
                binding.letterOverlayTv.visibility = View.VISIBLE

                val firstLetter = user.name.firstOrNull()?.toString()?.uppercase() ?: "U"
                binding.letterOverlayTv.text = firstLetter
            }
        }
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