package com.example.planperfect.view.planning.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.PlaceItemLayoutBinding // Import the binding class
import com.example.planperfect.view.planning.EditItineraryDetailsActivity
import com.example.planperfect.view.planning.PlacesDetailsActivity
import com.example.planperfect.view.planning.TripDetailsFragment
import com.example.planperfect.viewmodel.TripViewModel
import kotlinx.coroutines.launch

class PlacesAdapter(
    private var placesList: MutableList<TouristPlace>,
    private val tripId: String,
    private val tripViewModel: TripViewModel,
    private val dayId: String,
    private val lifecycleOwner: TripDetailsFragment
) : RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    // Store user role
    private var currentUserRole: String? = null

    inner class PlaceViewHolder(val binding: PlaceItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding =
            PlaceItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placesList[position]
        holder.binding.placeName.text = place.name
        holder.binding.itemDescription.text = getFirstTwoSentences(place.description)
        holder.binding.placeCategory.text = "• ${place.category}"
        holder.binding.note.text = place.notes
        holder.binding.duration.text = "${place.startTime} - ${place.endTime}"

        val imageUrl = place.imageUrls.firstOrNull()
        Glide.with(holder.binding.itemImage.context)
            .load(imageUrl)
            .placeholder(R.drawable.tourist_image_1)
            .into(holder.binding.itemImage)

        // Set button visibility based on user role
        if (currentUserRole == "viewer") {
            holder.binding.closeButton.visibility = View.GONE
            holder.binding.editButton.visibility = View.GONE
        } else {
            holder.binding.closeButton.visibility = View.VISIBLE
            holder.binding.editButton.visibility = View.VISIBLE
        }

        holder.binding.closeButton.setOnClickListener {
            lifecycleOwner.lifecycleScope.launch {
                val success = tripViewModel.removePlace(tripId, dayId, place)
                if (success) {
                    if (position < placesList.size) {
                        placesList.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        Log.d("PLACES_ADAPTER", "Empty List")
                    }
                }
            }
        }

        holder.binding.editButton.setOnClickListener {
            val context = holder.binding.root.context
            val intent = Intent(context, EditItineraryDetailsActivity::class.java).apply {
                // Pass the selected TouristPlace and its position to the details activity
                putExtra("place", place)
                putExtra("tripId", tripId)
                putExtra("dayId", dayId)
                putExtra("placePosition", holder.bindingAdapterPosition) // Pass the position
            }
            context.startActivity(intent)
        }

        holder.binding.cardButton.setOnClickListener {
                // navigate to PlacesDetailsActivity
                val context = holder.binding.root.context
                val intent = Intent(context, PlacesDetailsActivity::class.java).apply {
                    // Pass the selected TouristPlace to the details activity
                    putExtra("place", place)
                }
                context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    private fun getFirstTwoSentences(description: String): String {
        // Split the description by '.', '!', or '?' to get sentences
        val sentences = description.split(Regex("[.!?]"))
            .map { it.trim() } // Trim whitespace from each sentence
            .filter { it.isNotEmpty() } // Remove any empty sentences

        // Join the first two sentences and add a period at the end if they exist
        return sentences.take(2).joinToString(". ") + if (sentences.size > 2) "." else ""
    }

    // Function to update the places list
    fun updatePlaces(newPlaces: List<TouristPlace>, userRole: String?) {
        placesList.clear()
        placesList.addAll(newPlaces)
        currentUserRole = userRole // Save the user role
        notifyDataSetChanged() // Notify the RecyclerView to refresh
    }
}
