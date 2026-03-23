package com.idz.trailsync.features.chatbot

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.model.Message

abstract class BaseMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(message: Message)
}
