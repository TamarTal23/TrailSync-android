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
        firebaseModel.getAllUsers(callback)
    }

    fun getUserByEmail(email: String, callback: UserCallback) {
        firebaseModel.getUserByEmail(email, callback)
    }

    fun upsertUser(user: User, picture: Bitmap?, callback: BooleanCallback) {
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