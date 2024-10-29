package com.example.planperfect.view.authentication

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.planperfect.R
import com.example.planperfect.data.api.CountryApiService
import com.example.planperfect.databinding.FragmentRegisterBinding
import com.example.planperfect.data.model.User
import com.example.planperfect.data.repository.CountryRepository
import com.example.planperfect.view.home.CountryViewModelFactory
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.CountryViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentRegisterBinding
    private val nav by lazy { findNavController() }
    private val authVm: AuthViewModel by activityViewModels()
    private lateinit var countryVm: CountryViewModel
    private val countryMap = mutableMapOf<String, String>()
    private var selectedCurrencyCode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        setupTermsAndPrivacyText()
        val apiService = Retrofit.Builder()
            .baseUrl("https://restcountries.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountryApiService::class.java)

        // Create repository and ViewModelFactory
        val repository = CountryRepository(apiService)
        val factory = CountryViewModelFactory(repository)

        // Obtain the ViewModel with the custom factory
        countryVm = ViewModelProvider(this, factory).get(CountryViewModel::class.java)
        setupObservers()
        binding.spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCountry = parent.getItemAtPosition(position).toString()
                selectedCurrencyCode = countryMap[selectedCountry]
                selectedCurrencyCode?.let { Log.d("selectedCurrencyCode", it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optionally handle the case where no item is selected
            }
        }

        binding.editTextDOB.setOnClickListener {
            showDatePickerDialog()
        }

        binding.registerBtn.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val pwd = binding.editTextPassword.text.toString()
            if (validateInputFields()) {
//                Toast.makeText(context, "Validate success!", Toast.LENGTH_SHORT).show()
                auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener {
                    if (it.isSuccessful) {
                        saveUserToFireStoreDb()
                    } else {
                        Toast.makeText(context, "Failed to create user!", Toast.LENGTH_SHORT).show()
                        Log.e("Error during creating user >> ", it.exception.toString())
                    }
                }
            }else{
                Toast.makeText(context, "Validate unsuccessful!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setupObservers() {
        // Observe the list of countries
        countryVm.countries.observe(viewLifecycleOwner) { countries ->
            val countryNames = mutableListOf<String>()

            countries?.forEach { country ->
                country.name.common?.let { countryName ->
                    country.currencies?.forEach { (currencyCode, currency) ->
                        countryNames.add(countryName)
                        countryMap[countryName] = currencyCode // Map country to currency code
                    }
                }
            }

            // Sort country names alphabetically
            val sortedCountryNames = countryNames.sorted()

            setupCountrySpinner(sortedCountryNames) // Populate spinner with sorted country names
        }

        // Fetch countries data
        countryVm.fetchCountries()
    }
    private fun setupCountrySpinner(countries: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCountry.adapter = adapter
    }

    private fun showDatePickerDialog() {
        // Get the current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create and show the DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                // Format the selected date and set it to the EditText
                val formattedDate = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear)
                binding.editTextDOB.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun saveUserToFireStoreDb() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val name = binding.editTextName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
        val country = binding.spinnerCountry.selectedItem.toString()
        val dob = binding.editTextDOB.text.toString().trim()

        val userObj = User(
            id = userId,
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            country = country,
            currencyCode = selectedCurrencyCode ?: "",
            dateOfBirth = parseDate(dob)
        )

        lifecycleScope.launch {
            val success: Boolean = authVm.set(userObj)
            if (success) {
                Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                showSuccessDialog()
                // Navigate or do any other action
            } else {
                Toast.makeText(context, "Failed to create user!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun validateInputFields(): Boolean {
        val name = binding.editTextName.text.toString()
        val email = binding.editTextEmail.text.toString()
        val phoneNumber = binding.editTextPhoneNumber.text.toString()
        val dob = binding.editTextDOB.text.toString()
        val password = binding.editTextPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()
        val termsChecked = binding.checkboxTerms.isChecked

        val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}\$".toRegex()

        Log.d("Register Fragment", "Check termsChecked " + termsChecked)

        var isValid = true

        // Validate Name
        if (name.isBlank()) {
            binding.tvNameErrorView.visibility = View.VISIBLE
            binding.tvNameError.text = "Invalid name, Please re-enter."
            isValid = false
        }else{
            binding.tvNameErrorView.visibility = View.GONE
        }

        // Validate Email
        if (email.isBlank()) {
            binding.tvEmailErrorView.visibility = View.VISIBLE
            binding.tvEmailError.text = "Invalid email address, Please re-enter."
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tvEmailErrorView.visibility = View.VISIBLE
            binding.tvEmailError.text = "Invalid email address, Please re-enter."
            isValid = false
        }else{
            binding.tvEmailErrorView.visibility = View.GONE
        }

        // Validate Phone Number
        if (phoneNumber.isBlank()) {
            binding.tvPhoneErrorView.visibility = View.VISIBLE
            binding.tvPhoneError.text = "Invalid phone number, Please re-enter."
            isValid = false
        }else{
            binding.tvPhoneErrorView.visibility = View.GONE
        }

        // Validate Date of Birth
        if (dob.isBlank()) {
            binding.tvDOBErrorView.visibility = View.VISIBLE
            binding.tvDOBError.text = "Please select the date of birth."
            isValid = false
        }else{
            binding.tvDOBErrorView.visibility = View.GONE
        }

        // Validate Password
        if (password.isBlank()) {
            binding.tvPasswordErrorView.visibility = View.VISIBLE
            binding.tvPasswordError.text = "Invalid password, Please re-enter."
            isValid = false
        } else if (!passwordPattern.matches(password)) {
            binding.tvPasswordErrorView.visibility = View.VISIBLE
            binding.tvPasswordError.text = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one special character, and one number."
            isValid = false
        }else{
            binding.tvPasswordErrorView.visibility = View.GONE
        }

        // Validate Confirm Password
        if (confirmPassword.isBlank()) {
            binding.tvConfirmPasswordErrorView.visibility = View.VISIBLE
            binding.tvConfirmPasswordError.text = "Invalid password, Please re-enter."
            isValid = false
        } else if (confirmPassword != password) {
            binding.tvConfirmPasswordErrorView.visibility = View.VISIBLE
            binding.tvConfirmPasswordError.text = "Passwords do not match!"
            isValid = false
        }else{
            binding.tvConfirmPasswordErrorView.visibility = View.GONE
        }

        // Validate Terms Checkbox
        if (!termsChecked) {
            binding.checkboxErrorView.visibility = View.VISIBLE
            binding.tvCheckboxError.text = "Please check the box to accept the Terms and Conditions and Privacy Policy to complete your registration."
            isValid = false
        }else{
            binding.checkboxErrorView.visibility = View.GONE
        }

        return isValid
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("MM/dd/yyyy", Locale.UK).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private fun setupTermsAndPrivacyText() {
        val termsAndConditions = "Terms and Conditions"
        val privacyPolicy = "Privacy Policy"

        val text = "I accept the $termsAndConditions and $privacyPolicy"

        val spannableString = SpannableString(text)

        // Retrieve the custom color
        val linkColor = ContextCompat.getColor(requireContext(), R.color.gray_text_color)

        // Set up clickable spans for "Terms and Conditions"
        val termsStartIndex = text.indexOf(termsAndConditions)
        val termsEndIndex = termsStartIndex + termsAndConditions.length
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to Terms and Conditions Activity
                nav.navigate(R.id.termsAndConditionsFragment)
            }
        }, termsStartIndex, termsEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(linkColor), termsStartIndex, termsEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), termsStartIndex, termsEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set up clickable spans for "Privacy Policy"
        val privacyStartIndex = text.indexOf(privacyPolicy)
        val privacyEndIndex = privacyStartIndex + privacyPolicy.length
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to Privacy Policy Activity
                nav.navigate(R.id.privacyPolicyFragment)
            }
        }, privacyStartIndex, privacyEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(linkColor), privacyStartIndex, privacyEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), privacyStartIndex, privacyEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the spannable string to TextView
        binding.tvTermsAndPrivacy.text = spannableString
        binding.tvTermsAndPrivacy.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showSuccessDialog() {
        // Create a custom dialog
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_registration_success)

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_dialog)

        // Set up dialog components
        val btnBack = dialog.findViewById<Button>(R.id.btnBack)
        val btnLogin = dialog.findViewById<Button>(R.id.btnLogin)

        btnBack.setOnClickListener {
            // Dismiss the dialog and navigate back
            dialog.dismiss()
            nav.navigateUp()
        }

        btnLogin.setOnClickListener {
            // Navigate to the login screen
            dialog.dismiss()
            nav.navigate(R.id.loginFragment) // Assuming you have a loginFragment in the navigation
        }

        // Show the dialog
        dialog.show()
    }
}