package com.example.planperfect.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planperfect.data.repository.CountryRepository
import kotlinx.coroutines.launch

class CountryViewModel : ViewModel() {

    private val countryRepository = CountryRepository()

    private val _countries = MutableLiveData<List<String>>()
    val countries: LiveData<List<String>> get() = _countries

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchCountries() {
        viewModelScope.launch {
            countryRepository.getAllCountries(
                onSuccess = { countryList ->
                    _countries.value = countryList
                },
                onError = { throwable ->
                    _error.value = throwable.message
                }
            )
        }
    }
}