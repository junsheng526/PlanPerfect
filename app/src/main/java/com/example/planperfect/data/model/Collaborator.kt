package com.example.planperfect.data.model

data class Collaborator(
    val userId: String,
    val role: String, // "owner", "editor", or "viewer"
    val status: String?, // "pending", "accept", "reject"
)