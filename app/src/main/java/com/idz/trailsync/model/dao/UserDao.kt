package com.idz.trailsync.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.idz.trailsync.model.User

@Dao
interface UserDao {
    @Insert
    fun create(vararg user: User)

    @Query("SELECT * FROM User")
    fun getAll(): List<User>
}