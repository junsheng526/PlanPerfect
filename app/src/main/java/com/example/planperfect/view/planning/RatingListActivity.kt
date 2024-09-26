package com.example.planperfect.view.planning

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.R
import com.example.planperfect.data.model.UserRating
import com.example.planperfect.databinding.ActivityRatingListBinding
import com.example.planperfect.viewmodel.ReviewViewModel

class RatingListActivity : AppCompatActivity() {

    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var binding: ActivityRatingListBinding
    private lateinit var userRatingAdapter: UserRatingAdapter
    private var allReviews: List<UserRating> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRatingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        reviewViewModel = ViewModelProvider(this).get(ReviewViewModel::class.java)

        val placeId = intent.getStringExtra("placeId") ?: return

        // Set up RecyclerView
        userRatingAdapter = UserRatingAdapter(emptyList())
        binding.recyclerViewRatings.apply {
            adapter = userRatingAdapter
            layoutManager = LinearLayoutManager(this@RatingListActivity)
        }

        // Fetch reviews by place ID (real-time)
        reviewViewModel.fetchReviewsByPlaceId(placeId)

        // Observe reviewsLiveData
        reviewViewModel.reviewsLiveData.observe(this) { reviews ->
            allReviews = reviews // Store all reviews
            userRatingAdapter.updateReviews(reviews) // Update RecyclerView with reviews
            updateRatingSection(reviews) // Update the RatingBar and review count
        }

        // Handle button clicks for filtering
        binding.allButton.setOnClickListener { showAllReviews() }
        binding.rating5Button.setOnClickListener { filterReviewsByRating(5) }
        binding.rating4Button.setOnClickListener { filterReviewsByRating(4) }
        binding.rating3Button.setOnClickListener { filterReviewsByRating(3) }
        binding.rating2Button.setOnClickListener { filterReviewsByRating(2) }
        binding.rating1Button.setOnClickListener { filterReviewsByRating(1) }

        binding.writeReviewButton.setOnClickListener {
            val intent = Intent(this, WriteReviewActivity::class.java).apply {
                putExtra("placeId", placeId) // Pass the place ID to the WriteReviewActivity
            }
            startActivity(intent)
        }
    }

    private fun showAllReviews() {
        userRatingAdapter.updateReviews(allReviews) // Show all reviews
        resetButtonStyles() // Reset styles for buttons
        binding.allButton.setBackgroundResource(R.color.selected_date) // Highlight "All"
    }

    private fun filterReviewsByRating(rating: Int) {
        val filteredReviews = allReviews.filter { it.rating.toInt() == rating }
        userRatingAdapter.updateReviews(filteredReviews) // Update RecyclerView with filtered reviews
        resetButtonStyles() // Reset styles for buttons
        when (rating) {
            5 -> binding.rating5Button.setBackgroundResource(R.color.selected_date) // Highlight selected button
            4 -> binding.rating4Button.setBackgroundResource(R.color.selected_date)
            3 -> binding.rating3Button.setBackgroundResource(R.color.selected_date)
            2 -> binding.rating2Button.setBackgroundResource(R.color.selected_date)
            1 -> binding.rating1Button.setBackgroundResource(R.color.selected_date)
        }
    }

    private fun resetButtonStyles() {
        // Reset the background color of all rating buttons
        binding.allButton.setBackgroundResource(R.color.unselected_date)
        binding.rating5Button.setBackgroundResource(R.color.unselected_date)
        binding.rating4Button.setBackgroundResource(R.color.unselected_date)
        binding.rating3Button.setBackgroundResource(R.color.unselected_date)
        binding.rating2Button.setBackgroundResource(R.color.unselected_date)
        binding.rating1Button.setBackgroundResource(R.color.unselected_date)
    }

    private fun updateRatingSection(reviews: List<UserRating>) {
        val averageRating = reviewViewModel.getAverageRating(reviews)
        val reviewCount = reviewViewModel.getReviewCount(reviews)

        // Update RatingBar
        binding.ratingBar.rating = averageRating
        // Update TextView with average rating
        binding.averageRating.text = String.format("%.1f", averageRating)
        // Update TextView with review count
        binding.reviewCount.text = "($reviewCount reviews)"

        val ratingCounts = IntArray(5) // For storing counts for 1 to 5 stars

        reviews.forEach { rating ->
            val ratingValue = rating.rating.toInt() // Ensure rating is an Int

            // Check if the ratingValue is within the valid range
            if (ratingValue in 1..5) {
                ratingCounts[ratingValue - 1]++ // Increment the appropriate star count
            }
        }

        // Update buttons with counts
        binding.rating5Button.text = String.format("5 ★ (%d)", ratingCounts[4])
        binding.rating4Button.text = String.format("4 ★ (%d)", ratingCounts[3])
        binding.rating3Button.text = String.format("3 ★ (%d)", ratingCounts[2])
        binding.rating2Button.text = String.format("2 ★ (%d)", ratingCounts[1])
        binding.rating1Button.text = String.format("1 ★ (%d)", ratingCounts[0])
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }
    }
}