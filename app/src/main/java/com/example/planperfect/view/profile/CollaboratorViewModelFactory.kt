package com.example.planperfect.view.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.CollaboratorViewModel

class CollaboratorViewModelFactory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollaboratorViewModel::class.java)) {
            return CollaboratorViewModel(authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
