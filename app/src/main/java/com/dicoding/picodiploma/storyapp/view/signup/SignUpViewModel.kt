package com.dicoding.picodiploma.storyapp.view.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.storyapp.data.UserRepository
import com.dicoding.picodiploma.storyapp.data.api.response.RegisterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpViewModel(private val repository: UserRepository) : ViewModel() {

    fun register(name: String, email: String, password: String, callback: (RegisterResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.register(name, email, password)
                withContext(Dispatchers.Main) {
                    callback(response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(RegisterResponse(error = true, message = e.localizedMessage))
                }
            }
        }
    }
}