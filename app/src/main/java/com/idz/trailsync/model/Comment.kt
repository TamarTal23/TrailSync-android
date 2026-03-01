package com.idz.trailsync.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Comment(
    @PrimaryKey val id: String,
    val text: String,
    val author: String,
    val post: String,
) {

    companion object {
        private const val ID_KEY = "id"
        private const val TEXT_KEY = "text"
        private const val AUTHOR_KEY = "author"
        private const val POST_KEY = "post"

        fun fromJSON(json: Map<String, Any>): Comment {
            val id = json[ID_KEY] as? String ?: UUID.randomUUID().toString()
            val text = json[TEXT_KEY] as? String ?: ""
            val author = json[AUTHOR_KEY] as? String ?: ""
            val post = json[POST_KEY] as? String ?: ""

            return Comment(
                id = id,
                text = text,
                author = author,
                post = post
            )
        }
    }

    val json: Map<String, Any>
        get() {
            return hashMapOf(
                ID_KEY to id,
                TEXT_KEY to text,
                POST_KEY to post,
                AUTHOR_KEY to author
            )
        }
}