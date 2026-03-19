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
        PostRepository.shared.getPostsByAuthor(userId) { posts ->
            _userPosts.value = posts
        }
    }

    fun deletePost(post: Post, onComplete: (Boolean) -> Unit) {
        PostRepository.shared.deletePost(post.id) { success ->
            if (success) {
                val currentUserId = post.author
                refreshUserPosts(currentUserId)
            }
            onComplete(success)
        }
    }
}
