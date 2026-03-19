package com.idz.trailsync.features.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.idz.trailsync.databinding.CommentItemBinding
import com.idz.trailsync.model.Comment

class CommentAdapter :
    ListAdapter<Comment, CommentRowViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentRowViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentRowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentRowViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}