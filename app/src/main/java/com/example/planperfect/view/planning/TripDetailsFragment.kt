package com.example.planperfect.view.planning

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.FragmentTripDetailsBinding
import com.example.planperfect.view.planning.adapter.PlacesAdapter
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.CollaboratorViewModel
import com.example.planperfect.viewmodel.TripViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripDetailsFragment : Fragment() {

    private val tripViewModel: TripViewModel by viewModels()
    private lateinit var binding: FragmentTripDetailsBinding
    private lateinit var placesAdapter: PlacesAdapter // RecyclerView adapter to display places
    private var tripId = ""
    private var selectedDayId: String = "$tripId-Day-1"  // Track the selected dayId
    private var selectedButton: Button? = null

    private lateinit var collaboratorViewModel: CollaboratorViewModel
    private lateinit var authViewModel: AuthViewModel

    private var currentUserRole: String? = null


    companion object {
        private const val ARG_TRIP_ID = "tripId"

        fun newInstance(tripId: String): TripDetailsFragment {
            val fragment = TripDetailsFragment()
            val args = Bundle()
            args.putString(ARG_TRIP_ID, tripId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTripDetailsBinding.inflate(inflater, container, false)
        tripId = arguments?.getString(ARG_TRIP_ID) ?: return binding.root

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // Create CollaboratorViewModel directly
        collaboratorViewModel = CollaboratorViewModel(authViewModel)

        checkCurrentUserRole()

        // Initialize the RecyclerView to display the places
        binding.placesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch trip details from ViewModel in a coroutine
        lifecycleScope.launch {
            val trip = tripViewModel.get(tripId)
            if (trip != null) {
                val startDate = parseDate(trip.startDate)
                val endDate = parseDate(trip.endDate)

                if (startDate != null && endDate != null) {
                    val days = getDaysBetween(startDate, endDate)

                    // Dynamically create buttons based on the number of days
                    var firstButton: Button? = null
                    for (i in 1..days) {
                        val button = Button(requireContext()).apply {
                            text = "Day $i"
                            background = resources.getDrawable(R.drawable.button_background_default, null)
                            setTextColor(resources.getColor(R.color.text_color_date, null))

                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(16, 0, 16, 0)
                            }

                            layoutParams = params
                            setOnClickListener {
                                // Generate dayId for the selected day
                                selectedDayId = "$tripId-Day-$i"
                                Log.d("SELECTED ID::" , selectedDayId!!)

                                // Display places for this day
                                displayDayContent(i)

                                selectedButton?.background = resources.getDrawable(R.drawable.button_background_default, null) // Reset to default
                                background = resources.getDrawable(R.drawable.button_background_selected, null)
                                selectedButton = this

                                placesAdapter = PlacesAdapter(mutableListOf(), tripId, tripViewModel, selectedDayId, this@TripDetailsFragment)
                                binding.placesRecyclerView.adapter = placesAdapter
                            }
                        }
                        if (i == 1) {
                            firstButton = button // Capture the first button for Day 1
                        }
                        binding.dayButtonContainer.addView(button)
                    }

                    // Show content for Day 1 by default
                    firstButton?.let {
                        it.performClick()  // Simulate a click on the Day 1 button
                    }
                } else {
                    Toast.makeText(context, "Invalid trip dates", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Failed to fetch trip data", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle FAB click to add itinerary
        binding.fabAddItinerary.setOnClickListener {
            if (selectedDayId != "") {
                val intent = Intent(context, AddDestinationActivity::class.java)
                intent.putExtra("tripId", tripId)
                intent.putExtra("dayId", selectedDayId)  // Pass the selected dayId
                startActivity(intent)
            } else {
                Toast.makeText(context, "Please select a day first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnViewRoute.setOnClickListener {
            // Handle "View Route" button click
            if (selectedDayId.isNotEmpty()) {
                val intent = Intent(context, ViewRouteActivity::class.java).apply {
                    putExtra("tripId", tripId)
                    putExtra("dayId", selectedDayId)
                }
                startActivity(intent)
            } else {
                Toast.makeText(context, "Please select a day first", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
    private fun checkCurrentUserRole() {
        val currentUserId = authViewModel.getCurrentUserId() // Assumes a method to get current user ID
        lifecycleScope.launch {
            if (currentUserId != null && tripId != null) {
                currentUserRole = collaboratorViewModel.getUserRole(currentUserId, tripId)
                if(currentUserRole != null && currentUserRole == "viewer"){
                    binding.fabAddItinerary.visibility = View.GONE
                }
            }
        }
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            Toast.makeText(context, "Error parsing date: $dateString", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun getDaysBetween(startDate: Date, endDate: Date): Int {
        val diffInMillis = endDate.time - startDate.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1  // Add 1 to include the last day
    }

    private fun displayDayContent(day: Int) {
        val dayId = selectedDayId ?: return
        val itineraryCollection = FirebaseFirestore.getInstance()
            .collection("trip")
            .document(tripId)
            .collection("itineraries")

        itineraryCollection.document(dayId).addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Toast.makeText(context, "Failed to fetch itinerary for Day $day", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            documentSnapshot?.let {
                // Extract the list of places from the Firestore document
                val placesList = it.get("places") as? List<HashMap<String, Any>> ?: emptyList()
                val touristPlaces = placesList.map { placeMap ->
                    TouristPlace(
                        id = placeMap["id"] as? String ?: "",
                        name = placeMap["name"] as String,
                        category = placeMap["category"] as String,
                        startTime = placeMap["startTime"] as? String,
                        endTime = placeMap["endTime"] as? String,
                        notes = placeMap["notes"] as? String,
                        imageUrls = placeMap["imageUrls"] as List<String>,
                        description = placeMap["description"] as String,
                        longDescription = placeMap["longDescription"] as? String,
                        currencyCode = placeMap["currencyCode"] as? String,
                        isFavorite = placeMap["isFavorite"] as? Boolean ?: false,
                        latitude = placeMap["latitude"] as? Double,
                        longitude = placeMap["longitude"] as? Double,
                    )
                }

                // Sort the list of places (if needed)
                val sortedPlaces = touristPlaces.sortedWith(compareBy(
                    { place -> place.startTime?.let { parseTime(it, SimpleDateFormat("hh:mm a", Locale.getDefault())) } },
                    { place -> place.endTime?.let { parseTime(it, SimpleDateFormat("hh:mm a", Locale.getDefault())) } }
                ))

                // Update the RecyclerView adapter with the sorted places
                placesAdapter.updatePlaces(sortedPlaces, currentUserRole)
                lifecycleScope.launch {
                    val isSuccess = tripViewModel.updateSortedPlaces(tripId, dayId, sortedPlaces)
                }
            }
        }

//        itineraryCollection.document(dayId).get().addOnSuccessListener { documentSnapshot ->
//            val placesList = documentSnapshot.get("places") as? List<HashMap<String, Any>> ?: emptyList()
//            val touristPlaces = placesList.map { placeMap ->
//                TouristPlace(
//                    name = placeMap["name"] as String,
//                    category = placeMap["category"] as String,
//                    startTime = placeMap["startTime"] as? String,
//                    endTime = placeMap["endTime"] as? String,
//                    notes = placeMap["notes"] as? String,
//                    imageUrls = placeMap["imageUrls"] as List<String>,
//                    description = placeMap["description"] as String,
//                    longDescription = placeMap["longDescription"] as? String,
//                    currencyCode = placeMap["currencyCode"] as? String,
//                )
//            }
//
//            // Define the time format you're using (e.g., "HH:mm" for 24-hour time)
//            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
//
//            // Sort the list of places by start time and end time
//            val sortedPlaces = touristPlaces.sortedWith(compareBy(
//                { place -> place.startTime?.let { parseTime(it, timeFormat) } },
//                { place -> place.endTime?.let { parseTime(it, timeFormat) } }
//            ))
//
//            // Update the RecyclerView adapter with the sorted places
//            placesAdapter.updatePlaces(sortedPlaces, currentUserRole)
//            lifecycleScope.launch {
//                val isSuccess = tripViewModel.updateSortedPlaces(tripId, dayId, sortedPlaces)
//                if(isSuccess){
//                    Toast.makeText(context, "SortedList Updated", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }.addOnFailureListener {
//            Toast.makeText(context, "Failed to fetch itinerary for Day $day", Toast.LENGTH_SHORT).show()
//        }
    }

    private fun parseTime(timeString: String, timeFormat: SimpleDateFormat): Date? {
        return try {
            timeFormat.parse(timeString)
        } catch (e: Exception) {
            null // Return null if parsing fails
        }
    }
}
