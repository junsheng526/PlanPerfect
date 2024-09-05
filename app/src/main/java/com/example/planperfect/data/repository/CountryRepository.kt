package com.example.planperfect.data.repository

import com.example.planperfect.data.api.RetrofitInstance
import com.example.planperfect.data.model.Country
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CountryRepository {

    private val apiService = RetrofitInstance.api

    fun getAllCountries(
        onSuccess: (List<String>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        apiService.getAllCountries().enqueue(object : Callback<List<Country>> {
            override fun onResponse(call: Call<List<Country>>, response: Response<List<Country>>) {
                if (response.isSuccessful) {
                    val countries = response.body()?.map { it.name.common } ?: emptyList()
                    onSuccess(countries)
                } else {
                    onError(Throwable("Error fetching countries"))
                }
            }

            override fun onFailure(call: Call<List<Country>>, t: Throwable) {
                onError(t)
            }
        })
    }
}