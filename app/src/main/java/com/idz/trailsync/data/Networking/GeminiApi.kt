package com.idz.trailsync.data.Networking

import com.idz.trailsync.data.models.GeminiRequest
import com.idz.trailsync.data.models.GeminiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiAPI {
    @POST("v1beta/models/{model}:generateContent")
    fun getChatResponse(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}