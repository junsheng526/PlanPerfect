package com.example.planperfect.view.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.planperfect.R
import com.example.planperfect.data.model.Collaborator
import com.example.planperfect.data.model.CollaboratorWithUserDetails
import com.example.planperfect.data.model.Trip
import com.example.planperfect.databinding.FailedModalBinding
import com.example.planperfect.databinding.ItemCollaborationInvitationBinding
import com.example.planperfect.databinding.SuccessModalBinding
import com.example.planperfect.databinding.WarningModalBinding
import com.example.planperfect.utils.toBitmap

class CollaboratorInvitationAdapter(
    private val invitations: List<Pair<Trip, CollaboratorWithUserDetails>>,
    private val onActionClick: (Trip, String) -> Unit
) : RecyclerView.Adapter<CollaboratorInvitationAdapter.InvitationViewHolder>() {

    class InvitationViewHolder(val binding: ItemCollaborationInvitationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val binding = ItemCollaborationInvitationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InvitationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        val (trip, collaboratorWithUserDetails) = invitations[position]

        val invitationMsg = "${collaboratorWithUserDetails.user?.name ?: collaboratorWithUserDetails.collaborator.userId} " +
                "has invited you to join the itinerary named ${trip.name}. The travel duration is from ${trip.startDate} until ${trip.endDate}. Please review the details and confirm your participation."

        holder.binding.invitationMsg.text = invitationMsg
        holder.binding.collaboratorName.text = collaboratorWithUserDetails.user?.name ?: "Unknown"
        holder.binding.imageViewProfile

        if (collaboratorWithUserDetails.user?.photo?.toBitmap() != null) {
            holder.binding.imageViewProfile.setImageBitmap(collaboratorWithUserDetails.user.photo.toBitmap())
            holder.binding.letterOverlay.visibility = View.GONE
        } else {
            holder.binding.imageViewProfile.setImageResource(R.drawable.profile_bg)
            holder.binding.letterOverlay.visibility = View.VISIBLE

            val firstLetter = collaboratorWithUserDetails.user?.name?.firstOrNull()?.toString()?.uppercase() ?: "U"
            holder.binding.letterOverlay.text = firstLetter
        }

//        holder.binding.acceptBtn.setOnClickListener {
//            holder.binding.acceptBtn.isEnabled = false // Disable button after click
//            holder.binding.rejectBtn.isEnabled = false // Disable other button
//            onActionClick(trip, "accept")
//        }
//
//        holder.binding.rejectBtn.setOnClickListener {
//            holder.binding.acceptBtn.isEnabled = false // Disable other button
//            holder.binding.rejectBtn.isEnabled = false // Disable button after click
//            onActionClick(trip, "reject")
//        }

        holder.binding.acceptBtn.setOnClickListener {
            showConfirmationDialog(holder.itemView.context, "Accept Invitation", "Are you sure you want to accept this invitation?") {
                onActionClick(trip, "accept")
                showSuccessDialog(holder.itemView.context, "Success", "You have successfully accepted the invitation.")
            }
        }

        holder.binding.rejectBtn.setOnClickListener {
            showConfirmationDialog(holder.itemView.context, "Reject Invitation", "Are you sure you want to reject this invitation?") {
                onActionClick(trip, "reject")
                showErrorDialog(holder.itemView.context, "Invitation Rejected", "You have rejected the invitation.")
            }
        }
    }

    // Confirmation Dialog
    private fun showConfirmationDialog(context: Context, title: String?, description: String, onConfirm: () -> Unit) {
        val dialogViewBinding = WarningModalBinding.inflate(LayoutInflater.from(context))

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogViewBinding.root)
            .create()

        dialogViewBinding.modalTitle.text = title
        dialogViewBinding.modalDesc.text = description

        dialogViewBinding.btnConfirm.setOnClickListener {
            onConfirm()
            dialogBuilder.dismiss()
        }

        dialogViewBinding.btnBack.setOnClickListener {
            dialogBuilder.dismiss()
        }

        dialogBuilder.show()
    }

    // Success Dialog
    private fun showSuccessDialog(context: Context, title: String?, description: String) {
        val dialogViewBinding = SuccessModalBinding.inflate(LayoutInflater.from(context))

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogViewBinding.root)
            .create()

        dialogViewBinding.modalTitle.text = title
        dialogViewBinding.modalDesc.text = description

        dialogViewBinding.btnBack.setOnClickListener {
            dialogBuilder.dismiss()
        }

        dialogBuilder.show()
    }

    // Error Dialog
    private fun showErrorDialog(context: Context, title: String?, description: String) {
        val dialogViewBinding = FailedModalBinding.inflate(LayoutInflater.from(context))

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogViewBinding.root)
            .create()

        dialogViewBinding.modalTitle.text = title
        dialogViewBinding.modalDesc.text = description

        dialogViewBinding.btnBack.setOnClickListener {
            dialogBuilder.dismiss()
        }

        dialogBuilder.show()
    }

    override fun getItemCount() = invitations.size
}