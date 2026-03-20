package com.idz.trailsync.features.saved

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.data.repository.SavedPostRepository
import com.idz.trailsync.model.Post

class SavedPostViewModel : ViewModel() {
    private val _savedPosts = MutableLiveData<List<Post>>()
    val savedPosts: LiveData<List<Post>> = _savedPosts

    fun refreshSavedPosts() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        
        SavedPostRepository.shared.getSavedPostsForUser(userId) { savedList ->
            val postIds = savedList.map { it.postId }
            
            PostRepository.shared.getAllPosts { allPosts ->
                val filteredPosts = allPosts.filter { it.id in postIds }
                _savedPosts.value = filteredPosts
            }
        }
    }
}
