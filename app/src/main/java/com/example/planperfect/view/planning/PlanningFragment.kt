package com.example.planperfect.view.planning

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
import com.example.planperfect.view.profile.CollaboratorViewModelFactory
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.CollaboratorViewModel
import com.example.planperfect.viewmodel.TripViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlanningFragment : Fragment() {
    private lateinit var binding: FragmentPlanningBinding
    private lateinit var itineraryAdapter: ItineraryAdapter
    private val itineraryList = mutableListOf<Trip>()
    private lateinit var tripViewModel: TripViewModel
    private var searchQuery: String = ""
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var collaboratorVm: CollaboratorViewModel
    private var selectedDate: String? = null
    private var selectedStatus: String? = null
    private var selectedRole: String? = null

    private companion object {
        const val FILTER_REQUEST_CODE = 1001
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlanningBinding.inflate(inflater, container, false)

        val factory = CollaboratorViewModelFactory(authViewModel)
        collaboratorVm = ViewModelProvider(this, factory).get(CollaboratorViewModel::class.java)

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

        binding.filterBtn.setOnClickListener {
            val intent = Intent(activity, FilterItineraryActivity::class.java)
            if(selectedDate != null){
                intent.putExtra("FILTER_DATE", selectedDate)
            }

            if(selectedStatus != null){
                intent.putExtra("FILTER_STATUS", selectedStatus)
            }

            if(selectedRole != null){
                intent.putExtra("FILTER_ROLE", selectedRole)
            }
            startActivityForResult(intent, FILTER_REQUEST_CODE)
        }

        return binding.root
    }

    private fun loadUserData() {
        val userId = authViewModel.getCurrentUserId()

        if (!userId.isNullOrBlank()) {
            lifecycleScope.launch {
                val user = authViewModel.get(userId)
                user?.let {
                    populateUserData(it)
                }
            }
        }
    }

    private fun populateUserData(user: User) {
        binding.apply {
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

    @SuppressLint("NotifyDataSetChanged")
    private fun getStatus(startDate: String, endDate: String): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val start = dateFormat.parse(startDate)
        val end = dateFormat.parse(endDate)

        return when {
            currentDate.before(start) -> "Pending"
            currentDate.after(end) -> "Completed"
            else -> "Ongoing"
        }
    }

    private fun applyFilters(dateFilter: String?, statusFilter: String?, roleFilter: String?) {
        val userId = getCurrentUserId() ?: return

        lifecycleScope.launch {
            val filteredTrips = tripViewModel.trips.value?.mapNotNull { trip ->
                val status = getStatus(trip.startDate, trip.endDate)
                val userRole =
                    collaboratorVm.getUserRole(userId, trip.id) // Fetch role asynchronously

                // Apply filter conditions
                val dateMatches =
                    dateFilter == null || trip.startDate == dateFilter
                val statusMatches =
                    statusFilter == null || statusFilter == "-" || statusFilter == status
                val roleMatches =
                    roleFilter == null || roleFilter == "-" || roleFilter == userRole

                if (dateMatches && statusMatches && roleMatches) {
                    trip.copy()
                } else null
            } ?: listOf()

            itineraryList.clear()
            itineraryList.addAll(filteredTrips)
            itineraryAdapter.notifyDataSetChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILTER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val dateFilter = data?.getStringExtra("FILTER_DATE")
            val statusFilter = data?.getStringExtra("FILTER_STATUS")
            val roleFilter = data?.getStringExtra("FILTER_ROLE")

            selectedDate = dateFilter
            selectedStatus = statusFilter
            selectedRole = roleFilter

            applyFilters(dateFilter, statusFilter, roleFilter)
        }
    }
}
