package com.idz.trailsync.features.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
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

    private val _isRefreshing = MutableLiveData<Boolean>(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    val isPagingLoading: LiveData<Boolean> = PostRepository.shared.isPagingLoading

    fun updateLocation(location: String?) {
        val current = _filters.value ?: PostFilters()
        _filters.value = current.copy(location = location)
    }

    fun applyAdvancedFilters(maxPrice: String?, minDays: String?, maxDays: String?) {
        val current = _filters.value ?: PostFilters()
        _filters.value = current.copy(
            maxPrice = maxPrice?.toIntOrNull(),
            minDays = minDays?.toIntOrNull(),
            maxDays = maxDays?.toIntOrNull()
        )
    }

    fun clearFilters() {
        _filters.value = PostFilters()
    }

    fun refreshPosts() {
        _isRefreshing.value = true
        PostRepository.shared.refreshAllPosts {
            _isRefreshing.postValue(false)
        }
    }

    fun loadNextPage() {
        PostRepository.shared.loadNextPage()
    }
}
