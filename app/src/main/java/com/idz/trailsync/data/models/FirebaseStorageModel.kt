package com.idz.trailsync.data.models

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.idz.trailsync.base.StringCallback
import java.io.ByteArrayOutputStream
import java.util.UUID

class FirebaseStorageModel {

    private val storage = Firebase.storage

    fun uploadUserImage(image: Bitmap, userId: String, completion: StringCallback) {
        val storageRef = storage.reference
        val imagesUserRef = storageRef.child("profiles/${userId}_${System.currentTimeMillis()}.jpg")
        uploadImage(image, imagesUserRef, completion)
    }

    fun uploadPostImages(
        images: List<Bitmap>,
        postId: String,
        completion: (List<String>) -> Unit
    ) {
        val uploadedUrls = mutableListOf<String>()
        var completed = 0

        if (images.isEmpty()) {
            completion(emptyList())
            return
        }

        images.forEach { image ->
            val imageRef = storage.reference
                .child("posts/$postId/${UUID.randomUUID()}.jpg")

            uploadImage(image, imageRef) { url ->
                if (!url.isNullOrBlank()) {
                    uploadedUrls.add(url)
                }

                completed++
                if (completed == images.size) {
                    completion(uploadedUrls)
                }
            }
        }
    }
    private fun uploadImage(image: Bitmap, ref: StorageReference, completion: StringCallback) {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = ref.putBytes(data)
        uploadTask.addOnFailureListener {
            completion(null)
        }.addOnSuccessListener { _ ->
            ref.downloadUrl.addOnSuccessListener { uri ->
                completion(uri.toString())
            }.addOnFailureListener {
                completion(null)
            }
        }
    }
}
