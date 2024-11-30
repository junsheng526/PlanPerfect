package com.example.planperfect.view.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.planperfect.data.repository.CountryRepository
import com.example.planperfect.viewmodel.CountryViewModel

class CountryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CountryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CountryViewModel(CountryRepository(), context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}