package com.idz.trailsync.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User (
    @PrimaryKey val id: String,
    val email: String,
    val username: String,
    val password: String,
    val profilePicture: String? = null,
) {
    companion object {
        private const val ID_KEY = "id"
        private const val EMAIL_KEY = "email"
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"
        private const val PROFILE_PICTURE_KEY = "profilePicture"

        fun fromJSON(json: Map<String, Any>): User {
            val email = json[EMAIL_KEY] as? String ?: ""
            val password = json[PASSWORD_KEY] as? String ?: ""
            val id = json[ID_KEY] as? String ?: ""
            val profilePicture = json[PROFILE_PICTURE_KEY] as? String ?: ""
            val username = json[USERNAME_KEY] as? String ?: ""

            return User(
                email = email,
                password = password,
                id = id,
                profilePicture = profilePicture,
                username = username
            )
        }
    }
    val json: Map<String, String?>
        get() {
            return hashMapOf(
                EMAIL_KEY to email,
                PASSWORD_KEY to password,
                ID_KEY to id,
                PROFILE_PICTURE_KEY to profilePicture,
                USERNAME_KEY to username
            )
        }
}