package com.idz.trailsync.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.idz.trailsync.model.SavedPost

@Dao
interface SavedPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(vararg savedPost: SavedPost)

    @Query("SELECT * FROM SavedPost WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSavedPostsByUser(userId: String): List<SavedPost>

    @Query("SELECT * FROM SavedPost WHERE userId = :userId AND postId = :postId LIMIT 1")
    fun getSavedPost(userId: String, postId: String): SavedPost?

    @Query("DELETE FROM SavedPost WHERE userId = :userId AND postId = :postId")
    fun delete(userId: String, postId: String)

    @Query("DELETE FROM SavedPost WHERE userId = :userId")
    fun deleteAllByUser(userId: String)

    @Transaction
    fun clearAndInsertAll(userId: String, savedPosts: List<SavedPost>) {
        deleteAllByUser(userId)
        upsert(*savedPosts.toTypedArray())
    }
}
