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
                for (document in documents) {
                    val user: User = User.fromJSON(document.data ?: mapOf())
                    callback(user)
                }
            }
    }

    fun upsertUser(user: User, callback: BooleanCallback) {
        database.collection(Constants.COLLECTIONS.USERS).whereEqualTo("email", user.email).get()
            .addOnSuccessListener { documents ->
                if (documents.size() == 0) {
                    database.collection(Constants.COLLECTIONS.USERS).document()
                        .set(user.json).addOnSuccessListener {
                            callback(true)
                        }
                } else {
                    for (document in documents) {
                        document.reference.update(user.json).addOnSuccessListener {
                            callback(true)
                        }
                    }
                }

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
            FirebaseStorage.getInstance().reference.child("$folder/${System.currentTimeMillis()}_$name.jpg")
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
        callback: BooleanCallback
    ) {
        if (profileBitmap == null) {
            callback(false)
            return
        }
        uploadImage(profileBitmap, "profilePictures", user.id ?: "", { url ->
            val userWithPic = user.copy(profilePicture = url)
            database.collection(Constants.COLLECTIONS.USERS).whereEqualTo("email", user.email).get()
                .addOnSuccessListener { documents ->
                    if (documents.size() == 0) {
                        database.collection(Constants.COLLECTIONS.USERS).document()
                            .set(userWithPic.json).addOnSuccessListener {
                                callback(true)
                            }
                    } else {
                        for (document in documents) {
                            document.reference.update(userWithPic.json).addOnSuccessListener {
                                callback(true)
                            }
                        }
                    }
                }
        }, { _ ->
            callback(false)
        })
    }
}