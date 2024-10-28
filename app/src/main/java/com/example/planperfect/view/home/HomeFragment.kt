package com.example.planperfect.view.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.R
import com.example.planperfect.data.model.Tourist
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.data.model.User
import com.example.planperfect.databinding.FragmentHomeBinding
import com.example.planperfect.utils.DummyDataUtil
import com.example.planperfect.utils.toBitmap
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.PlacesViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var touristAdapter: TouristAdapter
    private lateinit var filteredAdapter: FilteredAdapter
    private val filteredList = mutableListOf<TouristPlace>()
    private lateinit var placesViewModel: PlacesViewModel
    private var searchQuery: String = ""
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Sample tourist data for the carousel
        val touristList = listOf(
            Tourist(R.drawable.tourist_image_1, "Bali,\nIndonesia"),
            Tourist(R.drawable.tourist_image_2, "Paris,\nFrance"),
            Tourist(R.drawable.tourist_image_3, "Rome,\nItaly"),
            Tourist(R.drawable.tourist_image_4, "Yokohama,\nJapan")
        )

//        lifecycleScope.launch {
//            DummyDataUtil.createDummyMalaysiaTouristPlaces()
//        }

        loadUserData()

        // Set up RecyclerView with TouristAdapter (Horizontal Carousel)
        touristAdapter = TouristAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = touristAdapter

        // Set up RecyclerView for filtered list (Vertical List)
        filteredAdapter = FilteredAdapter(filteredList)
        binding.filteredRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.filteredRecyclerView.adapter = filteredAdapter

        // Initialize PlacesViewModel
        placesViewModel = ViewModelProvider(this).get(PlacesViewModel::class.java)

        // Observe the placesLiveData and update UI when data changes
        placesViewModel.placesLiveData.observe(viewLifecycleOwner) { places ->
            touristAdapter.updateData(places)
            // Only update the filtered list if there's an active search query
            if (searchQuery.isNotEmpty()) {
                filterList(searchQuery)
            }
        }

//        placesViewModel.importTouristPlacesFromCsv(requireContext())

        // Handle search bar text change
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim() // Update the current query
                if (searchQuery.isEmpty()) {
                    // No search query, show home content and hide filtered list
                    binding.contentLayout.visibility = View.VISIBLE
                    binding.filteredRecyclerView.visibility = View.GONE
                    binding.noResultsImage.visibility = View.GONE
                    binding.actionbarTv.text = "Home"
                } else {
                    // There is a search query, show filtered results
                    binding.contentLayout.visibility = View.GONE
                    binding.actionbarTv.text = "Search"
                    filterList(searchQuery)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return binding.root
    }

    private fun loadUserData() {
        val userId = authViewModel.getCurrentUserId()

        if (!userId.isNullOrBlank()) {
            lifecycleScope.launch {
                val user = authViewModel.get(userId) // Fetch user data from ViewModel
                user?.let {
                    populateUserData(it) // Populate the UI with user data
                }
            }
        }
    }

    private fun populateUserData(user: User) {
        binding.apply {
            // Populate profile picture
            if (user.photo.toBitmap() != null) {
                headerProfile.setImageBitmap(user.photo.toBitmap())
                binding.letterOverlayTv.visibility = View.GONE
            } else {
                headerProfile.setImageResource(R.drawable.profile_bg)
                binding.letterOverlayTv.visibility = View.VISIBLE

                val firstLetter = user.name.firstOrNull()?.toString()?.uppercase() ?: "U"
                binding.letterOverlayTv.text = firstLetter
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun filterList(query: String) {
        // Filter places based on query
        val filteredItems = placesViewModel.placesLiveData.value?.filter { place ->
            place.name.contains(query, ignoreCase = true) ||
                    place.category.contains(query, ignoreCase = true)
        } ?: listOf()

        // Update filtered list and notify adapter
        filteredList.clear()
        filteredList.addAll(filteredItems)
        filteredAdapter.notifyDataSetChanged()

        // Show or hide RecyclerView based on the filtered list
        if (filteredItems.isEmpty()) {
            binding.filteredRecyclerView.visibility = View.GONE
            binding.noResultsImage.visibility = View.VISIBLE
        } else {
            binding.filteredRecyclerView.visibility = View.VISIBLE
            binding.noResultsImage.visibility = View.GONE
        }
    }
}
