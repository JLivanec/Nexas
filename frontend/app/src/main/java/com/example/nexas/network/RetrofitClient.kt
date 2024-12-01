package com.example.nexas.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient())
        .build()

    val openAIService: OpenAIAPI = retrofit.create(OpenAIAPI::class.java)
}