package com.example.planperfect.view.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planperfect.data.model.UserRating
import com.example.planperfect.databinding.ItemUserRatingBinding

class UserRatingAdapter(private var reviews: List<UserRating>) : RecyclerView.Adapter<UserRatingAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(private val binding: ItemUserRatingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userRating: UserRating) {
            binding.textViewRating.text = "${userRating.rating} Stars"
            binding.textViewReview.text = userRating.review
            binding.ratingBar.rating = userRating.rating
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemUserRatingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<UserRating>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}