package com.example.planperfect.data.api

import com.example.planperfect.data.model.DirectionsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenRouteServiceApi {
    @GET("v2/directions/driving-car")
    fun getDirections(
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("waypoints") waypoints: String? = null,
        @Header("Authorization") apiKey: String
    ): Call<DirectionsResponse>
}