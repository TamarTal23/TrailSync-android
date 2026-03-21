package com.idz.trailsync.features.post.photo

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.R
import com.idz.trailsync.databinding.PhotoItemBinding
import com.squareup.picasso.Picasso

class PhotoRowViewHolder<T>(
    private val binding: PhotoItemBinding,
    private val onRemoveClick: ((T) -> Unit)? = null
) : RecyclerView.ViewHolder(binding.root) {

    private var photo: T? = null
    private var isLast: Boolean = false

    fun bind(photo: T?, isLast: Boolean) {
        this.photo = photo
        this.isLast = isLast

        photo?.let { p ->
            updateDimensionsAndMargins()
            loadPhoto(p)
            setupRemoveButton(p)
        }
    }

    private fun updateDimensionsAndMargins() {
        val params = binding.photoCardView.layoutParams as ViewGroup.MarginLayoutParams
        val density = itemView.context.resources.displayMetrics.density
        
        if (onRemoveClick != null) {
            params.width = (80 * density).toInt()
            params.height = (80 * density).toInt()
            params.marginEnd = (8 * density).toInt()
        } else {
            params.width = (290 * density).toInt()
            params.height = (180 * density).toInt()
            params.marginEnd = if (isLast) 0 else (12 * density).toInt()
        }

        binding.photoCardView.layoutParams = params
    }

    private fun loadPhoto(photo: T) {
        when (photo) {
            is String -> {
                if (photo.startsWith("android.resource")) {
                    val resId = photo.substringAfterLast("/").toIntOrNull()
                    binding.photoShimmer.stopShimmer()
                    binding.photoShimmer.visibility = View.GONE
                    if (resId != null) {
                        binding.photoImage.setImageResource(resId)
                    }
                } else {
                    binding.photoShimmer.visibility = View.VISIBLE
                    binding.photoShimmer.startShimmer()
                    Picasso.get()
                        .load(photo)
                        .into(binding.photoImage, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                binding.photoShimmer.stopShimmer()
                                binding.photoShimmer.visibility = View.GONE
                            }

                            override fun onError(e: Exception?) {
                                binding.photoShimmer.stopShimmer()
                                binding.photoShimmer.visibility = View.GONE
                                binding.photoImage.setImageResource(android.R.drawable.ic_menu_gallery)
                            }
                        })
                }
            }
            is Uri -> {
                binding.photoShimmer.stopShimmer()
                binding.photoShimmer.visibility = View.GONE
                binding.photoImage.setImageURI(photo)
            }
        }
    }

    private fun setupRemoveButton(photo: T) {
        if (onRemoveClick != null) {
            binding.removePhotoButton.visibility = View.VISIBLE
            binding.removePhotoButton.setOnClickListener {
                onRemoveClick.invoke(photo)
            }
        } else {
            binding.removePhotoButton.visibility = View.GONE
        }
    }
}
