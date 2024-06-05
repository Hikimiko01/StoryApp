package com.dicoding.picodiploma.storyapp.data.api.services

import com.dicoding.picodiploma.storyapp.data.api.response.AddStoryResponse
import com.dicoding.picodiploma.storyapp.data.api.response.DetailStoryResponse
import com.dicoding.picodiploma.storyapp.data.api.response.GetStoryResponse
import com.dicoding.picodiploma.storyapp.data.api.response.LoginResponse
import com.dicoding.picodiploma.storyapp.data.api.response.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody
    ) : AddStoryResponse

    @GET("stories")
    suspend fun getStory() : GetStoryResponse

    @GET("stories/{id}")
    suspend fun detailStory(
        @Path("id") id: String
    ) : DetailStoryResponse
}