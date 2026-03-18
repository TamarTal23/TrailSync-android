package com.idz.trailsync.base

import com.idz.trailsync.model.Post
import com.idz.trailsync.model.User

typealias UsersCallback = (List<User>) -> Unit
typealias UserCallback = (User?) -> Unit
typealias BooleanCallback = (Boolean) -> Unit
typealias StringCallback = (String?) -> Unit
typealias PostsCallback = (List<Post>) -> Unit

object Constants {
    object COLLECTIONS {
        const val USERS = "users"
        const val COMMENTS = "comments"
        const val POSTS = "posts"
        const val SAVED_POSTS = "savedPosts"
    }

    object STORAGE {
        const val PROFILE_PICTURES = "profiles"
        const val POSTS_PICTURES = "posts"
    }
}