package com.idz.trailsync.data.repository

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.data.models.FirebaseModel
import com.idz.trailsync.data.models.FirebaseStorageModel
import com.idz.trailsync.data.models.PostsCallback
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
                    remotePosts.forEach { database.PostDao().upsert(it) }
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
        executor.execute {
            database.PostDao().upsert(post)

            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                callback(true)
            }
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
                        callback(result)
                    }
                }

            } ?: callback(true)
        }
    }}
