package com.idz.trailsync.model

import androidx.room.Embedded
import androidx.room.Relation

data class PostWithComments(
    @Embedded val post: Post,
    @Relation(
        parentColumn = "author",
        entityColumn = "id"
    )
    val author: User? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "postId"
    )
    val comments: List<Comment> = emptyList()
) {
    val commentsCount: Int get() = comments.size
}