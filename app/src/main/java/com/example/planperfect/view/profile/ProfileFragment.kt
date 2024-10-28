package com.example.planperfect.view.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.R
import com.example.planperfect.data.model.User
import com.example.planperfect.databinding.ActivityAddCollaboratorBinding
import com.example.planperfect.databinding.FragmentProfileBinding
import com.example.planperfect.utils.toBitmap
import com.example.planperfect.view.authentication.AuthenticationActivity
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.TripViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    // ViewBinding variable
    private lateinit var binding: FragmentProfileBinding
    private val tripViewModel: TripViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editBtn.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.invitationBtn.setOnClickListener {
            val intent = Intent(activity, CollaborationInvitationActivity::class.java)
            startActivity(intent)
        }

        binding.favouriteBtn.setOnClickListener {
            val intent = Intent(activity, FavoritePlacesActivity::class.java)
            startActivity(intent)
        }

        binding.showAllBtn.setOnClickListener {
            val intent = Intent(activity, TravelStatisticsActivity::class.java)
            startActivity(intent)
        }

        // Set up the logout button click listener
        binding.logoutButton.setOnClickListener {
            // Perform logout
            FirebaseAuth.getInstance().signOut()

            // Redirect the user to the login screen
            val intent = Intent(activity, AuthenticationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Set up the line chart
        tripViewModel.tripCountByYear.observe(viewLifecycleOwner) { tripCountByYear ->
            // Log the data to ensure it's being passed correctly
            Log.d("ProfileFragment", "Trip Count By Year: $tripCountByYear")
            setupLineChart(binding.tripCountChart, tripCountByYear)
        }

        loadUserData()
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
            username.text = user.name // Populate username
            userEmail.text = user.email // Populate email

            // Populate profile picture
            if (user.photo.toBitmap() != null) {
                imageViewProfile.setImageBitmap(user.photo.toBitmap())
                headerProfile.setImageBitmap(user.photo.toBitmap())
                binding.letterOverlay.visibility = View.GONE
                binding.letterOverlayTv.visibility = View.GONE
            } else {
                imageViewProfile.setImageResource(R.drawable.profile_bg)
                headerProfile.setImageResource(R.drawable.profile_bg)
                binding.letterOverlay.visibility = View.VISIBLE
                binding.letterOverlayTv.visibility = View.VISIBLE

                val firstLetter = user.name.firstOrNull()?.toString()?.uppercase() ?: "U"
                binding.letterOverlay.text = firstLetter
                binding.letterOverlayTv.text = firstLetter
            }
        }
    }

    private fun setupLineChart(lineChart: LineChart, tripCountByYear: Map<Int, Int>) {
        Log.d("ProfileFragment", "Updating chart with data: $tripCountByYear")

        val totalTrips = tripCountByYear.values.sum()
        binding.tripCount.text = totalTrips.toString()

        if (tripCountByYear.isEmpty()) {
            lineChart.clear() // Clear the chart if no data
            return
        }

        // Sort the map by year (x-axis value)
        val sortedTripCountByYear = tripCountByYear.toSortedMap()

        // Log to check if duplicate entries exist
        Log.d("ProfileFragment", "Sorted trip count by year: $sortedTripCountByYear")

        // Convert the sorted map to entries for the line chart
        val entries = sortedTripCountByYear.map { (year, count) ->
            Entry(year.toFloat(), count.toFloat())
        }

        Log.d("ProfileFragment", "Chart entries: $entries")

        // Create a dataset and give it a type
        val dataSet = LineDataSet(entries, "Trips by Year").apply {
            color = resources.getColor(R.color.purple_500, null)
            valueTextColor = resources.getColor(android.R.color.black, null)
            lineWidth = 2f
            circleRadius = 5f
            setCircleColor(resources.getColor(R.color.purple_500, null))
            setDrawValues(true)
            setDrawCircles(true)
            mode = LineDataSet.Mode.LINEAR // or CUBIC_BEZIER if you prefer smooth curves
        }

        // Create a line data object with the dataset
        val lineData = LineData(dataSet)

        // Customize chart appearance
        lineChart.apply {
            data = lineData
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setScaleEnabled(true)
            axisRight.isEnabled = false
            xAxis.apply {
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            }
            axisLeft.apply {
                setDrawGridLines(false)
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                        return value.toInt().toString() // Format Y-axis labels as integers
                    }
                }
                setLabelCount(totalTrips, true)
            }
            invalidate() // Refresh the chart
        }
    }
}
