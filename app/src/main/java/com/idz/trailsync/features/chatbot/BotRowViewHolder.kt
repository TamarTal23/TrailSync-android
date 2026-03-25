package com.idz.trailsync.features.chatbot

import com.idz.trailsync.databinding.ChatItemBotBinding
import com.idz.trailsync.model.ChatMessage

class BotRowViewHolder(private val binding: ChatItemBotBinding) : BaseMessageViewHolder(binding.root) {
    override fun bind(message: ChatMessage) {
        binding.messageText.text = message.content
    }
}
