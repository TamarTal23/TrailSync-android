package com.idz.trailsync.data.models

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.UsersCallback
import com.idz.trailsync.base.Constants
import com.idz.trailsync.base.PostsCallback
import com.idz.trailsync.base.UserCallback
import com.idz.trailsync.model.Post
import com.idz.trailsync.model.SavedPost
import com.idz.trailsync.model.User

class FirebaseModel {
    private val database = Firebase.firestore

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }
        database.firestoreSettings = setting
    }

    private fun getUserDocument(uid: String, callback: (DocumentReference) -> Unit) {
        database.collection(Constants.COLLECTIONS.USERS)
            .whereEqualTo("id", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    callback(snapshot.documents[0].reference)
                } else {
                    // Fallback to UID as document ID if no document with field id=uid exists
                    callback(database.collection(Constants.COLLECTIONS.USERS).document(uid))
                }
            }
            .addOnFailureListener {
                callback(database.collection(Constants.COLLECTIONS.USERS).document(uid))
            }
    }

    fun getAllUsers(callback: UsersCallback) {
        database.collection(Constants.COLLECTIONS.USERS).get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val users: MutableList<User> = mutableListOf()
                        for (doc in it.result) {
                            users.add(User.fromJSON(doc.data))
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
        database.collection(Constants.COLLECTIONS.USERS).whereEqualTo("id", id).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    callback(User.fromJSON(snapshot.documents[0].data ?: mapOf()))
                } else {
                    database.collection(Constants.COLLECTIONS.USERS).document(id).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) callback(User.fromJSON(doc.data ?: mapOf()))
                            else callback(null)
                        }
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun upsertUser(user: User, callback: BooleanCallback) {
        getUserDocument(user.id) { docRef ->
            docRef.set(user.json).addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
        }
    }

    fun upsertPost(post: Post, callback: BooleanCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(post.id).set(post.json)
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

    fun savePost(userId: String, postId: String, callback: BooleanCallback) {
        getUserDocument(userId) { userRef ->
            val savedPost = SavedPost(postId = postId, userId = userId)
            val postRef = database.collection(Constants.COLLECTIONS.POSTS).document(postId)
            val savedPostRef = userRef.collection(Constants.COLLECTIONS.SAVED_POSTS).document(postId)

            database.runTransaction { transaction ->
                transaction.set(savedPostRef, savedPost.json)
                transaction.update(postRef, "savedCount", FieldValue.increment(1))
                null
            }.addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
        }
    }

    fun unsavePost(userId: String, postId: String, callback: BooleanCallback) {
        getUserDocument(userId) { userRef ->
            val postRef = database.collection(Constants.COLLECTIONS.POSTS).document(postId)
            val savedPostRef = userRef.collection(Constants.COLLECTIONS.SAVED_POSTS).document(postId)

            database.runTransaction { transaction ->
                transaction.delete(savedPostRef)
                transaction.update(postRef, "savedCount", FieldValue.increment(-1))
                null
            }.addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
        }
    }

    fun getSavedPostsForUser(userId: String, callback: (List<SavedPost>) -> Unit) {
        getUserDocument(userId) { userRef ->
            userRef.collection(Constants.COLLECTIONS.SAVED_POSTS).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val savedPosts = task.result.map {
                            SavedPost.fromJSON(it.data, userId, it.id)
                        }
                        callback(savedPosts)
                    } else {
                        callback(listOf())
                    }
                }
        }
    }
}
