package com.example.planperfect.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planperfect.data.model.Country
import com.example.planperfect.data.repository.CountryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader


class CountryViewModel(private val repository: CountryRepository, private val context: Context) : ViewModel() {

    private val _countries = MutableLiveData<List<Country>?>()
    val countries: MutableLiveData<List<Country>?> get() = _countries

    fun fetchCountries() {
        repository.getAllCountries { countryList ->
            val jsonString = readJsonFromAssets("countries.json")
            // Parse the JSON string into a list of Country objects
            val countryList = parseJsonToCountryList(jsonString)
            _countries.value = countryList
            Log.d("CountryViewModel", "CountryList -> ${countryList.toString()}")
        }
    }

    private fun readJsonFromAssets(fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val inputStreamReader = InputStreamReader(inputStream)
        return inputStreamReader.readText()
    }

    private fun parseJsonToCountryList(jsonString: String): List<Country> {
        val gson = Gson()
        val listType = object : TypeToken<List<Country>>() {}.type
        return gson.fromJson(jsonString, listType)
    }
}