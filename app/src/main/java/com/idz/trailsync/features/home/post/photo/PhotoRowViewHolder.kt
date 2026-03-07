package com.idz.trailsync.features.home.post.photo

import android.content.res.Resources
import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.databinding.PhotoItemBinding

class PhotoRowViewHolder(
    private val binding: PhotoItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    private var photoPath: String? = null
    private var isLast: Boolean = false

    fun bind(photoPath: String?, isLast: Boolean) {
        this.photoPath = photoPath
        this.isLast = isLast

        photoPath?.let { path ->
            updatePhotoMargins()
            loadPhoto(path)
        }
    }

    private fun updatePhotoMargins() {
        val params = binding.photoCardView.layoutParams as ViewGroup.MarginLayoutParams
        params.marginEnd = if (isLast) 0 else (12 * Resources.getSystem().displayMetrics.density).toInt()
        binding.photoCardView.layoutParams = params
    }

    private fun loadPhoto(path: String) {
        if (path.startsWith("android.resource")) {
            val resId = path.substringAfterLast("/").toIntOrNull()
            if (resId != null) {
                binding.photoImage.setImageResource(resId)
            }
        } else {
            binding.photoImage.setImageURI(Uri.parse(path))
        }
    }
}