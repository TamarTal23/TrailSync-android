package com.idz.trailsync.features.home.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.databinding.PostListItemBinding
import com.idz.trailsync.features.home.post.PostRowViewHolder
import com.idz.trailsync.model.Post

class PostsAdapter(
    var posts: List<Post>? = null,
    var listener: OnPostClickListener? = null
) : RecyclerView.Adapter<PostRowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostRowViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding = PostListItemBinding.inflate(
            inflator,
            parent,
            false
        )

        return PostRowViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: PostRowViewHolder, position: Int) {
        posts?.get(position)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int = posts?.size ?: 0
}