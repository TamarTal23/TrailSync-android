package com.idz.trailsync

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.idz.trailsync.model.Model
import com.idz.trailsync.model.User

/**
 * A wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

sealed class LoginResult {
    object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
    object EmptyFields : LoginResult()
}

class AuthenticationViewModel : ViewModel() {
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _registrationResult = MutableLiveData<LoginResult>()
    val registrationResult: LiveData<LoginResult> = _registrationResult

    private val _updateProfileResult = MutableLiveData<Event<LoginResult>?>()
    val updateProfileResult: LiveData<Event<LoginResult>?> = _updateProfileResult

    fun login(email: String, password: String) {
        val auth = Firebase.auth
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _loginResult.value = LoginResult.Success
                    } else {
                        val exception = task.exception
                        _loginResult.value = LoginResult.Error(
                            if (exception is FirebaseAuthUserCollisionException)
                                "This email is already registered."
                            else
                                exception?.message ?: "Login failed"
                        )
                    }
                }
        } else {
            _loginResult.value = LoginResult.EmptyFields
        }
    }

    fun register(email: String, username: String, password: String, profileBitmap: Bitmap?) {
        val auth = Firebase.auth
        if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && profileBitmap != null) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: ""
                        val user = User(
                            id = uid,
                            email = email,
                            username = username,
                            profilePicture = null
                        )
                        Model.shared.upsertUser(
                            user,
                            profileBitmap
                        ) { success ->
                            if (success) {
                                _registrationResult.value = LoginResult.Success
                            } else {
                                _registrationResult.value =
                                    LoginResult.Error("Failed to save user to database")
                            }
                        }
                    } else {
                        val exception = task.exception
                        if (exception is FirebaseAuthUserCollisionException) {
                            _registrationResult.value =
                                LoginResult.Error("This email is already registered.")
                        } else {
                            _registrationResult.value =
                                LoginResult.Error(exception?.message ?: "Registration failed")
                        }
                    }
                }
        } else {
            _registrationResult.value = LoginResult.EmptyFields
        }
    }

    fun updateProfile(newUsername: String?, newProfileBitmap: Bitmap?) {
        val authUser = Firebase.auth.currentUser
        if (authUser == null) {
            _updateProfileResult.value = Event(LoginResult.Error("User not logged in"))
            return
        }

        Model.shared.getUserById(authUser.uid) { user ->
            if (user != null) {
                val updatedUser = user.copy(
                    username = newUsername ?: user.username
                )
                Model.shared.upsertUser(updatedUser, newProfileBitmap) { success ->
                    if (success) {
                        _updateProfileResult.value = Event(LoginResult.Success)
                    } else {
                        _updateProfileResult.value = Event(LoginResult.Error("Failed to update profile in database"))
                    }
                }
            } else {
                _updateProfileResult.value = Event(LoginResult.Error("User record not found"))
            }
        }
    }

    fun clearUpdateProfileResult() {
        _updateProfileResult.value = null
    }

    fun isUserLoggedIn(): Boolean {
        return Firebase.auth.currentUser != null
    }

    fun logout() {
        Firebase.auth.signOut()
    }
}
