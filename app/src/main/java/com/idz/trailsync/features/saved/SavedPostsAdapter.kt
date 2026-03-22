package com.idz.trailsync.features.saved

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.databinding.SavedPostListItemBinding
import com.idz.trailsync.features.post.OnPostClickListener
import com.idz.trailsync.model.Post

class SavedPostsAdapter(
    var posts: List<Post>? = null,
    var listener: OnPostClickListener? = null
) : RecyclerView.Adapter<SavedPostRowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedPostRowViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SavedPostListItemBinding.inflate(inflater, parent, false)
        return SavedPostRowViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: SavedPostRowViewHolder, position: Int) {
        posts?.get(position)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int = posts?.size ?: 0
}
