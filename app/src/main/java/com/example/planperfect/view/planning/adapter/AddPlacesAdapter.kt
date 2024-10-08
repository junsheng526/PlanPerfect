package com.example.planperfect.view.planning.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.AddTouristItemBinding
import com.example.planperfect.view.planning.PlacesDetailsActivity

class AddPlacesAdapter(
    private val items: List<TouristPlace>,
    private val onItemClicked: ((TouristPlace) -> Unit)? = null
) : RecyclerView.Adapter<AddPlacesAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: AddTouristItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TouristPlace) {

            val imageUrl = item.imageUrls.firstOrNull()

            Glide.with(binding.itemImage.context)
                .load(imageUrl)
                .placeholder(R.drawable.loading)
                .into(binding.itemImage)

            // Set other text fields
            binding.placeName.text = item.name
            binding.itemDescription.text = item.description
            binding.placeCategory.text = "\u2022 ${item.category}"

            binding.addBtn.setOnClickListener {
                // navigate to AddNewPlacesDetailsActivity
                onItemClicked?.invoke(item)
            }

            binding.root.setOnClickListener {
                // navigate to PlacesDetailsActivity
                val context = binding.root.context
                Log.d("DEBUGGING HAHA", item.toString())
                val intent = Intent(context, PlacesDetailsActivity::class.java).apply {
                    // Pass the selected TouristPlace to the details activity
                    putExtra("place", item)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AddTouristItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}