package com.example.planperfect.view.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planperfect.R
import com.example.planperfect.databinding.ItemFavouritePlaceBinding
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.view.planning.PlacesDetailsActivity

class PlacesVisitedAdapter(private val places: List<TouristPlace>) :
    RecyclerView.Adapter<PlacesVisitedAdapter.PlacesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val binding = ItemFavouritePlaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlacesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val place = places[position]
        holder.bind(place)
    }

    override fun getItemCount(): Int = places.size

    inner class PlacesViewHolder(private val binding: ItemFavouritePlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: TouristPlace) {
            binding.placeName.text = place.name
            binding.itemDescription.text = place.description
            val imageUrl = place.imageUrls.firstOrNull()

            Glide.with(binding.itemImage.context)
                .load(imageUrl)
                .placeholder(R.drawable.loading)
                .into(binding.itemImage)

            binding.closeButton.visibility = View.GONE

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, PlacesDetailsActivity::class.java).apply {
                    putExtra("place", place)
                }
                context.startActivity(intent)
            }
        }
    }
}
