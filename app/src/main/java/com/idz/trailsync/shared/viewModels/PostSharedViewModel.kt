package com.idz.trailsync.shared.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.data.repository.SavedPostRepository
import com.idz.trailsync.model.Post

class PostSharedViewModel : ViewModel() {
    private val postRepository = PostRepository.shared
    private val savedPostRepository = SavedPostRepository.shared

    val currentUserId: String? get() = Firebase.auth.currentUser?.uid

    private val _savedPostIds = MediatorLiveData<Set<String>>()
    val savedPostIds: LiveData<Set<String>> = _savedPostIds

    private val _savedPosts = MediatorLiveData<List<Post>>()
    val savedPosts: LiveData<List<Post>> = _savedPosts

    init {
        val uid = currentUserId
        if (uid != null) {
            val savedPostsRepoLiveData = savedPostRepository.getSavedPostsForUser(uid)
            
            _savedPostIds.addSource(savedPostsRepoLiveData) { list ->
                _savedPostIds.value = list.map { it.postId }.toSet()
            }

            _savedPosts.addSource(postRepository.getAllPosts()) { allPostsWithComments ->
                combineAndFilter(allPostsWithComments.map { it.post }, _savedPostIds.value)
            }
            
            _savedPosts.addSource(_savedPostIds) { ids ->
                val allPosts = postRepository.getAllPosts().value?.map { it.post }
                combineAndFilter(allPosts, ids)
            }
        }
    }

    private fun combineAndFilter(allPosts: List<Post>?, ids: Set<String>?) {
        if (allPosts != null && ids != null) {
            _savedPosts.value = allPosts.filter { it.id in ids }
        } else if (ids?.isEmpty() == true) {
            _savedPosts.value = emptyList()
        }
    }

    fun toggleSavePost(post: Post, callback: BooleanCallback) {
        val uid = currentUserId ?: return
        savedPostRepository.isPostSaved(uid, post.id) { isSaved ->
            if (isSaved) {
                savedPostRepository.unsavePost(uid, post.id, callback)
            } else {
                savedPostRepository.savePost(uid, post.id, callback)
            }
        }
    }

    fun deletePost(postId: String, callback: BooleanCallback) {
        postRepository.deletePost(postId, callback)
    }

    fun refreshSavedPosts() {
        currentUserId?.let { uid ->
            savedPostRepository.refreshSavedPostsForUser(uid)
        }
    }
}
