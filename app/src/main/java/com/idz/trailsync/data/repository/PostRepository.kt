package com.idz.trailsync.data.repository

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.data.models.FirebaseModel
import com.idz.trailsync.data.models.FirebaseStorageModel
import com.idz.trailsync.model.Post
import com.idz.trailsync.model.PostWithComments
import com.idz.trailsync.dao.AppLocalDB
import com.idz.trailsync.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class PostRepository private constructor() {
    private var executor = Executors.newSingleThreadExecutor()
    private val firebaseModel = FirebaseModel()
    private val firebaseStorageModel = FirebaseStorageModel()
    private val database: AppLocalDbRepository = AppLocalDB.database

    companion object {
        val shared = PostRepository()
    }

    fun getAllPosts(): LiveData<List<PostWithComments>> {
        refreshAllPosts()
        return database.PostDao().getAllWithComments()
    }

    private fun refreshAllPosts() {
        firebaseModel.getAllPosts { remotePosts ->
            executor.execute {
                val postDao = database.PostDao()
                remotePosts.forEach { post ->
                    postDao.upsert(post)
                    refreshCommentsForPost(post.id)
                }
            }
        }
    }

    fun getPostsByAuthor(authorId: String): LiveData<List<PostWithComments>> {
        refreshPostsByAuthor(authorId)
        return database.PostDao().getPostsByAuthorWithComments(authorId)
    }

    private fun refreshPostsByAuthor(authorId: String) {
        firebaseModel.getPostsByAuthor(authorId) { remotePosts ->
            executor.execute {
                val postDao = database.PostDao()
                remotePosts.forEach { post ->
                    postDao.upsert(post)
                    refreshCommentsForPost(post.id)
                }
            }
        }
    }

    private fun refreshCommentsForPost(postId: String) {
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

    fun upsertPost(post: Post, pictures: List<Bitmap>?, callback: BooleanCallback) {
        executor.execute {
            database.PostDao().upsert(post)
        }

        firebaseModel.upsertPost(post) { success ->
            if (!success) {
                callback(false)
                return@upsertPost
            }

            pictures?.let { images ->
                firebaseStorageModel.uploadPostImages(images, post.id) { urls ->
                    val updatedPost = post.copy(photos = urls)
                    firebaseModel.upsertPost(updatedPost) { result ->
                        if (result) {
                            executor.execute {
                                database.PostDao().upsert(updatedPost)
                            }
                        }
                        callback(result)
                    }
                }
            } ?: callback(true)
        }
    }
}