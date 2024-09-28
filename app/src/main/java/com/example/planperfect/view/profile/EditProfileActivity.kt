package com.example.planperfect.view.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.R
import com.example.planperfect.data.model.User
import com.example.planperfect.databinding.ActivityEditProfileBinding
import com.example.planperfect.utils.cropToBlob
import com.example.planperfect.utils.toBitmap
import com.example.planperfect.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val vm: AuthViewModel by viewModels()

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

        val userId = vm.getCurrentUserId()
        if (!userId.isNullOrBlank()) {
            lifecycleScope.launch {
                val user = vm.get(userId)
                user?.let { populateUserData(it) }
            }
        }
    }

    private fun submit() {
        val user = User(
            name = binding.editTextName.text.toString().trim(),
            phoneNumber = binding.editTextPhoneNumber.text.toString().trim(),
            email = binding.editTextEmail.text.toString().trim(),
            country = binding.editTextCountry.text.toString().trim(),
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
            editTextCountry.setText(user.country)

            if (user.photo.toBitmap() != null) {
                // Set the user's photo if it's not null
                binding.imgProfile.setImageBitmap(user.photo.toBitmap())
            }
            else{
                binding.imgProfile.setImageResource(R.drawable.profile_bg)
            }
        }
    }
}