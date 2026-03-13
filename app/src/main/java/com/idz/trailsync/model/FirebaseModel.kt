package com.idz.trailsync.model

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.storage.FirebaseStorage
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.UsersCallback
import com.idz.trailsync.base.Constants
import com.idz.trailsync.base.UserCallback
import java.io.ByteArrayOutputStream

class FirebaseModel {
    private val database = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }
        database.firestoreSettings = setting
    }

    fun getAllUsers(callback: UsersCallback) {
        database.collection(Constants.COLLECTIONS.USERS).get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val users: MutableList<User> = mutableListOf()
                        for (json in it.result) {
                            users.add(User.fromJSON(json.data))
                        }
                        callback(users)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun getUserByEmail(email: String, callback: UserCallback) {
        database.collection(Constants.COLLECTIONS.USERS).whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user: User = User.fromJSON(documents.documents[0].data ?: mapOf())
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun getUserById(id: String, callback: UserCallback) {
        database.collection(Constants.COLLECTIONS.USERS).document(id).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user: User = User.fromJSON(document.data ?: mapOf())
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun upsertUser(user: User, callback: BooleanCallback) {
        database.collection(Constants.COLLECTIONS.USERS).document(user.id).set(user.json)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun uploadImage(
        bitmap: Bitmap,
        folder: String,
        name: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val storageRef =
            storage.reference.child("$folder/${System.currentTimeMillis()}_$name.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri: Uri ->
                onSuccess(downloadUri.toString())
            }.addOnFailureListener { exception: Exception ->
                onError(exception)
            }
        }.addOnFailureListener { exception: Exception ->
            onError(exception)
        }
    }

    fun upsertUserWithImage(
        user: User,
        profileBitmap: Bitmap?,
        onComplete: (Boolean, User) -> Unit
    ) {
        if (profileBitmap == null) {
            upsertUser(user) { success ->
                onComplete(success, user)
            }
            return
        }
        uploadImage(profileBitmap, Constants.STORAGE.PROFILE_PICTURES, user.id, { url ->
            val userWithPic = user.copy(profilePicture = url)
            upsertUser(userWithPic) { success ->
                onComplete(success, userWithPic)
            }
        }, { _ ->
            onComplete(false, user)
        })
    }
}
