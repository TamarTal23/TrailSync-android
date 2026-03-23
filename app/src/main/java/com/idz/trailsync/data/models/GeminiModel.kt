package com.idz.trailsync.data.models

import android.util.Log
import com.idz.trailsync.BuildConfig
import com.idz.trailsync.model.ChatMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val role: String,
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

interface GeminiAPI {
    @POST("v1beta/models/{model}:generateContent")
    fun getChatResponse(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}

class GeminiModel {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val modelName = BuildConfig.GEMINI_MODEL

    fun fetchGeminiResponse(messageHistory: List<ChatMessage>, callback: (String?) -> Unit) {
        val contents = messageHistory
            .filter { it.role != "system" } 
            .map { msg ->
                Content(
                    role = msg.role,
                    parts = listOf(Part(msg.content))
                )
            }
        
        val request = GeminiRequest(contents = contents)

        GeminiClient.geminiApiClient.getChatResponse(modelName, apiKey, request).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val botResponse = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    callback(botResponse)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("GeminiModel", "Error Response: ${response.code()} - $errorBody")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                Log.e("GeminiModel", "Failure: ${t.message}", t)
                callback(null)
            }
        })
    }
}
