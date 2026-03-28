package com.idz.trailsync.features.savedPosts

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.databinding.SavedPostListItemBinding
import com.idz.trailsync.features.post.OnPostClickListener
import com.idz.trailsync.model.Post
import com.squareup.picasso.Picasso

class SavedPostRowViewHolder(
    private val binding: SavedPostListItemBinding,
    private val listener: OnPostClickListener?
) : RecyclerView.ViewHolder(binding.root) {

    private var post: Post? = null

    init {
        itemView.setOnClickListener {
            post?.let {
                listener?.onPostClick(it)
            }
        }

        binding.postSaveButton.setOnClickListener {
            post?.let {
                listener?.onSaveClick(it)
            }
        }
    }

    fun bind(post: Post) {
        this.post = post
        binding.postTitle.text = post.title

        val firstPhotoUrl = post.photos.firstOrNull()

        if (!firstPhotoUrl.isNullOrBlank()) {
            Picasso.get()
                .load(firstPhotoUrl)
                .fit()
                .centerCrop()
                .into(binding.postImage, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        Log.d("Picasso", "Successfully loaded image for: ${post.title}")
                    }

                    override fun onError(e: Exception?) {
                        Log.e("Picasso", "Failed to load image for: ${post.title}. Error: ${e?.message}")
                        binding.postImage.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                })
        } else {
            binding.postImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }
}
