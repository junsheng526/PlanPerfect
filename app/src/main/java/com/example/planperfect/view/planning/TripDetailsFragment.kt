package com.example.planperfect.view.planning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.R
import com.example.planperfect.viewmodel.TripViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripDetailsFragment : Fragment() {

    private val tripViewModel: TripViewModel by viewModels()

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
        val rootView = inflater.inflate(R.layout.fragment_trip_details, container, false)
        val dayButtonContainer = rootView.findViewById<LinearLayout>(R.id.dayButtonContainer)
        val dayContentTextView = rootView.findViewById<TextView>(R.id.dayContentTextView)

        val tripId = arguments?.getString(ARG_TRIP_ID) ?: return rootView

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
                            setOnClickListener {
                                // Display content for this day
                                displayDayContent(i, dayContentTextView)
                            }
                        }
                        if (i == 1) {
                            firstButton = button // Capture the first button for Day 1
                        }
                        dayButtonContainer.addView(button)
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

        return rootView
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

    private fun displayDayContent(day: Int, textView: TextView) {
        // Display content for the selected day
        textView.text = "Content for Day $day"  // Here you can update this with actual day-specific data
    }
}
