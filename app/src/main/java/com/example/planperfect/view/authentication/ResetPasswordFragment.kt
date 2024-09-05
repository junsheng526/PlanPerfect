package com.example.planperfect.view.authentication

import android.app.Dialog
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.planperfect.R
import com.example.planperfect.databinding.FragmentResetPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ResetPasswordFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentResetPasswordBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)

        auth = Firebase.auth

        binding.resetBtn.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            if (validateInputFields()) {
                auth.sendPasswordResetEmail(email).addOnSuccessListener { _ ->
                    showSuccessDialog()
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Send verification email failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return binding.root
    }

    private fun showSuccessDialog() {
        // Create a custom dialog
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_send_verification)

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_dialog)

        // Set up dialog components
        val btnLogin = dialog.findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            // Navigate to the login screen
            dialog.dismiss()
            nav.navigate(R.id.loginFragment) // Assuming you have a loginFragment in the navigation
        }

        // Show the dialog
        dialog.show()
    }

    private fun validateInputFields(): Boolean {
        val email = binding.editTextEmail.text.toString()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tvEmailErrorView.visibility = View.VISIBLE
            return false
        }else{
            binding.tvEmailErrorView.visibility = View.GONE
        }
        return true
    }
}