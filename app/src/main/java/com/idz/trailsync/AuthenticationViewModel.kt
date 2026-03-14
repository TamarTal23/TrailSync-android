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

open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

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
                        Model.shared.upsertUser(user, profileBitmap) { success ->
                            _registrationResult.value = if (success) LoginResult.Success
                            else LoginResult.Error("Failed to save user to database")
                        }
                    } else {
                        val exception = task.exception
                        _registrationResult.value = LoginResult.Error(
                            if (exception is FirebaseAuthUserCollisionException) "This email is already registered."
                            else exception?.message ?: "Registration failed"
                        )
                    }
                }
        } else {
            _registrationResult.value = LoginResult.EmptyFields
        }
    }

    fun updateProfile(newUsername: String?, newProfileBitmap: Bitmap?) {
        val authUser = Firebase.auth.currentUser ?: run {
            _updateProfileResult.value = Event(LoginResult.Error("User not logged in"))
            return
        }

        var isUpdateStarted = false
        Model.shared.getUserById(authUser.uid) { user ->
            if (isUpdateStarted || user == null) return@getUserById
            isUpdateStarted = true

            val updatedUser = user.copy(username = newUsername ?: user.username)
            Model.shared.upsertUser(updatedUser, newProfileBitmap) { success ->
                _updateProfileResult.value = if (success) Event(LoginResult.Success)
                else Event(LoginResult.Error("Failed to update profile"))
            }
        }
    }

    fun clearUpdateProfileResult() {
        _updateProfileResult.value = null
    }

    fun isUserLoggedIn(): Boolean = Firebase.auth.currentUser != null
    fun logout() = Firebase.auth.signOut()
}
