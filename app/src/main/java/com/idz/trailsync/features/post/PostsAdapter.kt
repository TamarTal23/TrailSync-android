package com.idz.trailsync.features.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idz.trailsync.databinding.PostListItemBinding
import com.idz.trailsync.model.PostWithComments

class PostsAdapter(
    var posts: List<PostWithComments>? = null,
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