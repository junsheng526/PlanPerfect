package com.example.planperfect.view.planning

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planperfect.R
import com.example.planperfect.data.model.Trip
import com.example.planperfect.databinding.ItineraryItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.ContextCompat

class ItineraryAdapter(private val itineraryList: List<Trip>) :
    RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val binding = ItineraryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItineraryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        val place = itineraryList[position]
        holder.bind(place)
    }

    override fun getItemCount(): Int = itineraryList.size

    inner class ItineraryViewHolder(private val binding: ItineraryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(trip: Trip) {
            // Bind data to views
            Glide.with(binding.imageViewBackground.context)
                .load(trip.imageUrl)  // item.image is the URL
                .placeholder(R.drawable.tourist_image_1)
                .into(binding.imageViewBackground)
            binding.itineraryName.text = trip.name

            val currentDate = Date()
            val startDate = parseDate(trip.startDate)
            val endDate = parseDate(trip.endDate)

            // Check if the dates are valid and set the status accordingly
            if (startDate != null && endDate != null) {
                when {
                    currentDate.before(startDate) -> {
                        // Trip is in the future
                        binding.statusButton.text = "Pending"
                        binding.statusButton.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.pending_color)
                    }
                    currentDate.after(endDate) -> {
                        // Trip has ended
                        binding.statusButton.text = "Completed"
                        binding.statusButton.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.completed_color)
                    }
                    currentDate in startDate..endDate -> {
                        // Trip is ongoing
                        binding.statusButton.text = "On Going"
                        binding.statusButton.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.ongoing_color)
                    }
                }
            } else {
                binding.statusButton.text = "Unknown Status"
            }

            // Handle status button click if needed
            binding.statusButton.setOnClickListener {
                // Implement status button click logic
            }
        }
        private fun parseDate(dateString: String): Date? {
            return try {
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                format.parse(dateString)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
