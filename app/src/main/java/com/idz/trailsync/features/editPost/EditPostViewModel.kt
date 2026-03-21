package com.idz.trailsync.features.editPost

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.model.Post

class EditPostViewModel : ViewModel() {
    val post = MutableLiveData<Post?>()
    val isUpdating = MutableLiveData<Boolean>(false)

    fun setPost(p: Post) {
        post.value = p
    }

    fun updatePost(updatedPost: Post, bitmaps: List<Bitmap>?, callback: BooleanCallback) {
        isUpdating.value = true
        PostRepository.shared.upsertPost(updatedPost, bitmaps) { success ->
            isUpdating.value = false
            callback(success)
        }
    }
}
