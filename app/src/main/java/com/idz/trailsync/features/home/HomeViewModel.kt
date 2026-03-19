package com.idz.trailsync.features.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.model.PostWithComments

class HomeViewModel : ViewModel() {
    val posts: LiveData<List<PostWithComments>> = PostRepository.shared.getAllPosts()


    fun refreshPosts() {
        PostRepository.shared.refreshAllPosts()
    }
}