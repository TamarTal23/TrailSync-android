package com.idz.trailsync.features.post

import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.idz.trailsync.R
import com.idz.trailsync.data.repository.SavedPostRepository
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
            val currentPost = post ?: return@setOnClickListener
            val currentUserId = Firebase.auth.currentUser?.uid ?: return@setOnClickListener

            isSaved = !isSaved

            val newSavedCount =
                if (isSaved) currentPost.savedCount + 1 else (currentPost.savedCount - 1).coerceAtLeast(
                    0
                )
            binding.saveCount.text = newSavedCount.toString()
            updateSaveButton()

            if (!isSaved) {
                SavedPostRepository.shared.unsavePost(currentUserId, currentPost.id) { success ->
                    if (!success) {
                        isSaved = true
                        binding.saveCount.text = currentPost.savedCount.toString()
                        updateSaveButton()
                    } else {
                        post = currentPost.copy(savedCount = newSavedCount)
                    }
                }
            } else {
                SavedPostRepository.shared.savePost(currentUserId, currentPost.id) { success ->
                    if (!success) {
                        isSaved = false
                        binding.saveCount.text = currentPost.savedCount.toString()
                        updateSaveButton()
                    } else {
                        post = currentPost.copy(savedCount = newSavedCount)
                    }
                }
            }
        }

        binding.postDeleteButton.setOnClickListener {
            post?.let {
                listener?.onDeleteClick(it)
            }
        }

        binding.postEditButton.setOnClickListener {
            post?.let {
                listener?.onEditClick(it)
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
        
        if (post.commentsLoaded) {
            binding.commentCountShimmer.stopShimmer()
            binding.commentCountShimmer.visibility = View.GONE
            binding.commentCount.visibility = View.VISIBLE
            binding.commentCount.text = postWithComments.commentsCount.toString()
        } else {
            binding.commentCount.visibility = View.GONE
            binding.commentCountShimmer.visibility = View.VISIBLE
            binding.commentCountShimmer.startShimmer()
        }

        val currentUserId = Firebase.auth.currentUser?.uid
        if (post.author == currentUserId) {
            binding.postDeleteButton.visibility = View.VISIBLE
            binding.postEditButton.visibility = View.VISIBLE
        } else {
            binding.postDeleteButton.visibility = View.GONE
            binding.postEditButton.visibility = View.GONE
        }

        if (currentUserId != null) {
            SavedPostRepository.shared.isPostSaved(currentUserId, post.id) { saved ->
                if (this.post?.id == post.id) {
                    isSaved = saved
                    updateSaveButton()
                }
            }
        }

        val firstPhotoUrl = post.photos.firstOrNull()

        if (!firstPhotoUrl.isNullOrBlank()) {
            if (firstPhotoUrl.startsWith("android.resource")) {
                val resId = firstPhotoUrl.substringAfterLast("/").toIntOrNull()
                binding.postImageShimmer.stopShimmer()
                binding.postImageShimmer.visibility = View.GONE
                if (resId != null) {
                    binding.postImage.setImageResource(resId)
                }
            } else {
                binding.postImageShimmer.visibility = View.VISIBLE
                binding.postImageShimmer.startShimmer()
                Picasso.get()
                    .load(firstPhotoUrl)
                    .fit()
                    .centerCrop()
                    .into(binding.postImage, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            binding.postImageShimmer.stopShimmer()
                            binding.postImageShimmer.visibility = View.GONE
                            Log.d("Picasso", "Successfully loaded image for: ${post.title}")
                        }

                        override fun onError(e: Exception?) {
                            binding.postImageShimmer.stopShimmer()
                            binding.postImageShimmer.visibility = View.GONE
                            Log.e(
                                "Picasso",
                                "Failed to load image for: ${post.title}. Error: ${e?.message}"
                            )

                            binding.postImage.setImageResource(android.R.drawable.ic_menu_gallery)
                        }
                    })
            }
        } else {
            binding.postImageShimmer.stopShimmer()
            binding.postImageShimmer.visibility = View.GONE
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
            binding.postSaveButton.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.dark_neutral
                )
            )
        }
    }
}
