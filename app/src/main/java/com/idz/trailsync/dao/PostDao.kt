package com.idz.trailsync.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.idz.trailsync.model.Post

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(vararg post: Post)

    @Query("SELECT * FROM Post")
    fun getAll(): List<Post>

    @Query("SELECT * FROM Post WHERE author = :userId")
    fun getPostsByAuthor(userId: String): List<Post>
}