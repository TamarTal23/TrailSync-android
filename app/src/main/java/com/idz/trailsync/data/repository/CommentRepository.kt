package com.idz.trailsync.data.repository

import androidx.lifecycle.LiveData
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.dao.AppLocalDB
import com.idz.trailsync.data.models.FirebaseModel
import com.idz.trailsync.model.Comment
import com.idz.trailsync.base.Constants
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.concurrent.Executors

class CommentRepository private constructor() {
    private val executor = Executors.newSingleThreadExecutor()
    private val database = AppLocalDB.database
    private val firestore = Firebase.firestore

    companion object {
        val shared = CommentRepository()
    }

    fun getCommentsForPost(postId: String): LiveData<List<Comment>> {
        refreshComments(postId)
        return database.CommentDao().getCommentsForPost(postId)
    }

    private fun refreshComments(postId: String) {
        firestore.collection(Constants.COLLECTIONS.POSTS)
            .document(postId)
            .collection(Comment.SUB_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                val comments = result.map { doc ->
                    Comment.fromJSON(doc.data, postId, doc.id)
                }
                executor.execute {
                    database.CommentDao().deleteCommentsForPost(postId)
                    database.CommentDao().insertAll(*comments.toTypedArray())
                }
            }
    }

    fun addComment(comment: Comment, callback: BooleanCallback) {
        firestore.collection(Constants.COLLECTIONS.POSTS)
            .document(comment.postId)
            .collection(Comment.SUB_COLLECTION)
            .add(comment.json)
            .addOnSuccessListener {
                refreshComments(comment.postId)
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}
