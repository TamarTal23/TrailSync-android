package com.idz.trailsync.features.savedPosts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.data.repository.SavedPostRepository
import com.idz.trailsync.model.Post

class SavedPostViewModel : ViewModel() {
    private val _savedPostIds = MutableLiveData<List<String>>()
    private val _savedPosts = MediatorLiveData<List<Post>>()
    val savedPosts: LiveData<List<Post>> = _savedPosts

    init {
        _savedPosts.addSource(PostRepository.shared.getAllPosts()) { postsWithComments ->
            filterSavedPosts(postsWithComments.map { it.post }, _savedPostIds.value)
        }
        _savedPosts.addSource(_savedPostIds) { ids ->
            val postsWithComments = PostRepository.shared.getAllPosts().value
            filterSavedPosts(postsWithComments?.map { it.post }, ids)
        }
    }

    private fun filterSavedPosts(allPosts: List<Post>?, ids: List<String>?) {
        if (allPosts != null && ids != null) {
            _savedPosts.value = allPosts.filter { it.id in ids }
        } else if (ids?.isEmpty() == true) {
            _savedPosts.value = emptyList()
        }
    }

    fun refreshSavedPosts() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        PostRepository.shared.refreshAllPosts()
        SavedPostRepository.shared.getSavedPostsForUser(userId) { savedList ->
            _savedPostIds.value = savedList.map { it.postId }
        }
    }
}
