package com.example.planperfect.view.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.planperfect.databinding.FragmentStatisticsBinding
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.CollaboratorViewModel
import com.example.planperfect.viewmodel.TripViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var collaboratorViewModel: CollaboratorViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var tripViewModel: TripViewModel
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModels
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        tripViewModel = ViewModelProvider(requireActivity()).get(TripViewModel::class.java)

        val factory = CollaboratorViewModelFactory(authViewModel)
        collaboratorViewModel = ViewModelProvider(this, factory).get(CollaboratorViewModel::class.java)

        // Get the year passed as an argument
        val year = arguments?.getString("year") ?: "All"

        userId = authViewModel.getCurrentUserId()

        tripViewModel.trips.observe(viewLifecycleOwner) { tripList ->
            if(tripList != null){
                userId?.let { collaboratorViewModel.calculateStatisticsForYear(year, it, tripList) }
            }
        }

        collaboratorViewModel.calculatedTripsLiveData.observe(viewLifecycleOwner) { tripList ->
            binding.tripBtn.setOnClickListener {
                if (tripList != null && tripList.isNotEmpty()) {
                    val intent = Intent(requireContext(), TotalTravelTripsActivity::class.java)
                    intent.putParcelableArrayListExtra("tripList", ArrayList(tripList))
                    startActivity(intent)
                }
            }
        }

        collaboratorViewModel.calculatedTripsLiveData.observe(viewLifecycleOwner) { tripList ->
            binding.travelDayBtn.setOnClickListener {
                if (tripList != null && tripList.isNotEmpty()) {
                    val intent = Intent(requireContext(), TotalTravelDaysActivity::class.java)
                    intent.putParcelableArrayListExtra("tripList", ArrayList(tripList))
                    startActivity(intent)
                }
            }
        }

        // Observe collaborator statistics for the year
        collaboratorViewModel.tripStatistics.observe(viewLifecycleOwner, Observer { statistics ->
            if (statistics != null) {
                // Update your UI with the statistics data
                binding.totalTripsTextView.text = "${statistics.totalTrips}"
                binding.totalTravelDaysTextView.text = "${statistics.totalTravelDays}"
                binding.totalPlacesVisitedTextView.text = "${statistics.totalPlacesVisited}"
            }
        })

        collaboratorViewModel.visitedPlacesLiveData.observe(viewLifecycleOwner) { placesList ->
            binding.placesBtn.setOnClickListener {
                Log.d("StatisticFragment placesList-> ", placesList.toString())
                if (placesList != null && placesList.isNotEmpty()) {
                    val intent = Intent(requireContext(), AllPlacesVisitedActivity::class.java)
                    intent.putParcelableArrayListExtra("placesList", ArrayList(placesList))
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
