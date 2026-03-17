package com.idz.trailsync.features.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.R
import com.idz.trailsync.databinding.CommentItemBinding
import com.idz.trailsync.model.Comment
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(private val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.commentTextView.text = comment.text
            binding.authorNameTextView.text = comment.authorName
            
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.commentTimeTextView.text = sdf.format(comment.createdAt)
            
            if (!comment.authorImage.isNullOrEmpty()) {
                Picasso.get()
                    .load(comment.authorImage)
                    .placeholder(R.drawable.user_icon_small)
                    .into(binding.authorImageView)
            } else {
                binding.authorImageView.setImageResource(R.drawable.user_icon_small)
            }
        }
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