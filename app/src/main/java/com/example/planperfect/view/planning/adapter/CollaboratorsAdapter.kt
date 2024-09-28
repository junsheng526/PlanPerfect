package com.example.planperfect.view.planning.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planperfect.data.model.User
import com.example.planperfect.databinding.ItemCollaboratorBinding

class CollaboratorsAdapter(private val collaborators: List<Pair<User, String>>) :
    RecyclerView.Adapter<CollaboratorsAdapter.CollaboratorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollaboratorViewHolder {
        val binding = ItemCollaboratorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollaboratorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CollaboratorViewHolder, position: Int) {
        val (user, role) = collaborators[position]
        holder.bind(user, role)
    }

    override fun getItemCount(): Int = collaborators.size

    inner class CollaboratorViewHolder(private val binding: ItemCollaboratorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, role: String) {
            binding.collaboratorName.text = user.name // Assuming User model has a 'name' field
            binding.collaboratorRole.text = role // Display the role
        }
    }
}
