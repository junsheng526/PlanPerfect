package com.example.planperfect.data.model

data class CategoryRequest(
    val categories: List<String>,
    val description: String // Add this line to include description
)