package com.example.planperfect.data.api

import com.example.planperfect.data.model.CategoryRequest
import com.example.planperfect.data.model.Recommendation
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/recommend")
    fun getRecommendations(@Body request: CategoryRequest): Call<List<Recommendation>>
}