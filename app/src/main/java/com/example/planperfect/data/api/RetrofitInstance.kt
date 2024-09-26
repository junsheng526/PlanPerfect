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

object WeatherApi {
    private const val BASE_URL = "https://api.weatherapi.com/v1/"

    val retrofitService: WeatherService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }
}

object CurrencyApi {
    private const val BASE_URL = "https://v6.exchangerate-api.com/v6/"

    val retrofitService: CurrencyService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyService::class.java)
    }
}