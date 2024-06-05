package com.dicoding.picodiploma.storyapp.data

import com.dicoding.picodiploma.storyapp.data.api.response.AddStoryResponse
import com.dicoding.picodiploma.storyapp.data.api.response.DetailStoryResponse
import com.dicoding.picodiploma.storyapp.data.api.services.ApiService
import com.dicoding.picodiploma.storyapp.data.api.response.ListStoryItem
import com.dicoding.picodiploma.storyapp.data.api.response.LoginResponse
import com.dicoding.picodiploma.storyapp.data.api.response.RegisterResponse
import com.dicoding.picodiploma.storyapp.data.pref.UserModel
import com.dicoding.picodiploma.storyapp.data.pref.UserPreference
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference

) {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun getStories(token: String): List<ListStoryItem>? {
        val response = apiService.getStory()
        return if (!response.error!!) response.story else null
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return apiService.login(email, password)
    }

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    suspend fun addStory(file: MultipartBody.Part, description: RequestBody): AddStoryResponse {
        return apiService.addStory(file, description)
    }

    suspend fun getStoryDetails(id: String): DetailStoryResponse {
        return apiService.detailStory(id)
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(apiService: ApiService, userPreference: UserPreference) = UserRepository(apiService, userPreference)

    }
}