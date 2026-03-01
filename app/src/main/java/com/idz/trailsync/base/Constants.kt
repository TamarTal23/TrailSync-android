package com.idz.trailsync.base

import com.idz.trailsync.model.User

typealias UsersCallback = (List<User>) -> Unit

object Constants {
    object COLLECTIONS {
        const val USERS = "users"
        const val COMMENTS = "comments"
        const val POSTS = "posts"
    }
}