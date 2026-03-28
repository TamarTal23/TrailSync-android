package com.idz.trailsync.model

data class CommentWithUser(
    val comment: Comment,
    val user: User?
)
