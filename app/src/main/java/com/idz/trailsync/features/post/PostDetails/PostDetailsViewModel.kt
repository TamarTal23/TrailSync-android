package com.idz.trailsync.features.post.PostDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.data.repository.CommentRepository
import com.idz.trailsync.model.Comment
import com.idz.trailsync.model.CommentWithUser
import com.idz.trailsync.model.User

class PostDetailsViewModel : ViewModel() {
    private val commentRepository = CommentRepository.shared

    fun getCommentsForPost(postId: String): LiveData<List<CommentWithUser>> {
        return commentRepository.getCommentsWithUserForPost(postId)
    }

    fun addComment(text: String, postId: String, user: User, callback: BooleanCallback) {
        val comment = Comment(
            id = java.util.UUID.randomUUID().toString(),
            text = text,
            author = user.id,
            postId = postId
        )
        commentRepository.addComment(comment, callback)
    }
}
