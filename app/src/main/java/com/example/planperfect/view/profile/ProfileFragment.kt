package com.example.planperfect.view.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.planperfect.R
import com.example.planperfect.databinding.FragmentProfileBinding
import com.example.planperfect.view.authentication.AuthenticationActivity
import com.example.planperfect.viewmodel.TripViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    // ViewBinding variable
    private var _binding: FragmentProfileBinding? = null
    private val tripViewModel: TripViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    private fun setupLineChart(lineChart: LineChart, tripCountByYear: Map<Int, Int>) {
        if (tripCountByYear.isEmpty()) {
            lineChart.clear() // Clear the chart if no data
            return
        }

        // Sort the map by year (x-axis value)
        val sortedTripCountByYear = tripCountByYear.toSortedMap()

        // Convert the sorted map to entries for the line chart
        val entries = sortedTripCountByYear.map { (year, count) ->
            Entry(year.toFloat(), count.toFloat())
        }

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
            }
            invalidate() // Refresh the chart
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding
    }
}
