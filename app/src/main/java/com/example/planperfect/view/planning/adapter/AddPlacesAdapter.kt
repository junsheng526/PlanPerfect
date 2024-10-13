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
            binding.itemDescription.text = getFirstTwoSentences(item.description)
            binding.placeCategory.text = "\u2022 ${item.category}"

            binding.addBtn.setOnClickListener {
                // navigate to AddNewPlacesDetailsActivity
                onItemClicked?.invoke(item)
            }

            binding.root.setOnClickListener {
                // navigate to PlacesDetailsActivity
                val context = binding.root.context
                val intent = Intent(context, PlacesDetailsActivity::class.java).apply {
                    // Pass the selected TouristPlace to the details activity
                    putExtra("place", item)
                }
                context.startActivity(intent)
            }
        }
    }

    private fun getFirstTwoSentences(description: String): String {
        // Split the description by '.', '!', or '?' to get sentences
        val sentences = description.split(Regex("[.!?]"))
            .map { it.trim() } // Trim whitespace from each sentence
            .filter { it.isNotEmpty() } // Remove any empty sentences

        // Join the first two sentences and add a period at the end if they exist
        return sentences.take(2).joinToString(". ") + if (sentences.size > 2) "." else ""
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