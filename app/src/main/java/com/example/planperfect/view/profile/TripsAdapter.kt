package com.example.planperfect.view.profile

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planperfect.R
import com.example.planperfect.data.model.Trip
import com.example.planperfect.databinding.StatisticItineraryItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TripsAdapter(
    private val tripList: List<Trip>,
    private val isTravelDays: Boolean,
    private val onTripClickListener: OnTripClickListener
) : RecyclerView.Adapter<TripsAdapter.TripViewHolder>() {

    interface OnTripClickListener {
        fun onTripClick(trip: Trip)
    }

    // ViewHolder class using View Binding for item views
    class TripViewHolder(private val binding: StatisticItineraryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind the trip data to the views
        @SuppressLint("SetTextI18n")
        fun bind(trip: Trip, position: Int, isTravelDays: Boolean, listener: OnTripClickListener?) {
            if (isTravelDays) {
                val duration = calculateTripDuration(trip.startDate, trip.endDate)
                binding.itemTitleTv.text = "Travel duration : $duration"
            } else {
                // Set the trip title (e.g., "Trip 1", "Trip 2", ...)
                binding.itemTitleTv.text = "Trip ${position + 1}"
            }

            // Set the itinerary name as the trip's name
            binding.itineraryName.text = trip.name

            // Load the trip image using Glide
            Glide.with(binding.imageViewBackground.context)
                .load(trip.imageUrl)
                .placeholder(R.drawable.tourist_image_1) // Placeholder image
                .into(binding.imageViewBackground)

            binding.root.setOnClickListener {
                listener?.onTripClick(trip)
            }

            Log.d("TripsAdapter", "Image URL: ${trip.imageUrl}")
        }

        private fun calculateTripDuration(startDate: String, endDate: String): Int {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return try {
                val start = format.parse(startDate)
                val end = format.parse(endDate)
                if (start != null && end != null) {
                    val diffInMillis = end.time - start.time
                    (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1 // +1 to include the start day
                } else {
                    0
                }
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }
    }

    // Create new views (this method is invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        // Inflate the layout using View Binding
        val binding = StatisticItineraryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    // Replace the contents of a view (this method is invoked by the layout manager)
    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        // Get the current trip at the given position
        val currentTrip = tripList[position]
        // Bind the data to the ViewHolder
        holder.bind(currentTrip, position, isTravelDays, onTripClickListener)
    }

    // Return the size of the trip list (invoked by the layout manager)
    override fun getItemCount(): Int {
        return tripList.size
    }
}
