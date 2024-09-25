package com.example.planperfect.view.planning

import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.ActivityPlacesDetailsBinding
import com.example.planperfect.utils.FavoritesManager
import com.example.planperfect.viewmodel.PlacesViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
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
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            if (favoritesManager.isFavorite(touristPlace.id)) {
                favoritesManager.removeFavorite(touristPlace.id)
                touristPlace.isFavorite = false
                updateFavoriteStatusInFirestore(false) // Update Firestore
                Toast.makeText(this@PlacesDetailsActivity, "${touristPlace.name} removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                favoritesManager.addFavorite(touristPlace.id)
                touristPlace.isFavorite = true
                updateFavoriteStatusInFirestore(true) // Update Firestore
                Toast.makeText(this@PlacesDetailsActivity, "${touristPlace.name} added to favorites", Toast.LENGTH_SHORT).show()
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
}
