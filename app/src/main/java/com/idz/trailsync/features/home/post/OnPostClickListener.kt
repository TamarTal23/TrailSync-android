package com.idz.trailsync.features.home.post

import com.idz.trailsync.models.Post

interface OnPostClickListener {
    fun onPostClick(post: Post)
}