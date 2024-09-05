package com.example.planperfect.data.model

data class Country(
    val name: Name
) {
    data class Name(
        val common: String
    )
}