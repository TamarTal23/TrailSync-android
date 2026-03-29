package com.idz.trailsync.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.idz.trailsync.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun create(vararg user: User)

    @Query("SELECT * FROM User WHERE id=:id")
    fun getById(id: String): User?
}
