package com.idz.trailsync.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.model.Post

class ProfileViewModel : ViewModel() {
    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> = _userPosts

    fun refreshUserPosts(userId: String) {
        PostRepository.Companion.shared.getPostsByAuthor(userId) { posts ->
            _userPosts.value = posts
        }
    }
}