package com.example.planperfect.view.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.ItemFilteredBinding

class FilteredAdapter(
    private val items: List<TouristPlace>,
    private val onItemClicked: ((TouristPlace) -> Unit)? = null
) : RecyclerView.Adapter<FilteredAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemFilteredBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TouristPlace) {
            Glide.with(binding.itemImage.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.tourist_image_1)
                .into(binding.itemImage)

            // Set other text fields
            binding.itemTitle.text = item.name
            binding.itemDescription.text = item.description
            binding.itemTag.text = "\u2022 ${item.category}"

            // Handle item click, if listener is provided
            binding.root.setOnClickListener {
                onItemClicked?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFilteredBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}