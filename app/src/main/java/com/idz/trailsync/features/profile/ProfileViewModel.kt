package com.idz.trailsync.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.model.PostWithComments

class ProfileViewModel : ViewModel() {
    private val _userId = MutableLiveData<String>()

    val userPosts: LiveData<List<PostWithComments>> = _userId.switchMap { id ->
        PostRepository.shared.getPostsByAuthor(id)
    }

    fun refreshPosts() {
        _userId.value?.let { uid ->
            PostRepository.shared.refreshPostsByAuthor(uid)
        }
    }

    fun setUserId(userId: String) {
        if (_userId.value != userId) {
            _userId.value = userId
        }
    }
}
