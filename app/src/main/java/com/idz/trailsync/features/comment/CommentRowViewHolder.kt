package com.idz.trailsync.features.comment

import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.R
import com.idz.trailsync.databinding.CommentItemBinding
import com.idz.trailsync.model.CommentWithUser
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CommentRowViewHolder(private val binding: CommentItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(commentWithUser: CommentWithUser) {
        val comment = commentWithUser.comment
        val user = commentWithUser.user

        binding.commentTextView.text = comment.text
        binding.authorNameTextView.text = user?.username ?: "Anonymous"
        binding.commentTimeTextView.text = formatTimestamp(comment.createdAt)

        val profilePicture = user?.profilePicture
        if (!profilePicture.isNullOrEmpty()) {
            Picasso.get()
                .load(profilePicture)
                .placeholder(R.drawable.user_icon_small)
                .error(R.drawable.user_icon_small)
                .into(binding.authorImageView)
        } else {
            binding.authorImageView.setImageResource(R.drawable.user_icon_small)
        }
    }

    private fun formatTimestamp(createdAt: Date): String {
        val now = Date()
        val diffInMs = now.time - createdAt.time

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMs)
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMs)
        val days = TimeUnit.MILLISECONDS.toDays(diffInMs)

        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> {
                val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                sdf.format(createdAt)
            }
        }
    }
}
