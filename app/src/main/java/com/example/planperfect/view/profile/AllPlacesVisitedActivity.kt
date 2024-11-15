package com.example.planperfect.view.profile

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.ActivityAllPlacesVisitedBinding
import com.example.planperfect.databinding.ActivityCollaborationInvitationBinding
import com.example.planperfect.view.planning.adapter.PlacesAdapter
import com.example.planperfect.viewmodel.CollaboratorViewModel

class AllPlacesVisitedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllPlacesVisitedBinding
    private lateinit var placesAdapter: PlacesVisitedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllPlacesVisitedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

//        val placesList: ArrayList<TouristPlace>? = intent.getParcelableArrayListExtra("placesList")
        val placesList = intent.getParcelableArrayListExtra<TouristPlace>("placesList")
        Log.d("VisitedPlaceActivity placesList-> ", placesList.toString())
//        val placesList = intent.getSerializableExtra("placesList") as? ArrayList<TouristPlace>

        // Set up the RecyclerView
        binding.itineraryRecyclerView.layoutManager = LinearLayoutManager(this)
        placesAdapter = PlacesVisitedAdapter(placesList ?: emptyList())
        binding.itineraryRecyclerView.adapter = placesAdapter
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