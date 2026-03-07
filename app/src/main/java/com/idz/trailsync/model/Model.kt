package com.idz.trailsync.model

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.Constants
import com.idz.trailsync.base.UserCallback
import com.idz.trailsync.model.dao.AppLocalDB
import com.idz.trailsync.model.dao.AppLocalDbRepository
import com.idz.trailsync.base.UsersCallback
import java.util.concurrent.Executors

class Model private constructor() {
    private var executor = Executors.newSingleThreadExecutor()
    private val firebaseModel = FirebaseModel()
    private val database: AppLocalDbRepository = AppLocalDB.database

    companion object {
        val shared = Model()
    }

    fun getAllUsers(callback: UsersCallback) {
        executor.execute {
            val localUsers = database.UserDao().getAll()
            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                callback(localUsers)
            }
            firebaseModel.getAllUsers { remoteUsers ->
                executor.execute {
                    remoteUsers.forEach { database.UserDao().create(it) }
                    HandlerCompat.createAsync(Looper.getMainLooper()).post {
                        callback(remoteUsers)
                    }
                }
            }
        }
    }

    fun getUserByEmail(email: String, callback: UserCallback) {
        executor.execute {
            val localUser = database.UserDao().getByEmail(email)
            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                callback(localUser)
            }
            firebaseModel.getUserByEmail(email) { remoteUser ->
                executor.execute {
                    remoteUser?.let { database.UserDao().create(it) }
                    HandlerCompat.createAsync(Looper.getMainLooper()).post {
                        callback(remoteUser)
                    }
                }
            }
        }
    }

    fun upsertUser(user: User, picture: Bitmap?, callback: BooleanCallback) {
        executor.execute {
            database.UserDao().create(user)
            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                callback(true)
            }
        }
        val customCallback = { uri: String? ->
            if (!uri.isNullOrBlank()) {
                val updatedUser = user.copy(profilePicture = uri)
                firebaseModel.upsertUser(updatedUser) { success ->
                    callback(success)
                }
            } else {
                callback(false)
            }
        }

        firebaseModel.upsertUser(user) { success ->
            if (success) {
                picture?.let {
                    firebaseModel.uploadImage(
                        picture,
                        Constants.STORAGE.PROFILE_PICTURES,
                        user.id,
                        customCallback
                    ) { exception -> callback(false) }
                } ?: callback(true)
            } else {
                callback(false)
            }
        }
    }

}