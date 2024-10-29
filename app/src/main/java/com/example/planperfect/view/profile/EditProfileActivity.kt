package com.example.planperfect.view.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.R
import com.example.planperfect.data.api.CountryApiService
import com.example.planperfect.data.model.User
import com.example.planperfect.data.repository.CountryRepository
import com.example.planperfect.databinding.ActivityEditProfileBinding
import com.example.planperfect.utils.cropToBlob
import com.example.planperfect.utils.toBitmap
import com.example.planperfect.view.home.CountryViewModelFactory
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.CountryViewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val vm: AuthViewModel by viewModels()
    private lateinit var countryVm: CountryViewModel
    private val countryMap = mutableMapOf<String, String>()
    private var selectedCountry: String = ""
    private var selectedCurrencyCode: String = ""

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            binding.imgProfile.setImageURI(it.data?.data)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        binding.cancelBtn.setOnClickListener {
            onBackPressed()
        }

        binding.imgProfileBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            launcher.launch(intent)
        }

        binding.saveBtn.setOnClickListener {
            submit()
        }

        // Initialize CountryViewModel with Retrofit
        val apiService = Retrofit.Builder()
            .baseUrl("https://restcountries.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountryApiService::class.java)
        val repository = CountryRepository(apiService)
        val factory = CountryViewModelFactory(repository)
        countryVm = ViewModelProvider(this, factory).get(CountryViewModel::class.java)

        // Observe country data and set up spinner
        setupCountrySpinner()

        val userId = vm.getCurrentUserId()
        if (!userId.isNullOrBlank()) {
            lifecycleScope.launch {
                val user = vm.get(userId)
                user?.let {
                    populateUserData(it)
                    setupObservers(it.country)
                }
            }
        }
    }

    private fun submit() {
        if (!validateInputs()) return
        val user = User(
            name = binding.editTextName.text.toString().trim(),
            phoneNumber = binding.editTextPhoneNumber.text.toString().trim(),
            email = binding.editTextEmail.text.toString().trim(),
            country = selectedCountry,
            currencyCode = selectedCurrencyCode,
            photo = binding.imgProfile.cropToBlob(300, 300),
        )

        lifecycleScope.launch {
            val err = vm.validate(user)
            if (err.isNotEmpty()) {
                AlertDialog.Builder(this@EditProfileActivity)
                    .setIcon(R.drawable.ic_error_icon)
                    .setTitle("Error")
                    .setMessage(err)
                    .setPositiveButton("Dismiss", null)
                    .show()
                return@launch
            }

            // Update user fields in Firestore
            val updated = vm.update(user)
            if (updated) {
                Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                onBackPressed() // Navigate back
            } else {
                Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
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

    private fun populateUserData(user: User) {
        with(binding) {
            editTextName.setText(user.name)
            editTextPhoneNumber.setText(user.phoneNumber)
            editTextEmail.setText(user.email)

            if (user.photo.toBitmap() != null) {
                // Set the user's photo if it's not null
                binding.imgProfile.setImageBitmap(user.photo.toBitmap())
            }
            else{
                binding.imgProfile.setImageResource(R.drawable.profile_bg)
            }
        }
    }

    private fun setupCountrySpinner() {
        val countryAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf())
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCountry.adapter = countryAdapter
    }

    private fun setupObservers(country: String) {
        countryVm.countries.observe(this) { countries ->
            val countryNames = mutableListOf<String>()
            countries?.forEach { country ->
                country.name.common?.let { countryName ->
                    countryNames.add(countryName)
                    countryMap[countryName] = country.currencies?.keys?.firstOrNull() ?: ""
                }
            }
            countryNames.sort()

            val countryAdapter = binding.spinnerCountry.adapter as ArrayAdapter<String>
            countryAdapter.clear()
            countryAdapter.addAll(countryNames)

            // Once the adapter is populated, set the selected country
            country?.let {
                val countryIndex = countryAdapter.getPosition(country)
                if (countryIndex >= 0) {
                    binding.spinnerCountry.setSelection(countryIndex)
                }
            }
        }
        countryVm.fetchCountries()
    }

    private fun validateInputs(): Boolean {
        val name = binding.editTextName.text.toString().trim()
        val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val country = selectedCountry
        val photo = binding.imgProfile.drawable

        if (name.isEmpty()) {
            binding.editTextName.error = "Name cannot be empty"
            return false
        }

        if (phoneNumber.isEmpty() || !phoneNumber.matches(Regex("^[+]?[0-9]{10,13}\$"))) {
            binding.editTextPhoneNumber.error = "Enter a valid phone number"
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Enter a valid email address"
            return false
        }

        if (country.isEmpty()) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show()
            return false
        }

//        if (photo == null) {
//            Toast.makeText(this, "Please select a profile photo", Toast.LENGTH_SHORT).show()
//            return false
//        }

        return true
    }
}