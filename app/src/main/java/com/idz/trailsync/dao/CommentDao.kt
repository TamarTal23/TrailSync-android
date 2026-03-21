package com.idz.trailsync.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.idz.trailsync.model.Comment

@Dao
interface CommentDao {
    @Query("SELECT * FROM Comment WHERE postId = :postId ORDER BY createdAt DESC")
    fun getCommentsForPost(postId: String): LiveData<List<Comment>>

    @Query("SELECT id FROM Comment WHERE postId = :postId")
    fun getCommentIdsForPost(postId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg comments: Comment)

    @Query("DELETE FROM Comment WHERE id IN (:ids)")
    fun deleteByIds(ids: List<String>)

    @Transaction
    fun syncCommentsForPost(postId: String, remoteComments: List<Comment>) {
        val localIds = getCommentIdsForPost(postId)
        val remoteIds = remoteComments.map { it.id }.toSet()
        val idsToDelete = localIds.filter { it !in remoteIds }

        if (idsToDelete.isNotEmpty()) {
            deleteByIds(idsToDelete)
        }
        
        if (remoteComments.isNotEmpty()) {
            insertAll(*remoteComments.toTypedArray())
        }
    }
}
