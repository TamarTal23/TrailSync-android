package com.idz.trailsync.model

import androidx.room.Embedded
import androidx.room.Relation

data class PostWithComments(
    @Embedded val post: Post,
    @Relation(
        parentColumn = "id",
        entityColumn = "postId"
    )
    val comments: List<Comment> = emptyList()
) {
    // This allows you to get the accurate count anywhere you have this object
    val commentsCount: Int get() = comments.size
}