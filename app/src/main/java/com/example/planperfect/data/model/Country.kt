package com.example.planperfect.data.model

data class Country(
    val name: Name,
    val currencies: Map<String, Currency>?
)

data class Name(
    val common: String,
    val official: String
)

data class Currency(
    val name: String,
    val symbol: String?
)