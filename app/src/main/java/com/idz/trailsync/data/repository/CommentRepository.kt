package com.idz.trailsync.data.repository

import androidx.lifecycle.LiveData
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.dao.AppLocalDB
import com.idz.trailsync.data.models.FirebaseModel
import com.idz.trailsync.model.Comment
import java.util.concurrent.Executors

class CommentRepository private constructor() {
    private val executor = Executors.newSingleThreadExecutor()
    private val database = AppLocalDB.database
    private val firebaseModel = FirebaseModel()

    companion object {
        val shared = CommentRepository()
    }

    fun getCommentsForPost(postId: String): LiveData<List<Comment>> {
        refreshComments(postId)
        return database.CommentDao().getCommentsForPost(postId)
    }

    private fun refreshComments(postId: String) {
        firebaseModel.getCommentsForPost(postId) { remoteComments ->
            executor.execute {
                val commentDao = database.CommentDao()

                val localIds = commentDao.getCommentIdsForPost(postId)
                val remoteIds = remoteComments.map { it.id }.toSet()
                val idsToDelete = localIds.filter { it !in remoteIds }

                if (idsToDelete.isNotEmpty()) {
                    commentDao.deleteByIds(idsToDelete)
                }

                if (remoteComments.isNotEmpty()) {
                    commentDao.insertAll(*remoteComments.toTypedArray())
                }
            }
        }
    }

    fun addComment(comment: Comment, callback: BooleanCallback) {
        firebaseModel.addComment(comment) { success ->
            if (success) {
                refreshComments(comment.postId)
            }
            callback(success)
        }
    }
}
