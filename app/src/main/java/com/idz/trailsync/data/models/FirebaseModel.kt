package com.idz.trailsync.data.models

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.UsersCallback
import com.idz.trailsync.base.Constants
import com.idz.trailsync.base.UserCallback
import com.idz.trailsync.model.Post
import com.idz.trailsync.model.User

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
                if (documents.isEmpty) {
                    callback(null)
                } else {
                    val user: User = User.fromJSON(documents.documents[0].data ?: mapOf())
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

    fun upsertPost(post: Post, callback: BooleanCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .whereEqualTo("id", post.id)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    database.collection(Constants.COLLECTIONS.POSTS)
                        .document()
                        .set(post.json)
                        .addOnSuccessListener {
                            callback(true)
                        }
                        .addOnFailureListener {
                            callback(false)
                        }
                } else {
                    for (document in documents) {
                        document.reference.update(post.json)
                            .addOnSuccessListener {
                                callback(true)
                            }
                            .addOnFailureListener {
                                callback(false)
                            }
                    }
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}
