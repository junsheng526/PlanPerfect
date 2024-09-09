package com.example.planperfect.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.R
import com.example.planperfect.databinding.ActivityLandingBinding
import com.example.planperfect.view.authentication.AuthenticationActivity
import com.example.planperfect.viewmodel.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("user")
    private val vm: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        auth = Firebase.auth
        setContentView(binding.root)

        lifecycleScope.launch {
            checkUser()
        }
    }

    private suspend fun checkUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            Log.d("LandingActivity", "Current user UID: $uid")

            // Fetch the user from Firestore using ViewModel
            val user = vm.get(uid)
            if (user != null) {
                // User found in Firestore
                Log.d("LandingActivity", "User found: ${user.email}")
                navigateToMainActivity()
            } else {
                // User document not found in Firestore
                Log.d("LandingActivity", "User document not found, navigating to AuthenticationActivity")
                navigateToAuthenticationActivity()
            }
        } else {
            // No current user logged in, navigate to AuthenticationActivity
            Log.d("LandingActivity", "No current user, navigating to AuthenticationActivity")
            navigateToAuthenticationActivity()
        }
    }

    private fun navigateToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToAuthenticationActivity(){
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()
    }
}