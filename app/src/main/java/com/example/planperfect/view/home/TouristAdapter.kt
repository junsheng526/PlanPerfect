package com.example.planperfect.view.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planperfect.R
import com.example.planperfect.data.model.Tourist
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.ItemTouristBinding
import com.example.planperfect.view.planning.PlacesDetailsActivity

class TouristAdapter(private var tourists: List<TouristPlace>) :
    RecyclerView.Adapter<TouristAdapter.TouristViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TouristViewHolder {
        val binding = ItemTouristBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TouristViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TouristViewHolder, position: Int) {
        holder.bind(tourists[position])
    }

    fun updateData(newItems: List<TouristPlace>) {
        tourists = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = tourists.size

    class TouristViewHolder(private val binding: ItemTouristBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tourist: TouristPlace) {
            binding.touristTitle.text = tourist.name.replace(" ", "\n")

            val imageUrl = tourist.imageUrls.firstOrNull()

            Glide.with(binding.touristImage.context)
                .load(imageUrl)
                .placeholder(R.drawable.tourist_image_1)
                .into(binding.touristImage)

            binding.root.setOnClickListener {
                // navigate to PlacesDetailsActivity
                val context = binding.root.context
                val intent = Intent(context, PlacesDetailsActivity::class.java).apply {
                    // Pass the selected TouristPlace to the details activity
                    putExtra("place", tourist)
                }
                context.startActivity(intent)
            }
        }
    }
}
