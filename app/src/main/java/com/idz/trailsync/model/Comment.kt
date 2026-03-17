package com.idz.trailsync.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.util.Date

@Entity
data class Comment(
    @PrimaryKey val id: String,
    val text: String,
    val author: String, // This is the user ID
    val authorName: String, // This is the username
    val authorImage: String? = null,
    val postId: String,
    val createdAt: Date = Date()
) {

    companion object {
        const val SUB_COLLECTION = "comments"
        private const val TEXT_KEY = "text"
        private const val AUTHOR_KEY = "author"
        private const val AUTHOR_NAME_KEY = "authorName"
        private const val AUTHOR_IMAGE_KEY = "authorImage"
        private const val CREATED_AT_KEY = "createdAt"

        fun fromJSON(json: Map<String, Any>, postId: String, docId: String): Comment {
            val text = json[TEXT_KEY] as? String ?: ""
            val author = json[AUTHOR_KEY] as? String ?: ""
            val authorName = json[AUTHOR_NAME_KEY] as? String ?: ""
            val authorImage = json[AUTHOR_IMAGE_KEY] as? String
            val createdAt = (json[CREATED_AT_KEY] as? Timestamp)?.toDate() ?: Date()

            return Comment(
                id = docId,
                text = text,
                author = author,
                authorName = authorName,
                authorImage = authorImage,
                postId = postId,
                createdAt = createdAt
            )
        }
    }

    val json: Map<String, Any>
        get() {
            val map = hashMapOf(
                TEXT_KEY to text,
                AUTHOR_KEY to author,
                AUTHOR_NAME_KEY to authorName,
                CREATED_AT_KEY to Timestamp(createdAt)
            )
            authorImage?.let { map[AUTHOR_IMAGE_KEY] = it }
            return map
        }
}