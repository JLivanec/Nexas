package com.example.nexas.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OpenAIAPI {
    @Headers("Authorization: Bearer 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")
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody
    ): Response<TranscriptionResponse>
}

data class TranscriptionResponse(val text: String)
