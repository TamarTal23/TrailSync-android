package com.idz.trailsync.features.post_lists

import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.R
import com.idz.trailsync.databinding.PostListItemBinding
import com.idz.trailsync.model.Post

class PostViewHolder(
    private val binding: PostListItemBinding,
    private val listener: OnPostClickListener?
) : RecyclerView.ViewHolder(binding.root) {

    private var post: Post? = null
    private var isSaved: Boolean = false

    init {
        itemView.setOnClickListener {
            post?.let {
                listener?.onPostClick(it)
            }
        }

        binding.postSaveButton.setOnClickListener {
            isSaved = !isSaved
            updateSaveButton()
        }
    }

    fun bind(post: Post) {
        this.post = post
        binding.postTitle.text = post.title
        binding.postLocation.text = post.location?.name ?: "Unknown"
        binding.postDays.text = "${post.numberOfDays} days"
        binding.postPrice.text = post.price.toString()
        
        // Static values as requested earlier
        binding.saveCount.text = "128"
        binding.commentCount.text = "50"

        // Load the first photo if it exists
        if (post.photos.isNotEmpty()) {
            val photoPath = post.photos[0]
            if (photoPath.startsWith("android.resource")) {
                val resId = photoPath.substringAfterLast("/").toIntOrNull()
                if (resId != null) {
                    binding.postImage.setImageResource(resId)
                }
            } else {
                binding.postImage.setImageURI(Uri.parse(photoPath))
            }
        } else {
            binding.postImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        updateSaveButton()
    }

    private fun updateSaveButton() {
        val context = itemView.context
        if (isSaved) {
            binding.postSaveButton.setImageResource(R.drawable.ic_bookmark_filled)
            binding.postSaveButton.setColorFilter(ContextCompat.getColor(context, R.color.orange))
        } else {
            binding.postSaveButton.setImageResource(R.drawable.ic_bookmark_outline)
            binding.postSaveButton.setColorFilter(ContextCompat.getColor(context, R.color.dark_neutral))
        }
    }
}