package com.example.planperfect.view.authentication

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.planperfect.R
import com.example.planperfect.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment() {

    private lateinit var binding: FragmentPrivacyPolicyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)

        setupToolbar()
        setupPrivacyPolicy()

        return binding.root
    }

    private fun setupPrivacyPolicy() {
        val privacyPolicyText = """
            1. Introduction
            PlanPerfect is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and share your information when you use our mobile application.
            
            2. Information We Collect
            Personal Information: Name, email, and contact details when you register.
            Usage Data: Device information, location data (via Google Maps API), and app usage patterns.
            Cookies: For tracking and improving user experience.
            
            3. How We Use Your Information
            We use your information to:
            Provide personalized itinerary planning and recommendations.
            Forecast weather conditions using the Weather API.
            Offer currency conversion rates through the OpenExchangeRate API.
            Improve our Service and communicate updates or promotions.
            
            4. Sharing of Information
            We do not sell your personal data. Information may be shared with:
            Service Providers: To operate our Service (e.g., Google Maps, Weather API, OpenExchangeRate).
            Legal Requirements: If required by law or to protect our rights.
            
            5. Data Security
            We implement reasonable measures to protect your information. However, no method of transmission over the Internet or electronic storage is 100% secure.
            
            6. Your Rights
            You have the right to access, correct, or delete your personal information. To exercise these rights, contact us at tehjs-pm21@student.tarc.edu.my.
            
            7. Changes to This Policy
            We may update this Privacy Policy periodically. We will notify you of any significant changes by posting the new policy on our app.
            
            8. Contact Information
            For any questions about this Privacy Policy, please contact us at tehjs-pm21@student.tarc.edu.my.
        """.trimIndent()

        // Set the text to the TextView
        binding.tvPrivacyPolicy.text = privacyPolicyText
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}
