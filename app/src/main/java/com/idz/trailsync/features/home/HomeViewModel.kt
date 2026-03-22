package com.idz.trailsync.features.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.model.PostWithComments

data class PostFilters(
    val maxPrice: Int? = null,
    val minDays: Int? = null,
    val maxDays: Int? = null,
    val location: String? = null
)

class HomeViewModel : ViewModel() {
    private val _filters = MutableLiveData<PostFilters>(PostFilters())
    
    val posts: LiveData<List<PostWithComments>> = _filters.switchMap { filters ->
        PostRepository.shared.getFilteredPosts(
            filters.maxPrice,
            filters.minDays,
            filters.maxDays,
            filters.location
        )
    }

    fun setFilters(maxPrice: Int?, minDays: Int?, maxDays: Int?, location: String?) {
        _filters.value = PostFilters(maxPrice, minDays, maxDays, location)
    }

    fun clearFilters() {
        _filters.value = PostFilters()
    }

    fun refreshPosts() {
        PostRepository.shared.refreshAllPosts()
    }

    fun deletePost(postId: String, callback: BooleanCallback) {
        PostRepository.shared.deletePost(postId, callback)
    }
}
