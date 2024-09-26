package com.example.planperfect.view.planning

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.planperfect.data.model.UserRating
import com.example.planperfect.databinding.ActivityWriteReviewBinding
import com.example.planperfect.viewmodel.ReviewViewModel
import com.google.firebase.auth.FirebaseAuth

class WriteReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteReviewBinding
    private val reviewViewModel: ReviewViewModel by viewModels() // Using viewModels delegate for ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSubmit.setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {
        val rating = binding.ratingBar.rating
        val reviewText = binding.editTextReview.text.toString().trim()

        if (rating == 0f || reviewText.isEmpty()) {
            Toast.makeText(this, "Please provide a rating and a review.", Toast.LENGTH_SHORT).show()
            return
        }

        // Sample userId; replace with actual logic to get the user ID
// Get the current user's ID from Firebase Auth
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the place ID from the intent
        val placeId = intent.getStringExtra("placeId") ?: run {
            Toast.makeText(this, "Place ID not found.", Toast.LENGTH_SHORT).show()
            return
        }
        val userRating = UserRating(userId, placeId, rating, reviewText) // Make sure placeId is included

        // Call ViewModel to save the review
        reviewViewModel.addReview(userRating) { success ->
            if (success) {
                Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after submission
            } else {
                Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}