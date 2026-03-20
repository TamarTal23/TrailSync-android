package com.idz.trailsync.data.models

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.UsersCallback
import com.idz.trailsync.base.Constants
import com.idz.trailsync.base.PostsCallback
import com.idz.trailsync.base.UserCallback
import com.idz.trailsync.model.Comment
import com.idz.trailsync.model.Post
import com.idz.trailsync.model.User

class FirebaseModel {
    private val database = Firebase.firestore

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }
        database.firestoreSettings = setting
    }

    fun getAllUsers(callback: UsersCallback) {
        database.collection(Constants.COLLECTIONS.USERS).get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val users: MutableList<User> = mutableListOf()
                        for (json in it.result) {
                            users.add(User.fromJSON(json.data))
                        }
                        callback(users)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun getAllPosts(callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val posts: MutableList<Post> = mutableListOf()
                    for (json in it.result) {
                        posts.add(Post.fromJSON(json.data))
                    }
                    callback(posts)
                } else {
                    callback(listOf())
                }
            }
    }

    fun getPostsByAuthor(authorId: String, callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .whereEqualTo("author", authorId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val posts = task.result.map { Post.fromJSON(it.data) }
                    callback(posts)
                } else {
                    callback(listOf())
                }
            }
    }

    fun getUserByEmail(email: String, callback: UserCallback) {
        database.collection(Constants.COLLECTIONS.USERS).whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user: User = User.fromJSON(documents.documents[0].data ?: mapOf())
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun getUserById(id: String, callback: UserCallback) {
        database.collection(Constants.COLLECTIONS.USERS).document(id).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user: User = User.fromJSON(document.data ?: mapOf())
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun upsertUser(user: User, callback: BooleanCallback) {
        database.collection(Constants.COLLECTIONS.USERS).document(user.id).set(user.json)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }


    fun upsertPost(post: Post, callback: BooleanCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(post.id).set(post.json)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun getCommentsForPost(postId: String, callback: (List<Comment>) -> Unit) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .document(postId)
            .collection(Comment.SUB_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                val comments = result.map { doc ->
                    Comment.fromJSON(doc.data, postId, doc.id)
                }
                callback(comments)
            }
            .addOnFailureListener {
                callback(listOf())
            }
    }

    fun addComment(comment: Comment, callback: BooleanCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .document(comment.postId)
            .collection(Comment.SUB_COLLECTION)
            .add(comment.json)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun deletePost(postId: String, callback: BooleanCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(postId).delete()
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }
}
