package com.idz.trailsync.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID

@Entity
data class SavedPost(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val userId: String,
    val createdAt: Date = Date()
) {
    companion object {
        private const val POST_ID_KEY = "postId"
        private const val CREATED_AT_KEY = "createdAt"

        fun fromJSON(json: Map<String, Any>, userId: String, docId: String): SavedPost {
            val postId = json[POST_ID_KEY] as? String ?: ""
            val createdAt =
                (json[CREATED_AT_KEY] as? Timestamp)?.toDate() ?: Date()

            return SavedPost(
                id = docId,
                postId = postId,
                userId = userId,
                createdAt = createdAt
            )
        }
    }

    val json: Map<String, Any>
        get() {
            return hashMapOf(
                POST_ID_KEY to postId,
                CREATED_AT_KEY to Timestamp(createdAt)
            )
        }
}