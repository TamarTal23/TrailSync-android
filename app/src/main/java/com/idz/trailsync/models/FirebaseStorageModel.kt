package com.idz.trailsync.models

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.idz.trailsync.base.StringCallback
import java.io.ByteArrayOutputStream

class FirebaseStorageModel {

    private val storage = Firebase.storage

    fun uploadUserImage(image: Bitmap, userId: String, completion: StringCallback) {
        val storageRef = storage.reference
        val imagesUserRef = storageRef.child("profiles/${userId}_${System.currentTimeMillis()}.jpg")
        uploadImage(image, imagesUserRef, completion)
    }

    fun uploadPostImage(image: Bitmap, postId: String, completion: StringCallback) {
        val storageRef = storage.reference
        val imagesPostRef = storageRef.child("posts/${postId}_${System.currentTimeMillis()}.jpg")
        uploadImage(image, imagesPostRef, completion)
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
