package com.idz.trailsync.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.idz.trailsync.models.Post

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun create(vararg post: Post)

}