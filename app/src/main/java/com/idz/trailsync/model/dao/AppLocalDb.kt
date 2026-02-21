package com.idz.trailsync.model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.idz.trailsync.base.MyApplication
import com.idz.trailsync.model.Post
import com.idz.trailsync.model.Comment
import com.idz.trailsync.model.User

@Database(entities = [User::class, Comment:: class, Post::class ], version = 5)
abstract class AppLocalDbRepository: RoomDatabase(){
    abstract fun UserDao(): UserDao
// TODO: Add PostDao and CommentDao when they are implemented
}

object AppLocalDB{
    val database: AppLocalDbRepository by lazy {
        val context = MyApplication.Globals.context?:throw IllegalArgumentException("Application context is missing")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "dbFileName.db"
        ).fallbackToDestructiveMigration().build()
    }
}