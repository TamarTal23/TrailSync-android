package com.idz.trailsync.model

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.idz.trailsync.base.UsersCallback
import com.idz.trailsync.base.Constants
import com.idz.trailsync.base.UserCallback

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
        database.collection(Constants.COLLECTIONS.USERS).whereEqualTo("email",email).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user : User = User.fromJSON(document.data ?: mapOf())
                    callback(user)
                }
            }
    }
}