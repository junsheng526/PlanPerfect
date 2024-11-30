package com.example.planperfect.data.repository

import com.example.planperfect.data.api.CountryApiService
import com.example.planperfect.data.api.RetrofitInstance
import com.example.planperfect.data.model.Country
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CountryRepository() {

    private val apiService = RetrofitInstance.countryApi

    fun getAllCountries(callback: (List<Country>?) -> Unit) {
        apiService.getAllCountries().enqueue(object : Callback<List<Country>> {
            override fun onResponse(call: Call<List<Country>>, response: Response<List<Country>>) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<Country>>, t: Throwable) {
                println("Error: ${t.message}")
                callback(null)
            }
        })
    }
}