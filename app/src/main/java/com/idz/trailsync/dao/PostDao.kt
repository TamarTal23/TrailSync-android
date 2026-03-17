package com.idz.trailsync.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.idz.trailsync.model.Post
import com.idz.trailsync.model.PostWithComments

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(vararg post: Post)

    @Transaction
    @Query("SELECT * FROM Post ORDER BY createdAt DESC")
    fun getAllWithComments(): LiveData<List<PostWithComments>>

    @Transaction
    @Query("SELECT * FROM Post WHERE author = :userId ORDER BY createdAt DESC")
    fun getPostsByAuthorWithComments(userId: String): LiveData<List<PostWithComments>>
    
    @Query("SELECT * FROM Post WHERE id = :postId")
    fun getPostById(postId: String): Post?
}