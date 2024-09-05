package com.example.planperfect.view.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.planperfect.R
import com.example.planperfect.databinding.FragmentLoginBinding
import com.example.planperfect.data.model.User
import com.example.planperfect.view.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding
    private val nav by lazy { findNavController() }
    private val user = Firebase.firestore.collection("user")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.start_color_gradient)

        auth = Firebase.auth

        binding.toRegisterBtn.setOnClickListener {
            nav.navigate(R.id.registerFragment)
        }

        binding.tvForgotPassword.setOnClickListener {
            nav.navigate(R.id.resetPasswordFragment)
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val pwd = binding.editTextPassword.text.toString()
            if (validateInputFields()) {
                auth.signInWithEmailAndPassword(email, pwd).addOnSuccessListener { authResult ->
                    // Fetch user data from FireStore based on the authenticated user's email
                    fetchUserAndNavigate(authResult.user?.uid ?: "")
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Login failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return binding.root
    }

    private fun fetchUserAndNavigate(userId: String) {
        user.document(userId).get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject<User>()
            if (user != null) {
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(
                    requireContext(),
                    "User data not found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(
                requireContext(),
                "Failed to fetch user data: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun validateInputFields(): Boolean {
        val email = binding.editTextEmail.text.toString()
        val pwd = binding.editTextPassword.text.toString()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Invalid email format!"
            return false
        }
        if (pwd.length < 8) {
            binding.editTextPassword.error = "Password should be at least 8 characters long!"
            return false
        }
        return true
    }
}