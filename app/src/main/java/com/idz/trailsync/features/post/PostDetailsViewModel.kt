package com.idz.trailsync.features.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.data.repository.CommentRepository
import com.idz.trailsync.model.Comment
import com.idz.trailsync.model.CommentWithUser

class PostDetailsViewModel : ViewModel() {
    private val repository = CommentRepository.shared

    fun getCommentsForPost(postId: String): LiveData<List<CommentWithUser>> {
        return repository.getCommentsWithUserForPost(postId)
    }

    fun addComment(comment: Comment, callback: BooleanCallback) {
        repository.addComment(comment, callback)
    }
}