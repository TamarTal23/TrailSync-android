package com.idz.trailsync.features.home.post.photo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.databinding.PhotoItemBinding

class PhotoAdapter(
    var photos: List<String>? = null
) : RecyclerView.Adapter<PhotoRowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoRowViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding = PhotoItemBinding.inflate(
            inflator,
            parent,
            false
        )

        return PhotoRowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoRowViewHolder, position: Int) {
        photos?.let {
            val isLast = position == it.size - 1
            holder.bind(it[position], isLast)
        }
    }

    override fun getItemCount(): Int = photos?.size ?: 0
}