package com.idz.trailsync.features.post.PostDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.data.repository.CommentRepository
import com.idz.trailsync.data.repository.UserRepository
import com.idz.trailsync.model.Comment
import com.idz.trailsync.model.CommentWithUser
import com.idz.trailsync.model.User

class PostDetailsViewModel : ViewModel() {
    private val commentRepository = CommentRepository.shared
    private val userRepository = UserRepository.shared

    private val _postAuthor = MutableLiveData<User?>()
    val postAuthor: LiveData<User?> = _postAuthor

    fun getCommentsForPost(postId: String): LiveData<List<CommentWithUser>> {
        return commentRepository.getCommentsWithUserForPost(postId)
    }

    fun fetchPostAuthor(authorId: String) {
        userRepository.getUserById(authorId) { user ->
            _postAuthor.postValue(user)
        }
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
