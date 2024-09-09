package com.example.planperfect.view.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planperfect.R
import com.example.planperfect.data.model.Itinerary
import com.example.planperfect.databinding.ItineraryItemBinding

class ItineraryAdapter(private val itineraryList: List<Itinerary>) :
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

        fun bind(place: Itinerary) {
            // Bind data to views
            Glide.with(binding.imageViewBackground.context)
                .load(place.imageUrl)  // item.image is the URL
                .placeholder(R.drawable.tourist_image_1)
                .into(binding.imageViewBackground)
            binding.itineraryName.text = place.name

            // Handle status button click if needed
            binding.statusButton.setOnClickListener {
                // Implement status button click logic
            }
        }
    }
}
