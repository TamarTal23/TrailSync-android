package com.idz.trailsync.features.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.data.repository.CommentRepository
import com.idz.trailsync.model.Comment

class PostDetailsViewModel : ViewModel() {
    private val repository = CommentRepository.shared

    fun getCommentsForPost(postId: String): LiveData<List<Comment>> {
        return repository.getCommentsForPost(postId)
    }

    fun addComment(comment: Comment, callback: BooleanCallback) {
        repository.addComment(comment, callback)
    }
}