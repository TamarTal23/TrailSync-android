package com.idz.trailsync.data.repository

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
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
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    companion object {
        val shared = PostRepository()
    }

    fun getAllPosts(): LiveData<List<PostWithComments>> {
        return database.PostDao().getAllWithComments()
    }

    fun getFilteredPosts(maxPrice: Int?, minDays: Int?, maxDays: Int?, location: String?): LiveData<List<PostWithComments>> {
        return database.PostDao().getFilteredPosts(maxPrice, minDays, maxDays, location)
    }

    fun refreshAllPosts() {
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
        return database.PostDao().getPostsByAuthorWithComments(authorId)
    }

    fun refreshPostsByAuthor(authorId: String) {
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

    fun refreshCommentsForPost(postId: String) {
        firebaseModel.getCommentsForPost(postId) { remoteComments ->
            executor.execute {
                database.CommentDao().syncCommentsForPost(postId, remoteComments)
            }
        }
    }

    fun upsertPost(post: Post, pictures: List<Bitmap>?, callback: BooleanCallback) {
        firebaseModel.upsertPost(post) { success ->
            if (!success) {
                mainHandler.post { callback(false) }
                return@upsertPost
            }

            if (pictures != null && pictures.isNotEmpty()) {
                firebaseStorageModel.uploadPostImages(pictures, post.id) { urls ->
                    val allPhotos = post.photos.toMutableList()
                    allPhotos.addAll(urls)

                    val updatedPost = post.copy(photos = allPhotos)
                    firebaseModel.upsertPost(updatedPost) { result ->
                        if (result) {
                            upsertLocal(updatedPost) {
                                mainHandler.post { callback(true) }
                            }
                        } else {
                            mainHandler.post { callback(false) }
                        }
                    }
                }
            } else {
                upsertLocal(post) {
                    mainHandler.post { callback(true) }
                }
            }
        }
    }

    private fun upsertLocal(post: Post, onComplete: () -> Unit = {}) {
        executor.execute {
            database.PostDao().upsert(post)
            onComplete()
        }
    }

    fun deletePost(postId: String, callback: BooleanCallback) {
        firebaseModel.deletePost(postId) { success ->
            if (success) {
                firebaseStorageModel.deletePostImages(postId) { storageSuccess ->
                    executor.execute {
                        database.PostDao().deleteById(postId)
                        mainHandler.post {
                            callback(true)
                        }
                    }
                }
            } else {
                mainHandler.post {
                    callback(false)
                }
            }
        }
    }
}
