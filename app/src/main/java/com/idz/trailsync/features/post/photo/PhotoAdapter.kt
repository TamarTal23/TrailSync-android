package com.idz.trailsync.features.post.photo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.databinding.PhotoItemBinding

class PhotoAdapter<T>(
    var photos: List<T>? = null,
    var onRemoveClick: ((T) -> Unit)? = null
) : RecyclerView.Adapter<PhotoRowViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoRowViewHolder<T> {
        val inflator = LayoutInflater.from(parent.context)
        val binding = PhotoItemBinding.inflate(
            inflator,
            parent,
            false
        )

        return PhotoRowViewHolder(binding, onRemoveClick)
    }

    override fun onBindViewHolder(holder: PhotoRowViewHolder<T>, position: Int) {
        photos?.let {
            val isLast = position == it.size - 1
            holder.bind(it[position], isLast)
        }
    }

    override fun getItemCount(): Int = photos?.size ?: 0
}