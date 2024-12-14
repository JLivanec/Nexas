package com.example.nexas.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Secret not longer allowed for repository. Transcriptions will not work
 */

interface OpenAIAPI {
    @Headers("Authorization: Bearer ")
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody
    ): Response<TranscriptionResponse>
}

data class TranscriptionResponse(val text: String)
