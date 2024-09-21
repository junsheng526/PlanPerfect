package com.example.planperfect.view.planning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace

class PlacesAdapter(private var placesList: MutableList<TouristPlace>) :
    RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.place_name)
        val placeCategory: TextView = itemView.findViewById(R.id.place_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.place_item_layout, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placesList[position]
        holder.placeName.text = place.name
        holder.placeCategory.text = place.category
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    // Function to update the places list
    fun updatePlaces(newPlaces: List<TouristPlace>) {
        placesList.clear()
        placesList.addAll(newPlaces)
        notifyDataSetChanged() // Notify the RecyclerView to refresh
    }
}
