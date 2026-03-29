package com.idz.trailsync.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.MyApplication
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

    private var lastDocument: DocumentSnapshot? = null
    private val _isPagingLoading = MutableLiveData<Boolean>(false)
    val isPagingLoading: LiveData<Boolean> = _isPagingLoading
    
    private val _isRefreshing = MutableLiveData<Boolean>(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    companion object {
        val shared = PostRepository()
        private const val PAGE_SIZE = 5L
        private const val POSTS_LAST_UPDATED = "posts_last_updated"
    }

    fun getAllPosts(): LiveData<List<PostWithComments>> {
        return database.PostDao().getAllWithComments()
    }

    fun getFilteredPosts(maxPrice: Int?, minDays: Int?, maxDays: Int?, location: String?): LiveData<List<PostWithComments>> {
        return database.PostDao().getFilteredPosts(maxPrice, minDays, maxDays, location)
    }

    fun refreshAllPosts() {
        val context = MyApplication.Globals.context ?: return
        val sharedPrefs = context.getSharedPreferences("TAG", Context.MODE_PRIVATE)
        val lastUpdated = sharedPrefs.getLong(POSTS_LAST_UPDATED, 0L)

        _isRefreshing.postValue(true)
        firebaseModel.getPostsSince(lastUpdated) { remotePosts ->
            if (remotePosts.isEmpty()) {
                mainHandler.post { _isRefreshing.value = false }
                return@getPostsSince
            }

            executor.execute {
                val postDao = database.PostDao()
                var latestTime = lastUpdated
                
                remotePosts.forEach { post ->
                    val localPost = postDao.getById(post.id)
                    val postToSave = if (localPost != null) {
                        post.copy(commentsLoaded = localPost.commentsLoaded)
                    } else {
                        post
                    }
                    postDao.upsert(postToSave)
                    refreshCommentsForPost(post.id)
                    
                    if (post.updatedAt.time > latestTime) {
                        latestTime = post.updatedAt.time
                    }
                }
                
                sharedPrefs.edit().putLong(POSTS_LAST_UPDATED, latestTime).apply()
                mainHandler.post { _isRefreshing.value = false }
            }
        }
    }

    fun loadNextPage() {
        if (_isPagingLoading.value == true) return
        
        _isPagingLoading.value = true
        firebaseModel.getPostsPaged(PAGE_SIZE, lastDocument) { remotePosts, lastDoc ->
            lastDocument = lastDoc
            executor.execute {
                val postDao = database.PostDao()
                remotePosts.forEach { post ->
                    val localPost = postDao.getById(post.id)
                    val postToSave = if (localPost != null) {
                        post.copy(commentsLoaded = localPost.commentsLoaded)
                    } else {
                        post
                    }
                    postDao.upsert(postToSave)
                    refreshCommentsForPost(post.id)
                }
                mainHandler.post {
                    _isPagingLoading.value = false
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
                    val localPost = postDao.getById(post.id)
                    val postToSave = if (localPost != null) {
                        post.copy(commentsLoaded = localPost.commentsLoaded)
                    } else {
                        post
                    }
                    postDao.upsert(postToSave)
                    refreshCommentsForPost(post.id)
                }
            }
        }
    }

    fun refreshCommentsForPost(postId: String) {
        firebaseModel.getCommentsForPost(postId) { remoteComments ->
            executor.execute {
                database.CommentDao().syncCommentsForPost(postId, remoteComments)
                database.PostDao().updateCommentsLoaded(postId, true)
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
        executor.execute {
            database.PostDao().deleteById(postId)
            
            mainHandler.post {
                callback(true)
                
                firebaseModel.deletePost(postId) { success ->
                    if (success) {
                        firebaseStorageModel.deletePostImages(postId) { }
                    }
                }
            }
        }
    }

    fun clearLocalDatabase() {
        executor.execute {
            database.clearAllTables()
        }
    }
}
