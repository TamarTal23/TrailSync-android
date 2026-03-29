package com.idz.trailsync.data.models

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.StringCallback
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.Executors

class FirebaseStorageModel {

    private val storage = Firebase.storage
    private val executor = Executors.newSingleThreadExecutor()

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

    fun deletePostImages(postId: String, callback: BooleanCallback) {
        val storageRef = storage.reference.child("posts/$postId")

        storageRef.listAll().addOnSuccessListener { listResult ->
            val totalItems = listResult.items.size
            if (totalItems == 0) {
                callback(true)
                return@addOnSuccessListener
            }
            
            var deletedCount = 0
            var failed = false
            
            listResult.items.forEach { item ->
                item.delete().addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        failed = true
                    }
                    deletedCount++
                    if (deletedCount == totalItems) {
                        callback(!failed)
                    }
                }
            }
        }.addOnFailureListener {
            callback(false)
        }
    }

    private fun uploadImage(image: Bitmap, ref: StorageReference, completion: StringCallback) {
        executor.execute {
            val baos = ByteArrayOutputStream()
            // Compression is a heavy task, must be on background thread
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
}
