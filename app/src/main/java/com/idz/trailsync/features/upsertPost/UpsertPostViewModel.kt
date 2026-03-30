package com.idz.trailsync.features.upsertPost

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idz.trailsync.data.repository.PostRepository
import com.idz.trailsync.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpsertPostViewModel : ViewModel() {
    private val _post = MutableLiveData<Post?>()
    val post: LiveData<Post?> = _post
    
    private val _isProcessing = MutableLiveData<Boolean>(false)
    val isProcessing: LiveData<Boolean> = _isProcessing

    fun setPost(p: Post?) {
        _post.value = p
    }

    fun upsertPost(
        context: Context,
        updatedPost: Post,
        uris: List<Uri>,
        capturedBitmaps: List<Bitmap>,
        callback: (Boolean) -> Unit
    ) {
        _isProcessing.value = true
        viewModelScope.launch {
            val allBitmaps = withContext(Dispatchers.IO) {
                val decodedBitmaps = uris.mapNotNull { uri ->
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(context.contentResolver, uri)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                decodedBitmaps + capturedBitmaps
            }

            withContext(Dispatchers.IO) {
                PostRepository.shared.upsertPost(updatedPost, allBitmaps) { success ->
                    viewModelScope.launch(Dispatchers.Main) {
                        _isProcessing.value = false
                        callback(success)
                    }
                }
            }
        }
    }
}
