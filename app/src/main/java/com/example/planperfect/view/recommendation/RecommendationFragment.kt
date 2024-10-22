package com.example.planperfect.view.recommendation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.planperfect.data.api.ApiService
import com.example.planperfect.data.model.CategoryRequest
import com.example.planperfect.data.model.Recommendation
import com.example.planperfect.databinding.FragmentRecommendationBinding
import com.example.planperfect.ml.KnnModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecommendationFragment : Fragment() {

    private lateinit var binding: FragmentRecommendationBinding
    private val selectedCategories = mutableListOf<String>()

    private val allCategories = listOf(
        "Eco-Tourism", "Nature Tourism", "Religious Tourism", "Adventure Tourism",
        "Rural Tourism", "Gastronomic Tourism", "Beach Tourism", "Other"
    )

    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecommendationBinding.inflate(inflater, container, false)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.12:5000") // Update with your server's IP
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Initialize checkboxes
        val checkboxes = listOf(
            binding.checkEcoTourism,
            binding.checkNatureTourism,
            binding.checkReligiousTourism,
            binding.checkAdventureTourism,
            binding.checkRuralTourism,
            binding.checkGastronomicTourism,
            binding.checkBeachTourism,
            binding.checkOther
        )

        // Set listener for each checkbox
        checkboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                handleCheckboxSelection(checkbox, isChecked)
            }
        }

        // Set up the navigation button
        binding.btnNavigate.setOnClickListener {
            if (selectedCategories.isNotEmpty()) {
                runModelInference()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select at least one option",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    private fun handleCheckboxSelection(checkbox: CheckBox, isChecked: Boolean) {
        val category = checkbox.text.toString()

        if (isChecked) {
            if (selectedCategories.size < 3) {
                selectedCategories.add(category)
            } else {
                checkbox.isChecked = false
                Toast.makeText(
                    requireContext(),
                    "You can select up to 3 categories",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            selectedCategories.remove(category)
        }
    }

    private fun runModelInference() {
        val request = CategoryRequest(selectedCategories)

        apiService.getRecommendations(request).enqueue(object : Callback<List<Recommendation>> {
            override fun onResponse(
                call: Call<List<Recommendation>>,
                response: Response<List<Recommendation>>
            ) {
                if (response.isSuccessful) {
                    val recommendations = response.body()
                    recommendations?.let {
                        // Process recommendations (example: show them in a Toast or navigate to another activity)
                        it.forEach { recommendation ->
                            Log.d("RecommendationFragment", "Title: ${recommendation.title}, Description: ${recommendation.description}, Image URL: ${recommendation.image_url}")
                        }
                        // Navigate to a new activity to display recommendations
                        navigateToRecommendationActivity(it)
                    } ?: run {
                        Log.e("RecommendationFragment", "No recommendations found")
                    }
                } else {
                    Log.e("RecommendationFragment", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Recommendation>>, t: Throwable) {
                Log.e("RecommendationFragment", "API call failed: ${t.message}")
            }
        })
    }

    private fun navigateToRecommendationActivity(recommendations: List<Recommendation>) {
        // Implement navigation logic to pass recommendations to the next activity
        // Example:
        // val intent = Intent(requireActivity(), RecommendationActivity::class.java)
        // intent.putParcelableArrayListExtra("recommendations", ArrayList(recommendations))
        // startActivity(intent)

        Log.d("RecommendationFragment", "Navigating with recommendations: ${recommendations.size} found")
        // Navigation logic goes here
    }
}
