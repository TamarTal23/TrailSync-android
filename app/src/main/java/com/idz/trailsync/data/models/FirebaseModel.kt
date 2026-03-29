package com.idz.trailsync.data.models

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.idz.trailsync.base.BooleanCallback
import com.idz.trailsync.base.Constants
import com.idz.trailsync.base.PostsCallback
import com.idz.trailsync.base.UserCallback
import com.idz.trailsync.model.Comment
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
                    callback(database.collection(Constants.COLLECTIONS.USERS).document(uid))
                }
            }
            .addOnFailureListener {
                callback(database.collection(Constants.COLLECTIONS.USERS).document(uid))
            }
    }

    fun getPostsSince(since: Long, callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .whereGreaterThan(
                "updatedAt",
                Timestamp(since / 1000, ((since % 1000) * 1000000).toInt())
            )
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val posts = task.result.map { Post.fromJSON(it.data, it.id) }
                    callback(posts)
                } else {
                    callback(listOf())
                }
            }
    }

    fun getPostsPaged(
        limit: Long,
        lastDocument: DocumentSnapshot?,
        callback: (List<Post>, DocumentSnapshot?) -> Unit
    ) {
        var query = database.collection(Constants.COLLECTIONS.POSTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val posts = task.result.map { Post.fromJSON(it.data, it.id) }
                val lastVisible =
                    if (task.result.isEmpty) null else task.result.documents[task.result.size() - 1]
                callback(posts, lastVisible)
            } else {
                callback(listOf(), null)
            }
        }
    }

    fun getPostsByAuthor(authorId: String, callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .whereEqualTo("author", authorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val posts = task.result.map { Post.fromJSON(it.data, it.id) }
                    callback(posts)
                } else {
                    callback(listOf())
                }
            }
    }

    fun getUserById(id: String, callback: UserCallback) {
        database.collection(Constants.COLLECTIONS.USERS).document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) callback(User.fromJSON(doc.data ?: mapOf(), doc.id))
                else {
                    database.collection(Constants.COLLECTIONS.USERS).whereEqualTo("id", id).get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty) {
                                callback(
                                    User.fromJSON(
                                        snapshot.documents[0].data ?: mapOf(),
                                        snapshot.documents[0].id
                                    )
                                )
                            } else {
                                callback(null)
                            }
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
                if (task.isSuccessful) {
                    callback(true)
                    updateUserComments(user) {}
                } else {
                    callback(false)
                }
            }
        }
    }

    private fun updateUserComments(user: User, callback: () -> Unit) {
        database.collectionGroup(Comment.SUB_COLLECTION)
            .whereEqualTo("author", user.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    callback()
                    return@addOnSuccessListener
                }

                val batch = database.batch()
                for (document in querySnapshot.documents) {
                    batch.update(document.reference, "authorImage", user.profilePicture)
                }

                batch.commit().addOnCompleteListener {
                    callback()
                }
            }
            .addOnFailureListener {
                callback()
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
        val postRef = database.collection(Constants.COLLECTIONS.POSTS).document(comment.postId)
        val commentRef = postRef.collection(Comment.SUB_COLLECTION).document()

        database.runTransaction { transaction ->
            transaction.set(commentRef, comment.json)
            transaction.update(postRef, "updatedAt", FieldValue.serverTimestamp())
            transaction.update(postRef, "commentCount", FieldValue.increment(1))
            null
        }.addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    fun deletePost(postId: String, callback: BooleanCallback) {
        val postRef = database.collection(Constants.COLLECTIONS.POSTS).document(postId)

        postRef.collection(Comment.SUB_COLLECTION).get()
            .addOnSuccessListener { commentSnapshot ->
                val batch = database.batch()
                for (commentDoc in commentSnapshot.documents) {
                    batch.delete(commentDoc.reference)
                }
                batch.delete(postRef)

                batch.commit().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        database.collectionGroup(Constants.COLLECTIONS.SAVED_POSTS)
                            .whereEqualTo("postId", postId)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                if (!snapshot.isEmpty) {
                                    val cleanupBatch = database.batch()
                                    for (doc in snapshot.documents) {
                                        cleanupBatch.delete(doc.reference)
                                    }
                                    cleanupBatch.commit().addOnCompleteListener { cleanupTask ->
                                        callback(cleanupTask.isSuccessful)
                                    }
                                } else {
                                    callback(true)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(
                                    "FirebaseModel",
                                    "Failed to cleanup saved posts: ${e.message}"
                                )
                                callback(true)
                            }
                    } else {
                        callback(false)
                    }
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun savePost(userId: String, postId: String, callback: BooleanCallback) {
        getUserDocument(userId) { userRef ->
            val savedPost = SavedPost(postId = postId, userId = userId)
            val postRef = database.collection(Constants.COLLECTIONS.POSTS).document(postId)
            val savedPostRef =
                userRef.collection(Constants.COLLECTIONS.SAVED_POSTS).document(postId)

            database.runTransaction { transaction ->
                transaction.set(savedPostRef, savedPost.json)
                transaction.update(postRef, "savedCount", FieldValue.increment(1))
                transaction.update(postRef, "updatedAt", FieldValue.serverTimestamp())
                null
            }.addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
        }
    }

    fun unsavePost(userId: String, postId: String, callback: BooleanCallback) {
        getUserDocument(userId) { userRef ->
            val postRef = database.collection(Constants.COLLECTIONS.POSTS).document(postId)
            val savedPostRef =
                userRef.collection(Constants.COLLECTIONS.SAVED_POSTS).document(postId)

            database.runTransaction { transaction ->
                transaction.delete(savedPostRef)
                transaction.update(postRef, "savedCount", FieldValue.increment(-1))
                transaction.update(postRef, "updatedAt", FieldValue.serverTimestamp())
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
