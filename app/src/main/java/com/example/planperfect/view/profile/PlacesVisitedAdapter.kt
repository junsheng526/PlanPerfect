package com.example.planperfect.view.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace

class PlacesVisitedAdapter(private val places: List<TouristPlace>) :
    RecyclerView.Adapter<PlacesVisitedAdapter.PlacesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favourite_place, parent, false)
        return PlacesViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val place = places[position]
        holder.bind(place)
    }

    override fun getItemCount(): Int = places.size

    inner class PlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placeName: TextView = itemView.findViewById(R.id.place_name)
        private val placeDescription: TextView = itemView.findViewById(R.id.itemDescription)

        fun bind(place: TouristPlace) {
            placeName.text = place.name
            placeDescription.text = place.description
            // Set other data fields (like an image, if available) as needed
        }
    }
}
