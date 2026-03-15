package com.idz.trailsync.features.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.model.Post

class HomeViewModel : ViewModel() {
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    fun refreshPosts() {
        PostRepository.shared.getAllPosts { posts ->
            _posts.value = posts
        }
    }

    fun setPosts(posts: List<Post>) {
        _posts.value = posts
    }
}