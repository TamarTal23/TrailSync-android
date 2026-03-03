package com.idz.trailsync.features.post_lists

import com.idz.trailsync.model.Post

interface OnPostClickListener {
    fun onPostClick(post: Post)
}