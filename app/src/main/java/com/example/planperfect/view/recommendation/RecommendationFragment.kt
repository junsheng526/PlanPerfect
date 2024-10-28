package com.example.planperfect.view.recommendation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.R
import com.example.planperfect.data.api.ApiService
import com.example.planperfect.data.model.CategoryRequest
import com.example.planperfect.data.model.Recommendation
import com.example.planperfect.data.model.User
import com.example.planperfect.databinding.FragmentRecommendationBinding
import com.example.planperfect.utils.toBitmap
import com.example.planperfect.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecommendationFragment : Fragment() {

    private lateinit var binding: FragmentRecommendationBinding
    private val selectedCategories = mutableListOf<String>()
    private lateinit var apiService: ApiService
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecommendationBinding.inflate(inflater, container, false)

        setupCardViewListeners()
        setupNavigateButton()

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000") // Update with your server's IP
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
        loadUserData()
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

    private fun setupCardViewListeners() {
        val categoryCards = mapOf(
            binding.cardEcoTourism to Pair(binding.checkEcoTourism, "Eco-Tourism"),
            binding.cardNatureTourism to Pair(binding.checkNatureTourism, "Nature Tourism"),
            binding.cardReligiousTourism to Pair(binding.checkReligiousTourism, "Religious Tourism"),
            binding.cardAdventureTourism to Pair(binding.checkAdventureTourism, "Adventure Tourism"),
            binding.cardRuralTourism to Pair(binding.checkRuralTourism, "Rural Tourism"),
            binding.cardGastronomicTourism to Pair(binding.checkGastronomicTourism, "Gastronomic Tourism"),
            binding.cardBeachTourism to Pair(binding.checkBeachTourism, "Beach Tourism"),
            binding.cardOtherTourism to Pair(binding.checkOtherTourism, "Other Tourism")
        )

        for ((card, pair) in categoryCards) {
            val (checkBox, category) = pair

            // CardView click listener
            card.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
                Log.d("Category Selection", "Card clicked: $category, Checked: ${checkBox.isChecked}")
                handleCheckboxSelection(category, checkBox.isChecked)
                Log.d("Selected Categories", selectedCategories.joinToString(", "))
            }

            // CheckBox click listener
            checkBox.setOnClickListener {
                Log.d("Checkbox Selection", "Checkbox clicked: $category, Checked: ${checkBox.isChecked}")
                handleCheckboxSelection(category, checkBox.isChecked)
                Log.d("Selected Categories", selectedCategories.joinToString(", "))
            }
        }
    }

    private fun handleCheckboxSelection(category: String, isChecked: Boolean) {
        if (isChecked) {
            selectedCategories.add(category)
        } else {
            selectedCategories.remove(category)
        }
    }

    private fun setupNavigateButton() {
        binding.btnNavigate.setOnClickListener {
            // Check if either categories are selected or description is not empty
            if (selectedCategories.isNotEmpty() || binding.editDescription.text.isNotEmpty()) {
                runModelInference()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select at least one category or provide a description.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun runModelInference() {
        // Get the description input from the EditText
        val description = binding.editDescription.text.toString()
        val request = CategoryRequest(selectedCategories, description)

        Log.d("RecommendationFragment RequestDO", request.toString())

        apiService.getRecommendations(request).enqueue(object : Callback<List<Recommendation>> {
            override fun onResponse(
                call: Call<List<Recommendation>>,
                response: Response<List<Recommendation>>
            ) {
                if (response.isSuccessful) {
                    val recommendations = response.body()
                    recommendations?.let {
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
        val intent = Intent(requireActivity(), RecommendationActivity::class.java)
        intent.putParcelableArrayListExtra("recommendations", ArrayList(recommendations))
        startActivity(intent)

        Log.d("RecommendationFragment", "Navigating with recommendations: ${recommendations.size} found")
    }
}
