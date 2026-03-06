package com.idz.trailsync.model

import android.graphics.Bitmap
import android.os.Looper
import androidx.core.os.HandlerCompat
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.UserCallback
import com.idz.trailsync.model.dao.AppLocalDB
import com.idz.trailsync.model.dao.AppLocalDbRepository
import com.idz.trailsync.base.UsersCallback
import java.util.concurrent.Executors

class Model private constructor() {
    private var executor = Executors.newSingleThreadExecutor()
    private val firebaseModel = FirebaseModel()

    private val database: AppLocalDbRepository = AppLocalDB.database
    private var mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    companion object {
        val shared = Model()
    }

    fun getAllUsers(callback: UsersCallback) {
        firebaseModel.getAllUsers(callback)
    }

    fun getUserByEmail(email: String, callback: UserCallback) {
        firebaseModel.getUserByEmail(email, callback)
    }

    // todo handle image upload
    fun upsertUser(user: User, callback: BooleanCallback) {
        firebaseModel.upsertUser(user) { success ->
            callback(success)
        }
    }

}