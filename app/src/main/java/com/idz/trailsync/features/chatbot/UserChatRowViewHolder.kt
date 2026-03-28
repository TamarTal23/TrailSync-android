package com.idz.trailsync.features.chatbot

import com.idz.trailsync.databinding.ChatItemUserBinding
import com.idz.trailsync.model.ChatMessage

class UserChatRowViewHolder(private val binding: ChatItemUserBinding) : BaseMessageViewHolder(binding.root) {
    override fun bind(message: ChatMessage) {
        binding.messageText.text = message.content
    }
}
