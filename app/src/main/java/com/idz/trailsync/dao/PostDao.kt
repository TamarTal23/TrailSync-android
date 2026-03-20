package com.idz.trailsync.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.idz.trailsync.model.Post

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(vararg post: Post)

    @Query("SELECT * FROM Post ORDER BY createdAt DESC")
    fun getAll(): List<Post>

    @Query("SELECT * FROM Post WHERE author = :userId ORDER BY createdAt DESC")
    fun getPostsByAuthor(userId: String): List<Post>

    @Query("SELECT * FROM Post WHERE id = :postId")
    fun getById(postId: String): Post?

    @Query("DELETE FROM Post WHERE id = :postId")
    fun deleteById(postId: String)

    @Query("DELETE FROM Post")
    fun deleteAll()

    @Transaction
    fun clearAndInsertAll(posts: List<Post>) {
        deleteAll()
        upsert(*posts.toTypedArray())
    }
}
