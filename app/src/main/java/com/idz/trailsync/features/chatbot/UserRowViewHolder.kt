package com.idz.trailsync.features.chatbot

import com.idz.trailsync.databinding.ChatItemUserBinding
import com.idz.trailsync.model.Message

class UserRowViewHolder(private val binding: ChatItemUserBinding) : BaseMessageViewHolder(binding.root) {
    override fun bind(message: Message) {
        binding.messageText.text = message.content
    }
}
