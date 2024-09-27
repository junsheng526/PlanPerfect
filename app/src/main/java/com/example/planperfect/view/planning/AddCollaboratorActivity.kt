package com.example.planperfect.view.planning

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.planperfect.R
import com.example.planperfect.databinding.ActivityAddCollaboratorBinding
import com.example.planperfect.databinding.ActivityAddDestinationBinding
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.CollaboratorViewModel

class AddCollaboratorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCollaboratorBinding
    private lateinit var collaboratorViewModel: CollaboratorViewModel
    private lateinit var authViewModel: AuthViewModel
    private var tripId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCollaboratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Initialize the AuthViewModel
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // Create CollaboratorViewModel directly
        collaboratorViewModel = CollaboratorViewModel(authViewModel)

        // Get tripId from the intent or previous activity
        tripId = intent.getStringExtra("tripId") // Make sure this is passed correctly

        // Set up the role spinner
        setupRoleSpinner()

        // Set onClickListener for the button
        binding.addCollaboratorButton.setOnClickListener {
            addCollaborator()
        }

        // Observe collaborator addition status
        collaboratorViewModel.collaboratorAdditionStatus.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Collaborator added successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity on success
            } else {
                Toast.makeText(this, "Failed to add collaborator", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("editor", "viewer") // Add other roles if needed
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.roleSpinner.adapter = adapter
    }

    private fun addCollaborator() {
        val userEmail = binding.userEmailEditText.text.toString().trim()
        val selectedRole = binding.roleSpinner.selectedItem.toString()

        if (userEmail.isNotEmpty() && tripId != null) {
            collaboratorViewModel.addCollaborator(userEmail, selectedRole, tripId!!)
        } else {
            Toast.makeText(this, "Please enter valid data", Toast.LENGTH_SHORT).show()
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

}
