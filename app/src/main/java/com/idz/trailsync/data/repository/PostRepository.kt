package com.idz.trailsync.data.repository

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.PostsCallback
import com.idz.trailsync.data.models.FirebaseModel
import com.idz.trailsync.data.models.FirebaseStorageModel
import com.idz.trailsync.model.Post
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

    fun getAllPosts(callback: PostsCallback) {
        executor.execute {
            val localPosts = database.PostDao().getAll()
            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                callback(localPosts)
            }

            firebaseModel.getAllPosts { remotePosts ->
                executor.execute {
                    database.PostDao().clearAndInsertAll(remotePosts)
                    
                    val updatedLocalPosts = database.PostDao().getAll()
                    HandlerCompat.createAsync(Looper.getMainLooper()).post {
                        callback(updatedLocalPosts)
                    }
                }
            }
        }
    }

    fun getPostsByAuthor(authorId: String, callback: PostsCallback) {
        executor.execute {
            val localPosts = database.PostDao().getPostsByAuthor(authorId)
            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                callback(localPosts)
            }

            firebaseModel.getPostsByAuthor(authorId) { remotePosts ->
                executor.execute {
                    remotePosts.forEach { database.PostDao().upsert(it) }
                    val updatedLocalPosts = database.PostDao().getPostsByAuthor(authorId)
                    HandlerCompat.createAsync(Looper.getMainLooper()).post {
                        callback(updatedLocalPosts)
                    }
                }
            }
        }
    }

    fun upsertPost(post: Post, pictures: List<Bitmap>?, callback: BooleanCallback) {
        firebaseModel.upsertPost(post) { success ->
            if (!success) {
                HandlerCompat.createAsync(Looper.getMainLooper()).post {
                    callback(false)
                }
                return@upsertPost
            }

            if (pictures != null && pictures.isNotEmpty()) {
                firebaseStorageModel.uploadPostImages(pictures, post.id) { urls ->
                    val updatedPost = post.copy(photos = urls)

                    firebaseModel.upsertPost(updatedPost) { result ->
                        if (result) {
                            upsertLocal(updatedPost, callback)
                        } else {
                            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                                callback(false)
                            }
                        }
                    }
                }
            } else {
                upsertLocal(post, callback)
            }
        }
    }

    private fun upsertLocal(post: Post, callback: BooleanCallback) {
        executor.execute {
            database.PostDao().upsert(post)
            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                callback(true)
            }
        }
    }

    fun deletePost(postId: String, callback: BooleanCallback) {
        firebaseModel.deletePost(postId) { success ->
            if (success) {
                firebaseStorageModel.deletePostImages(postId) { storageSuccess ->
                    executor.execute {
                        database.PostDao().deleteById(postId)
                        HandlerCompat.createAsync(Looper.getMainLooper()).post {
                            callback(true)
                        }
                    }
                }
            } else {
                callback(false)
            }
        }
    }
}
