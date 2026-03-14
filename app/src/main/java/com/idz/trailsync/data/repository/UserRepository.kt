package com.idz.trailsync.data.repository

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.UserCallback
import com.idz.trailsync.base.UsersCallback
import com.idz.trailsync.dao.AppLocalDB
import com.idz.trailsync.dao.AppLocalDbRepository
import com.idz.trailsync.data.models.FirebaseModel
import com.idz.trailsync.data.models.FirebaseStorageModel
import com.idz.trailsync.model.User
import java.util.concurrent.Executors

class UserRepository private constructor() {
    private var executor = Executors.newSingleThreadExecutor()
    private val firebaseModel = FirebaseModel()
    private val firebaseStorageModel = FirebaseStorageModel()
    private val database: AppLocalDbRepository = AppLocalDB.database

    companion object {
        val shared = UserRepository()
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

    fun getUserByEmail(email: String?, callback: UserCallback) {
        executor.execute {
            val localUser = database.UserDao().getByEmail(email)
            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                callback(localUser)
            }
            firebaseModel.getUserByEmail(email ?: "") { remoteUser ->
                if (remoteUser != null) {
                    executor.execute {
                        database.UserDao().create(remoteUser)
                        HandlerCompat.createAsync(Looper.getMainLooper()).post {
                            callback(remoteUser)
                        }
                    }
                }
            }
        }
    }

    fun getUserById(id: String, callback: UserCallback) {
        executor.execute {
            val localUser = database.UserDao().getById(id)
            HandlerCompat.createAsync(Looper.getMainLooper()).post {
                callback(localUser)
            }
            firebaseModel.getUserById(id) { remoteUser ->
                if (remoteUser != null) {
                    executor.execute {
                        database.UserDao().create(remoteUser)
                        HandlerCompat.createAsync(Looper.getMainLooper()).post {
                            callback(remoteUser)
                        }
                    }
                }
            }
        }
    }

    fun upsertUser(user: User, picture: Bitmap?, callback: BooleanCallback) {
        if (picture != null) {
            firebaseStorageModel.uploadUserImage(picture, user.id) { url ->
                if (url != null) {
                    val updatedUser = user.copy(profilePicture = url)
                    syncUserToDatabases(updatedUser, callback)
                } else {
                    HandlerCompat.createAsync(Looper.getMainLooper()).post {
                        callback(false)
                    }
                }
            }
        } else {
            syncUserToDatabases(user, callback)
        }
    }

    private fun syncUserToDatabases(user: User, callback: BooleanCallback) {
        firebaseModel.upsertUser(user) { success ->
            if (success) {
                executor.execute {
                    database.UserDao().create(user)
                    HandlerCompat.createAsync(Looper.getMainLooper()).post {
                        callback(true)
                    }
                }
            } else {
                HandlerCompat.createAsync(Looper.getMainLooper()).post {
                    callback(false)
                }
            }
        }
    }
}
