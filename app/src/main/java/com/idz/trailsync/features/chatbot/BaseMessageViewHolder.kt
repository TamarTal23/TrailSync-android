package com.idz.trailsync.features.chatbot

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.model.ChatMessage

abstract class BaseMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(message: ChatMessage)
}
