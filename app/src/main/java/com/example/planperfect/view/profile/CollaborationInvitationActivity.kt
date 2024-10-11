package com.example.planperfect.view.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.data.model.Collaborator
import com.example.planperfect.data.model.CollaboratorWithUserDetails
import com.example.planperfect.data.model.Trip
import com.example.planperfect.databinding.ActivityCollaborationInvitationBinding
import com.example.planperfect.databinding.FailedModalBinding
import com.example.planperfect.databinding.SuccessModalBinding
import com.example.planperfect.databinding.WarningModalBinding
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.CollaboratorViewModel

class CollaborationInvitationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCollaborationInvitationBinding
    private val authVm: AuthViewModel by viewModels() // Use the default ViewModelProvider
    private val collaboratorVm: CollaboratorViewModel by viewModels {
        CollaboratorViewModelFactory(authVm) // Pass AuthViewModel to the factory
    }
    private lateinit var userId: String
    private lateinit var adapter: CollaboratorInvitationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollaborationInvitationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        userId = authVm.getCurrentUserId()?.toString() ?: return

        // Initialize RecyclerView
        binding.recyclerViewInvitations.layoutManager = LinearLayoutManager(this)

        // Fetch pending invitations for the current user
        if (userId.isNotEmpty()) {
            collaboratorVm.getPendingInvitations(userId)
        } else {
            Log.e("CollaborationInvitationActivity", "User ID is null or empty")
        }

        // Observe LiveData for pending invitations
        collaboratorVm.pendingInvitationsLiveData.observe(this) { invitations ->
            Log.d("CollaborationInvitationActivity", "Received invitations: $invitations")
            updateInvitationsList(invitations)
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

    private fun updateInvitationsList(invitations: List<Pair<Trip, CollaboratorWithUserDetails>>) {
        if (invitations.isNotEmpty()) {
            adapter = CollaboratorInvitationAdapter(invitations) { trip, action ->
                handleInvitationAction(trip.id, action)
            }
            binding.recyclerViewInvitations.adapter = adapter
            Log.d("Invitations", "Updated adapter with ${invitations.size} invitations.")
        } else {
            // Handle the case where there are no invitations
            Log.d("Invitations", "No invitations found.")
            // You may want to show an empty state message or layout here
        }
    }

    private fun handleInvitationAction(tripId: String, action: String) {
        when (action) {
            "accept" -> {
                collaboratorVm.updateCollaborationStatus(tripId, userId, "accept")
                Log.d("Invitations", "Accepted invitation for tripId: $tripId")
            }
            "reject" -> {
                collaboratorVm.updateCollaborationStatus(tripId, userId, "reject")
                Log.d("Invitations", "Rejected invitation for tripId: $tripId")
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
}
