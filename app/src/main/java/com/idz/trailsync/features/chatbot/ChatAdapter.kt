package com.idz.trailsync.features.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.databinding.ChatItemBotBinding
import com.idz.trailsync.databinding.ChatItemUserBinding
import com.idz.trailsync.model.ChatMessage

class ChatAdapter : RecyclerView.Adapter<BaseMessageViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }

    fun setMessages(newMessages: List<ChatMessage>) {
        val diffCallback = MessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messages.clear()
        messages.addAll(newMessages)
        diffResult.dispatchUpdatesTo(this)
    }

    class MessageDiffCallback(
        private val oldList: List<ChatMessage>,
        private val newList: List<ChatMessage>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].content == newList[newItemPosition].content &&
                    oldList[oldItemPosition].role == newList[newItemPosition].role
        }
    }

    fun addLoadingMessage(text: String) {
        messages.add(ChatMessage(text, "model"))
        notifyItemInserted(messages.size - 1)
    }

    fun removeLoadingMessage() {
        if (messages.isNotEmpty() && (messages.last().content == "Thinking..." || messages.last().content == "Typing...")) {
            val index = messages.size - 1
            messages.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].role == "user") VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            UserRowViewHolder(ChatItemUserBinding.inflate(inflater, parent, false))
        } else {
            BotRowViewHolder(ChatItemBotBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: BaseMessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size
}
