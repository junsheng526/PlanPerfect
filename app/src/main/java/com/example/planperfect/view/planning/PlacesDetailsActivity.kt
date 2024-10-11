package com.example.planperfect.view.planning

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.R
import com.example.planperfect.data.api.CurrencyApi
import com.example.planperfect.data.api.WeatherApi
import com.example.planperfect.data.model.CurrencyResponse
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.data.model.WeatherResponse
import com.example.planperfect.databinding.ActivityPlacesDetailsBinding
import com.example.planperfect.utils.FavoritesManager
import com.example.planperfect.view.planning.adapter.ImageCarouselAdapter
import com.example.planperfect.viewmodel.PlacesViewModel
import com.example.planperfect.viewmodel.ReviewViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale

class PlacesDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlacesDetailsBinding
    private val placesViewModel: PlacesViewModel by viewModels()
    private lateinit var touristPlaceId: String
    private lateinit var touristPlace: TouristPlace
    private lateinit var favoritesManager: FavoritesManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the TouristPlace ID from the intent
        touristPlace = intent.getParcelableExtra("place")!!
        touristPlaceId = touristPlace.id

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        favoritesManager = FavoritesManager(userId)

        setupToolbar()
        setupImageCarousel()
        setupDetails()
        setupMap()

        // Fetch place details by ID
        fetchPlaceDetails(touristPlaceId)

        binding.loveIcon.setOnClickListener {
            toggleFavorite()
        }

        binding.showAllReviewsButton.setOnClickListener {
            val intent = Intent(this, RatingListActivity::class.java).apply {
                putExtra("placeId", touristPlaceId)  // Pass the placeId here
            }
            startActivity(intent)
        }
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            if (favoritesManager.isFavorite(touristPlace.id)) {
                favoritesManager.removeFavorite(touristPlace.id)
                touristPlace.isFavorite = false
                updateFavoriteStatusInFirestore(false) // Update Firestore
                Toast.makeText(
                    this@PlacesDetailsActivity,
                    "${touristPlace.name} removed from favorites",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                favoritesManager.addFavorite(touristPlace.id)
                touristPlace.isFavorite = true
                updateFavoriteStatusInFirestore(true) // Update Firestore
                Toast.makeText(
                    this@PlacesDetailsActivity,
                    "${touristPlace.name} added to favorites",
                    Toast.LENGTH_SHORT
                ).show()
            }
            updateFavoriteIcon()
        }
    }

    private fun updateFavoriteStatusInFirestore(isFavorite: Boolean) {
        db.collection("touristPlaces").document(touristPlaceId)
            .update("isFavorite", isFavorite)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating favorite status", Toast.LENGTH_SHORT).show()
            }
    }

    private suspend fun updateFavoriteIcon() {
        if (favoritesManager.isFavorite(touristPlace.id)) {
            binding.loveIcon.setImageResource(R.drawable.ic_favourite) // Your filled heart icon
        } else {
            binding.loveIcon.setImageResource(R.drawable.ic_not_favourite) // Your outlined heart icon
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }
    }

    private fun setupImageCarousel() {
        val imageUrls = touristPlace.imageUrls
        val imageCarouselAdapter = ImageCarouselAdapter(imageUrls.toMutableList())

        // Set the adapter to the RecyclerView
        binding.recyclerViewCarousel.adapter = imageCarouselAdapter
        binding.recyclerViewCarousel.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupDetails() {
        // Set description and address
        if (touristPlace.longDescription != null) {
            binding.detailDescription.text = touristPlace.longDescription
        } else {
            binding.detailDescription.text = touristPlace.description
        }
        binding.locationAddress.text =
            "Address: ${getAddressFromLocation(touristPlace.latitude, touristPlace.longitude)}"
    }

    private fun setupMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            // Enable zoom controls
            googleMap.uiSettings.isZoomControlsEnabled = true

            val location = LatLng(touristPlace.latitude ?: 0.0, touristPlace.longitude ?: 0.0)
            googleMap.addMarker(MarkerOptions().position(location).title(touristPlace.name))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        lifecycleScope.launch {
            val place = placesViewModel.getPlaceById(placeId) // Fetch the place details
            if (place != null) {
                touristPlace = place
                // Get the favorite status from Firestore and update the local TouristPlace object
                favoritesManager.isFavorite(touristPlace.id).let { isFav ->
                    touristPlace.isFavorite = isFav
                    updateFavoriteIcon() // Update the favorite icon based on the fetched status
                }
                setupDetails() // Update UI with the new details
                setupImageCarousel() // Update image carousel with new images
                setupMap() // Update map with the new location
                fetchWeatherAndCurrency()
                fetchReviews()
            } else {
                Toast.makeText(this@PlacesDetailsActivity, "Place not found", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getAddressFromLocation(latitude: Double?, longitude: Double?): String? {
        if (latitude == null || longitude == null) return "Address not available"

        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                // Return the first address
                addresses[0].getAddressLine(0)
            } else {
                "Address not found"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unable to get address"
        }
    }

    private fun fetchWeatherAndCurrency() {
        val lat = touristPlace.latitude ?: 0.0
        val lon = touristPlace.longitude ?: 0.0
        val location = "$lat,$lon"
        val currencyCode = touristPlace.currencyCode

        // Fetch Weather
        lifecycleScope.launch {
            try {
                val weatherResponse = WeatherApi.retrofitService.getCurrentWeather(
                    "e04165cc595e47c4982125923242609", location
                )
                updateWeatherUI(weatherResponse)
            } catch (e: Exception) {
                Toast.makeText(
                    this@PlacesDetailsActivity,
                    "Error fetching weather",
                    Toast.LENGTH_SHORT
                ).show()
            }

            Log.d("currencyCode", currencyCode!!)

            if (currencyCode != null) {
                fetchCurrencyFromApi(currencyCode)
            } else {
                Toast.makeText(
                    this@PlacesDetailsActivity,
                    "Currency code not available",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun fetchCurrencyFromApi(currencyCode: String) {
        val apiKey = "7f08c78040b9238328364018"
        try {
            // Make the API call using the currency service
            val currencyResponse = CurrencyApi.retrofitService.getExchangeRates(apiKey)

            // Log the entire response for debugging
            Log.d("CurrencyResponse", currencyResponse.toString())

            // Update the UI with the retrieved currency data
            updateCurrencyUI(currencyResponse, currencyCode)
        } catch (e: HttpException) {
            Log.e("APIError", "HTTP error: ${e.code()} - ${e.message()}")
            Toast.makeText(
                this@PlacesDetailsActivity,
                "Error fetching currency",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e("APIError", "Error fetching currency: ${e.message}")
            Toast.makeText(
                this@PlacesDetailsActivity,
                "Error fetching currency",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateWeatherUI(weatherResponse: WeatherResponse) {
        val temperature = weatherResponse.current.temp_c
        binding.weather.text = "Current weather = $temperature°C"
    }

    private fun updateCurrencyUI(currencyResponse: CurrencyResponse, currencyCode: String) {
        val rate = currencyResponse.conversion_rates[currencyCode] ?: 1.0
        binding.currency.text = "1 USD = $rate ${touristPlace.currencyCode}"
    }

    private fun fetchReviews() {
        db.collection("reviews")
            .whereEqualTo("placeId", touristPlaceId)
            .get()
            .addOnSuccessListener { result ->
                val reviewCount = result.size()
                var excellentCount = 0
                var veryGoodCount = 0
                var averageCount = 0
                var poorCount = 0
                var terribleCount = 0

                for (document in result) {
                    val rating = document.getDouble("rating")
                    if (rating != null) {
                        when {
                            rating == 5.0 -> excellentCount++
                            rating >= 4.0 && rating < 5.0 -> veryGoodCount++
                            rating >= 3.0 && rating < 4.0 -> averageCount++
                            rating >= 2.0 && rating < 3.0 -> poorCount++
                            rating >= 1.0 && rating < 2.0 -> terribleCount++
                        }
                    }
                }

                var totalRatingPoints = 0.0

                for (document in result) {
                    val rating = document.getDouble("rating") // Get the rating as a Double
                    if (rating != null) {
                        totalRatingPoints += rating // Add the rating directly to total points
                    }
                }

                // Update the review count
                updateReviewCount(reviewCount)

                // Initialize progress bar maximum values
                initializeProgressBars(reviewCount)

                val averageRating = if (reviewCount > 0) {
                    totalRatingPoints.toFloat() / reviewCount
                } else {
                    0f // Set to 0 if there are no reviews
                }

                val formattedAverageRating = String.format("%.1f", averageRating)

                // Update RatingBar with the calculated average rating
                binding.ratingBar.rating = formattedAverageRating.toFloat()
                binding.averageRating.text = formattedAverageRating

                // Check if reviewCount is greater than zero to avoid division by zero
                if (reviewCount > 0) {

                    // Update progress bars with the actual counts
                    updateProgressBars(
                        excellentCount,
                        veryGoodCount,
                        averageCount,
                        poorCount,
                        terribleCount
                    )

                    // Update rating counts
                    updateRatingCounts(
                        excellentCount,
                        veryGoodCount,
                        averageCount,
                        poorCount,
                        terribleCount
                    )
                } else {
                    // Handle the case when there are no reviews
                    updateProgressBars(0, 0, 0, 0, 0) // Reset progress bars
                    updateRatingCounts(0, 0, 0, 0, 0)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FetchReviews", "Error getting documents: ", exception)
                Toast.makeText(this, "Error fetching reviews", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateReviewCount(count: Int) {
        binding.reviewCount.text = "$count reviews"
    }

    private fun updateProgressBars(
        excellent: Int,
        veryGood: Int,
        average: Int,
        poor: Int,
        terrible: Int
    ) {
        binding.progressExcellent.progress = excellent
        binding.progressVeryGood.progress = veryGood
        binding.progressAverage.progress = average
        binding.progressPoor.progress = poor
        binding.progressTerrible.progress = terrible
    }

    private fun initializeProgressBars(maxCount: Int) {
        // Set maximum value for all progress bars
        binding.progressExcellent.max = maxCount
        binding.progressVeryGood.max = maxCount
        binding.progressAverage.max = maxCount
        binding.progressPoor.max = maxCount
        binding.progressTerrible.max = maxCount
    }

    private fun updateRatingCounts(
        excellentCount: Int,
        veryGoodCount: Int,
        averageCount: Int,
        poorCount: Int,
        terribleCount: Int
    ) {
        binding.progressExcellent.progress = excellentCount
        binding.excellentCount.text = excellentCount.toString()

        binding.progressVeryGood.progress = veryGoodCount
        binding.veryGoodCount.text = veryGoodCount.toString()

        binding.progressAverage.progress = averageCount
        binding.averageCount.text = averageCount.toString()

        binding.progressPoor.progress = poorCount
        binding.poorCount.text = poorCount.toString()

        binding.progressTerrible.progress = terribleCount
        binding.terribleCount.text = terribleCount.toString()
    }
}
