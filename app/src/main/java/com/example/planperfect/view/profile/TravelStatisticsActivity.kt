package com.example.planperfect.view.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.planperfect.databinding.ActivityTravelStatisticsBinding
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.TripViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TravelStatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTravelStatisticsBinding
    private lateinit var tabAdapter: StatisticsPagerAdapter
    private lateinit var tripViewModel: TripViewModel
    private lateinit var authViewModel: AuthViewModel
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTravelStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        tripViewModel = ViewModelProvider(this).get(TripViewModel::class.java)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        userId = authViewModel.getCurrentUserId()

        // Observe trips and calculate statistics in a coroutine
        tripViewModel.trips.observe(this) { tripList ->
            // Launch a coroutine to get user trip years
            userId?.let { currentUserId ->
                CoroutineScope(Dispatchers.IO).launch {
                    val uniqueYears = tripViewModel.getUserTripYears(currentUserId) // Fetch unique years
                    withContext(Dispatchers.Main) {
                        // Update UI on the main thread
                        val yearsList = mutableListOf("All") // Add "All" tab
                        uniqueYears?.let { yearsList.addAll(it) }

                        tabAdapter = StatisticsPagerAdapter(this@TravelStatisticsActivity, yearsList)
                        binding.viewPager.adapter = tabAdapter

                        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                            tab.text = yearsList[position]
                        }.attach()
                    }
                }
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

    private inner class StatisticsPagerAdapter(activity: AppCompatActivity, private val years: List<String>) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = years.size

        override fun createFragment(position: Int): Fragment {
            val fragment = StatisticsFragment()
            val bundle = Bundle().apply {
                putString("year", years[position])
            }
            fragment.arguments = bundle

            // Only calculate statistics for years other than "All"
            if (years[position] != "All") {
                userId?.let { tripViewModel.calculateStatisticsForYear(years[position]) }
            }

            return fragment
        }
    }
}
