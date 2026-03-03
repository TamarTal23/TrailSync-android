package com.idz.trailsync.features.post_lists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.databinding.PostListItemBinding
import com.idz.trailsync.model.Post

class PostsAdapter(
    private val posts: List<Post>,
    private val listener: OnPostClickListener? = null
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
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size
}