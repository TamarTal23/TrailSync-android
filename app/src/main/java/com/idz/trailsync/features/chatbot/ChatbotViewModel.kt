package com.idz.trailsync.features.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.idz.trailsync.data.models.GeminiModel
import com.idz.trailsync.model.ChatMessage

class ChatbotViewModel : ViewModel() {
    private val geminiModel = GeminiModel()
    private val _messages = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())
    val messages: LiveData<MutableList<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading


    init {
        if (_messages.value?.isEmpty() == true) {
            _messages.value?.add(ChatMessage("You are an assistant for travel planning and route generation. Only respond with related information about destinations, itineraries, and travel tips. Keep responses concise and under 350 words.Use bullet points when helpful.", "system"))
            addMessage("Hi, let me help you plan your next trip!", "model")
        }
    }

    fun sendMessage(text: String) {
        addMessage(text, "user")
        _isLoading.value = true

        val currentMessages = _messages.value ?: mutableListOf()
        val historyForApi = currentMessages.toList()

        geminiModel.fetchGeminiResponse(historyForApi) { response ->
            _isLoading.postValue(false)
            if (response != null) {
                addMessage(response, "model")
            } else {
                addMessage("Sorry, I encountered an error. Please try again.", "model")
            }
        }
    }

    private fun addMessage(content: String, role: String) {
        val currentList = _messages.value ?: mutableListOf()
        currentList.add(ChatMessage(content, role))
        _messages.postValue(currentList)
    }
}
