package com.idz.trailsync.features.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.idz.trailsync.databinding.CommentItemBinding
import com.idz.trailsync.model.CommentWithUser

class CommentAdapter :
    ListAdapter<CommentWithUser, CommentRowViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentRowViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentRowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentRowViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<CommentWithUser>() {
        override fun areItemsTheSame(oldItem: CommentWithUser, newItem: CommentWithUser): Boolean {
            return oldItem.comment.id == newItem.comment.id
        }

        override fun areContentsTheSame(oldItem: CommentWithUser, newItem: CommentWithUser): Boolean {
            return oldItem == newItem
        }
    }
}
