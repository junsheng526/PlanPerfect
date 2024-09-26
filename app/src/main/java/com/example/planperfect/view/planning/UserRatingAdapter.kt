package com.example.planperfect.view.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planperfect.data.model.UserRating
import com.example.planperfect.databinding.ItemUserRatingBinding
import com.example.planperfect.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRatingAdapter(
    private var reviews: List<UserRating>,
    private val authViewModel: AuthViewModel
) : RecyclerView.Adapter<UserRatingAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(private val binding: ItemUserRatingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userRating: UserRating, userName: String) {
            binding.textViewName.text = userName
            binding.textViewReview.text = userRating.review
            binding.ratingBar.rating = userRating.rating
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemUserRatingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val userRating = reviews[position]

        // Use Coroutine to fetch the user name
        CoroutineScope(Dispatchers.Main).launch {
            val user = authViewModel.get(userRating.userId)
            val userName = user?.name ?: "Unknown User"
            holder.bind(userRating, userName)
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<UserRating>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}