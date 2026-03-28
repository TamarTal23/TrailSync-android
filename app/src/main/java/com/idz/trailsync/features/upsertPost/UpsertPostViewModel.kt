package com.idz.trailsync.features.upsertPost

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.model.Post

class UpsertPostViewModel : ViewModel() {
    private val _post = MutableLiveData<Post?>()
    val post: LiveData<Post?> = _post
    
    val isProcessing = MutableLiveData<Boolean>(false)

    fun setPost(p: Post?) {
        _post.value = p
    }

    fun upsertPost(updatedPost: Post, bitmaps: List<Bitmap>?, callback: (Boolean) -> Unit) {
        isProcessing.value = true
        PostRepository.shared.upsertPost(updatedPost, bitmaps) { success ->
            isProcessing.value = false
            callback(success)
        }
    }
}
