package com.idz.trailsync.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.idz.trailsync.model.Post
import com.idz.trailsync.model.PostWithComments

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(post: Post): Long

    @Update
    fun update(post: Post)

    @Transaction
    fun upsert(post: Post) {
        if (insert(post) == -1L) {
            update(post)
        }
    }

    @Transaction
    @Query("SELECT * FROM Post ORDER BY createdAt DESC")
    fun getAllWithComments(): LiveData<List<PostWithComments>>

    @Transaction
    @Query("SELECT * FROM Post WHERE author = :userId ORDER BY createdAt DESC")
    fun getPostsByAuthorWithComments(userId: String): LiveData<List<PostWithComments>>

    @Query("SELECT * FROM Post WHERE id = :postId")
    fun getById(postId: String): Post?

    @Query("UPDATE Post SET commentsLoaded = :loaded WHERE id = :postId")
    fun updateCommentsLoaded(postId: String, loaded: Boolean)

    @Query("DELETE FROM Post WHERE id = :postId")
    fun deleteById(postId: String)

    @Query("DELETE FROM Post")
    fun deleteAll()
}
