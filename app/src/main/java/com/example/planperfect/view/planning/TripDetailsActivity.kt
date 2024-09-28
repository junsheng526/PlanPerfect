package com.example.planperfect.view.planning

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.databinding.ActivityTripDetailsBinding
import com.example.planperfect.view.planning.adapter.TripDetailsPagerAdapter
import com.example.planperfect.viewmodel.TripViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class TripDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripDetailsBinding
    private val tripViewModel: TripViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Retrieve tripId from Intent
        val tripId = intent.getStringExtra("tripId") ?: return

        lifecycleScope.launch {
            val trip = tripViewModel.get(tripId)
            if (trip != null) {
                binding.toolbarTitle.text = trip.name
            }
        }

        // Setup ViewPager and TabLayout
        val adapter = TripDetailsPagerAdapter(this, tripId)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Trip Details"
                1 -> "Collaborators"
                else -> null
            }
        }.attach()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }

        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}
