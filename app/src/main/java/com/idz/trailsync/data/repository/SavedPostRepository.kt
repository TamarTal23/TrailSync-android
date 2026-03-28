package com.idz.trailsync.data.repository

import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.dao.AppLocalDB
import com.idz.trailsync.dao.AppLocalDbRepository
import com.idz.trailsync.data.models.FirebaseModel
import com.idz.trailsync.model.SavedPost
import java.util.concurrent.Executors

class SavedPostRepository private constructor() {
    private val executor = Executors.newSingleThreadExecutor()
    private val firebaseModel = FirebaseModel()
    private val database: AppLocalDbRepository = AppLocalDB.database

    companion object {
        val shared = SavedPostRepository()
    }

    fun getSavedPostsForUser(userId: String): LiveData<List<SavedPost>> {
        return database.SavedPostDao().getSavedPostsByUser(userId)
    }

    fun refreshSavedPostsForUser(userId: String) {
        firebaseModel.getSavedPostsForUser(userId) { remoteSavedPosts ->
            executor.execute {
                database.SavedPostDao().clearAndInsertAll(userId, remoteSavedPosts)
            }
        }
    }

    fun savePost(userId: String, postId: String, callback: BooleanCallback) {
        firebaseModel.savePost(userId, postId) { success ->
            if (success) {
                executor.execute {
                    val savedPost = SavedPost(postId = postId, userId = userId)
                    database.SavedPostDao().upsert(savedPost)

                    val post = database.PostDao().getById(postId)
                    if (post != null) {
                        val updatedPost = post.copy(savedCount = post.savedCount + 1)
                        database.PostDao().upsert(updatedPost)
                    }

                    HandlerCompat.createAsync(Looper.getMainLooper()).post {
                        callback(true)
                    }
                }
            } else {
                HandlerCompat.createAsync(Looper.getMainLooper()).post {
                    callback(false)
                }
            }
        }
    }

    fun unsavePost(userId: String, postId: String, callback: BooleanCallback) {
        firebaseModel.unsavePost(userId, postId) { success ->
            if (success) {
                executor.execute {
                    database.SavedPostDao().delete(userId, postId)

                    val post = database.PostDao().getById(postId)
                    if (post != null) {
                        val updatedPost = post.copy(savedCount = (post.savedCount - 1).coerceAtLeast(0))
                        database.PostDao().upsert(updatedPost)
                    }

                    HandlerCompat.createAsync(Looper.getMainLooper()).post {
                        callback(true)
                    }
                }
            } else {
                HandlerCompat.createAsync(Looper.getMainLooper()).post {
                    callback(false)
                }
            }
        }
    }

    fun isPostSaved(userId: String, postId: String, callback: (Boolean) -> Unit) {
        executor.execute {
            val savedPost = database.SavedPostDao().getSavedPostById(userId, postId)
            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                callback(savedPost != null)
            }
        }
    }
}
