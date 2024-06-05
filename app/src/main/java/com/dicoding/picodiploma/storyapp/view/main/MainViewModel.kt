package com.dicoding.picodiploma.storyapp.view.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.storyapp.data.UserRepository
import com.dicoding.picodiploma.storyapp.data.api.response.ListStoryItem
import com.dicoding.picodiploma.storyapp.data.pref.UserModel
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository) : ViewModel() {

    private val _stories = MutableLiveData<List<ListStoryItem>>()
    val stories: LiveData<List<ListStoryItem>> = _stories

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun getStories(token: String){
        viewModelScope.launch {
            try {
                val stories = repository.getStories(token)
                _stories.value = stories ?: emptyList()
            } catch (e: Exception) {
                _stories.value = emptyList()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

}