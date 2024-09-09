package com.example.planperfect.data.api

import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL_COUNTRY = "https://restcountries.com/"

    // Retrofit instance for Country API
    val countryApi: CountryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_COUNTRY)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountryApiService::class.java)
    }
}
