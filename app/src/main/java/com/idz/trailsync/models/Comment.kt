package com.idz.trailsync.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Comment(
    @PrimaryKey val id: String,
    val text: String,
    val author: String,
    val postId: String,
    val createdAt: Date = Date()
) {

    companion object {
        const val SUB_COLLECTION = "comments"
        private const val TEXT_KEY = "text"
        private const val AUTHOR_KEY = "author"
        private const val CREATED_AT_KEY = "createdAt"

        fun fromJSON(json: Map<String, Any>, postId: String, docId: String): Comment {
            val text = json[TEXT_KEY] as? String ?: ""
            val author = json[AUTHOR_KEY] as? String ?: ""
            val createdAt = (json[CREATED_AT_KEY] as? com.google.firebase.Timestamp)?.toDate() ?: Date()

            return Comment(
                id = docId,
                text = text,
                author = author,
                postId = postId,
                createdAt = createdAt
            )
        }
    }

    val json: Map<String, Any>
        get() {
            return hashMapOf(
                TEXT_KEY to text,
                AUTHOR_KEY to author,
                CREATED_AT_KEY to com.google.firebase.Timestamp(createdAt)
            )
        }
}