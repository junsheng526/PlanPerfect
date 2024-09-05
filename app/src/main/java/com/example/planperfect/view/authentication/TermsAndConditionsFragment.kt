package com.example.planperfect.view.authentication

import android.content.Intent
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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.planperfect.R
import com.example.planperfect.databinding.FragmentTermsAndConditionsBinding

class TermsAndConditionsFragment : Fragment() {

    private lateinit var binding: FragmentTermsAndConditionsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTermsAndConditionsBinding.inflate(inflater, container, false)

        setupToolbar()
        setupTermsAndConditions()

        return binding.root
    }

    private fun setupTermsAndConditions() {
        val termsAndConditionsText = """
            1. Introduction
            Welcome to PlanPerfect, a smart itinerary planning and recommendation system. By using this mobile application, you agree to these Terms and Conditions. If you do not agree, please do not use the Service.
            
            2. Use of Service
            PlanPerfect provides itinerary planning, weather forecasting, and currency conversion using third-party services like Google Maps API, Weather API and OpenExchangeRate API. The Service is for personal, non-commercial use only. You agree not to misuse the Service, including any unauthorized access or interference.
            
            3. Account Registration
            To use certain features, you must create an account with accurate information. You are responsible for maintaining the confidentiality of your account and password and for any activities that occur under your account.
            
            4. Third-Party Services
            Our Service integrates with third-party APIs (Google Maps, Weather API, OpenExchangeRate). We are not responsible for the content, accuracy, or reliability of third-party services.
            
            5. Limitation of Liability
            PlanPerfect is provided "as is." We are not liable for any direct, indirect, incidental, or consequential damages arising from your use of the Service, including errors in data, weather forecasts, or currency conversions.
            
            6. Changes to the Terms
            We may update these Terms periodically. Changes will be posted on this page, and your continued use of the Service indicates your acceptance of the new Terms.
            
            7. Governing Law
            These Terms are governed by the laws of Malaysia. Any disputes arising out of or in connection with these Terms shall be resolved in the courts of Malaysia.
            
            8. Contact Information
            For questions about these Terms, please contact us at tehjs-pm21@student.tarc.edu.my.
        """.trimIndent()

        val termsAndConditions = SpannableString(termsAndConditionsText)

        // Setting the text to the TextView
        binding.tvTermsAndConditions.text = termsAndConditions
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
