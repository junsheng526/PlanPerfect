package com.example.planperfect.data.api

import com.example.planperfect.data.model.Country
import retrofit2.Call
import retrofit2.http.GET

interface CountryApiService {
    @GET("v3.1/all")
    fun getAllCountries(): Call<List<Country>>
}