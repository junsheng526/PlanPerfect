package com.example.planperfect.view.profile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.databinding.ActivityFavoritePlacesBinding
import com.example.planperfect.utils.FavoritesManager
import com.example.planperfect.viewmodel.PlacesViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FavoritePlacesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritePlacesBinding
    private val placesViewModel: PlacesViewModel by viewModels()
    private lateinit var placesAdapter: FavouritePlacesAdapter
    private lateinit var favoritesManager: FavoritesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritePlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        favoritesManager = FavoritesManager(userId)

        // Initialize the adapter here
        placesAdapter = FavouritePlacesAdapter(favoritesManager)

        // Set up the RecyclerView after initializing the adapter
        setupRecyclerView()

        // Fetch the user's favorite places
        lifecycleScope.launch {
            placesViewModel.getFavoritePlaces(userId)
        }

        // Observe favorite places LiveData and update UI
        placesViewModel.favoritePlacesLiveData.observe(this) { favoritePlaces ->
            if (favoritePlaces != null) {
                placesAdapter.submitList(favoritePlaces)
            }
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

    private fun setupRecyclerView() {
        binding.favoritePlacesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FavoritePlacesActivity)
            adapter = placesAdapter // Adapter is now initialized before this line
        }
    }
}