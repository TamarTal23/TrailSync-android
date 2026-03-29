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

    private val _isRefreshing = MutableLiveData<Boolean>(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    fun refreshPosts() {
        _userId.value?.let { uid ->
            _isRefreshing.value = true
            PostRepository.shared.refreshPostsByAuthor(uid) {
                _isRefreshing.postValue(false)
            }
        }
    }

    fun setUserId(userId: String) {
        if (_userId.value != userId) {
            _userId.value = userId
        }
    }
}
