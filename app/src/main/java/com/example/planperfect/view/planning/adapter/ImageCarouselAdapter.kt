package com.example.planperfect.view.planning.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.planperfect.R
import com.example.planperfect.databinding.CarouselImageItemBinding

class ImageCarouselAdapter(private val imageUrls: MutableList<String>) : RecyclerView.Adapter<ImageCarouselAdapter.ImageViewHolder>() {

    // This variable will track the current image index
    private var currentImageIndex = 0

    inner class ImageViewHolder(private val binding: CarouselImageItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            // Load the current image
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.loading)
                .into(binding.carouselImage)

            // Set visibility of arrows based on the current index
            binding.leftArrow.visibility = if (currentImageIndex > 0) View.VISIBLE else View.INVISIBLE
            binding.rightArrow.visibility = if (currentImageIndex < imageUrls.size - 1) View.VISIBLE else View.INVISIBLE

            // Handle left arrow click to move to the previous image
            binding.leftArrow.setOnClickListener {
                if (currentImageIndex > 0) {
                    currentImageIndex--
                    notifyDataSetChanged() // Notify the adapter to refresh the views
                }
            }

            // Handle right arrow click to move to the next image
            binding.rightArrow.setOnClickListener {
                if (currentImageIndex < imageUrls.size - 1) {
                    currentImageIndex++
                    notifyDataSetChanged() // Notify the adapter to refresh the views
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        // Use view binding to inflate the item layout
        val binding = CarouselImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUrls[currentImageIndex]) // Bind the current image based on the index
    }

    override fun getItemCount(): Int = 1 // We show only one image at a time

    // Additional method to update the data and reset the index
    fun setImageUrls(newImageUrls: List<String>) {
        imageUrls.clear() // Clear the mutable list
        imageUrls.addAll(newImageUrls) // Add all new URLs
        currentImageIndex = 0
        notifyDataSetChanged()
    }
}
