package com.idz.trailsync.features.post

import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.idz.trailsync.R
import com.idz.trailsync.databinding.PostListItemBinding
import com.idz.trailsync.model.Post
import com.idz.trailsync.model.PostWithComments
import com.squareup.picasso.Picasso

class PostRowViewHolder(
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

        binding.postDeleteButton.setOnClickListener {
            post?.let {
                listener?.onDeleteClick(it)
            }
        }
    }

    fun bind(postWithComments: PostWithComments) {
        val post = postWithComments.post
        this.post = post
        binding.postTitle.text = post.title
        binding.postLocation.text = post.location?.name ?: "Unknown"
        binding.postDays.text = "${post.numberOfDays} days"
        binding.postPrice.text = post.price.toString()

        binding.saveCount.text = post.savedCount.toString()
        binding.commentCount.text = postWithComments.commentsCount.toString()

        val currentUserId = Firebase.auth.currentUser?.uid
        if (post.author == currentUserId) {
            binding.postDeleteButton.visibility = View.VISIBLE
        } else {
            binding.postDeleteButton.visibility = View.GONE
        }

        val firstPhotoUrl = post.photos.firstOrNull()

        if (!firstPhotoUrl.isNullOrBlank()) {
            if (firstPhotoUrl.startsWith("android.resource")) {
                val resId = firstPhotoUrl.substringAfterLast("/").toIntOrNull()

                if (resId != null) {
                    binding.postImage.setImageResource(resId)
                }
            } else {
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
