package com.example.planperfect.data.api

import com.example.planperfect.data.model.CurrencyResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyService {
    @GET("{apiKey}/latest/USD")
    suspend fun getExchangeRates(@Path("apiKey") apiKey: String): CurrencyResponse
}