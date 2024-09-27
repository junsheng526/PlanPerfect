package com.example.planperfect.view.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.planperfect.R
import com.example.planperfect.databinding.FragmentProfileBinding
import com.example.planperfect.view.authentication.AuthenticationActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    // ViewBinding variable
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the logout button click listener
        binding.logoutButton.setOnClickListener {
            // Perform logout
            FirebaseAuth.getInstance().signOut()

            // Redirect the user to the login screen (assuming you have a LoginActivity)
            val intent = Intent(activity, AuthenticationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding
    }
}
