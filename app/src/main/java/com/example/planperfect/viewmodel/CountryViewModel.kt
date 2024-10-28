package com.example.planperfect.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planperfect.data.model.Country
import com.example.planperfect.data.repository.CountryRepository
import kotlinx.coroutines.launch


class CountryViewModel(private val repository: CountryRepository) : ViewModel() {

    private val _countries = MutableLiveData<List<Country>?>()
    val countries: MutableLiveData<List<Country>?> get() = _countries

    fun fetchCountries() {
        repository.getAllCountries { countryList ->
            _countries.value = countryList
        }
    }
}