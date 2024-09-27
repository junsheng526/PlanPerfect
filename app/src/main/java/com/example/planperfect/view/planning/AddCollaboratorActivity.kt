package com.example.planperfect.view.planning

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.planperfect.databinding.ActivityAddCollaboratorBinding
import com.example.planperfect.databinding.FailedModalBinding
import com.example.planperfect.databinding.SuccessModalBinding
import com.example.planperfect.databinding.WarningModalBinding
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
            showConfirmationDialog(
                null,
                "Are you sure you want to invite this person to become a collaborator?"
            ) {
                addCollaborator()
            }
        }

        // Observe collaborator addition status
        collaboratorViewModel.collaboratorAdditionStatus.observe(this) { success ->
            if (success) {
                showSuccessDialog(null, "The collaborator invitation has been sent successfully.")
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
            showErrorDialog(
                null,
                "The user ID provided is invalid. Please re-enter a valid user ID."
            )
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

    private fun showConfirmationDialog(title: String?, description: String, onConfirm: () -> Unit) {
        // Inflate the custom modal layout using View Binding
        val dialogViewBinding = WarningModalBinding.inflate(layoutInflater)

        // Create an AlertDialog
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogViewBinding.root)
            .create()

        if (title == null) {
            dialogViewBinding.modalTitle.visibility = View.GONE
        } else {
            // Customize title and description using View Binding
            dialogViewBinding.modalTitle.text = title
            dialogViewBinding.modalDesc.text = description
        }

        dialogViewBinding.btnConfirm.setOnClickListener {
            onConfirm()
            dialogBuilder.dismiss()
        }

        dialogViewBinding.btnBack.setOnClickListener {
            dialogBuilder.dismiss()
        }

        dialogBuilder.show()
    }

    private fun showSuccessDialog(title: String?, description: String) {
        // Inflate the custom modal layout using View Binding
        val dialogViewBinding = SuccessModalBinding.inflate(layoutInflater)

        // Create an AlertDialog
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogViewBinding.root)
            .create()

        if (title == null) {
            dialogViewBinding.modalTitle.visibility = View.GONE
        } else {
            // Customize title and description using View Binding
            dialogViewBinding.modalTitle.text = title
            dialogViewBinding.modalDesc.text = description
        }


        dialogViewBinding.btnBack.setOnClickListener {
            dialogBuilder.dismiss()
            finish()
        }

        dialogBuilder.show()
    }

    private fun showErrorDialog(title: String?, description: String) {
        // Inflate the custom modal layout using View Binding
        val dialogViewBinding = FailedModalBinding.inflate(layoutInflater)

        // Create an AlertDialog
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogViewBinding.root)
            .create()

        // Customize title and description using View Binding
        dialogViewBinding.modalTitle.text = title
        dialogViewBinding.modalDesc.text = description

        dialogViewBinding.btnBack.setOnClickListener {
            dialogBuilder.dismiss()
        }

        dialogBuilder.show()
    }
}
