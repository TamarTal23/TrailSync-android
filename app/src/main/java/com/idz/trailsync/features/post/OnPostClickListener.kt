package com.idz.trailsync.features.post

import com.idz.trailsync.model.Post

interface OnPostClickListener {
    fun onPostClick(post: Post)
    fun onDeleteClick(post: Post)
}