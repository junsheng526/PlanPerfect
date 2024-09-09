package com.example.planperfect.view.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.R
import com.example.planperfect.data.model.Tourist
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.FragmentHomeBinding
import com.example.planperfect.viewmodel.PlacesViewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var touristAdapter: TouristAdapter
    private lateinit var filteredAdapter: FilteredAdapter
    private val filteredList = mutableListOf<TouristPlace>()
    private lateinit var placesViewModel: PlacesViewModel
    private var searchQuery: String = "" // Track the current search query

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

        // Set up RecyclerView with TouristAdapter (Horizontal Carousel)
        touristAdapter = TouristAdapter(touristList)
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
            // Only update the filtered list if there's an active search query
            if (searchQuery.isNotEmpty()) {
                filterList(searchQuery)
            }
        }

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
