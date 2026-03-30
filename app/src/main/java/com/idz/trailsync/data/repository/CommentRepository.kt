package com.idz.trailsync.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.dao.AppLocalDB
import com.idz.trailsync.data.models.FirebaseModel
import com.idz.trailsync.model.Comment
import com.idz.trailsync.model.CommentWithUser
import java.util.concurrent.Executors

class CommentRepository private constructor() {
    private val executor = Executors.newSingleThreadExecutor()
    private val database = AppLocalDB.database
    private val firebaseModel = FirebaseModel()
    private val userRepository = UserRepository.shared

    companion object {
        val shared = CommentRepository()
    }

    fun getCommentsWithUserForPost(postId: String): LiveData<List<CommentWithUser>> {
        val commentsLiveData = database.CommentDao().getCommentsForPost(postId)
        val result = MediatorLiveData<List<CommentWithUser>>()

        result.addSource(commentsLiveData) { comments ->
            if (comments == null) {
                result.value = emptyList()
                return@addSource
            }

            val commentWithUsers = mutableListOf<CommentWithUser>()
            var completedCount = 0

            if (comments.isEmpty()) {
                result.value = emptyList()
                return@addSource
            }

            comments.forEach { comment ->
                userRepository.getUserById(comment.author) { user ->
                    commentWithUsers.add(CommentWithUser(comment, user))
                    completedCount++
                    
                    if (completedCount == comments.size) {
                        val orderedList = comments.mapNotNull { c ->
                            commentWithUsers.find { it.comment.id == c.id }
                        }
                        result.value = orderedList
                    }
                }
            }
        }
        return result
    }

    fun refreshComments(postId: String, onComplete: (() -> Unit)? = null) {
        firebaseModel.getCommentsForPost(postId) { remoteComments ->
            executor.execute {
                database.CommentDao().syncCommentsForPost(postId, remoteComments)
                onComplete?.invoke()
            }
        }
    }

    fun addComment(comment: Comment, callback: BooleanCallback) {
        executor.execute {
            database.CommentDao().insertAll(comment)

            firebaseModel.addComment(comment) { success ->
                if (success) {
                    refreshComments(comment.postId) {
                        callback(true)
                    }
                } else {
                    callback(false)
                }
            }
        }
    }
}
