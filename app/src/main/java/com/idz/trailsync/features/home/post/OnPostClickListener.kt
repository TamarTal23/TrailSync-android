package com.idz.trailsync.features.home.post

import com.idz.trailsync.model.Post

interface OnPostClickListener {
    fun onPostClick(post: Post)
}