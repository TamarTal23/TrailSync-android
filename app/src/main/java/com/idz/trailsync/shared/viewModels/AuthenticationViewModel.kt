package com.idz.trailsync.shared.viewModels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.idz.trailsync.model.User
import com.idz.trailsync.data.repository.UserRepository

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
}

sealed class LoginResult {
    object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
    object EmptyFields : LoginResult()
}

class AuthenticationViewModel : ViewModel() {
    private val userRepository = UserRepository.shared
    
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _registrationResult = MutableLiveData<LoginResult>()
    val registrationResult: LiveData<LoginResult> = _registrationResult

    private val _updateProfileResult = MutableLiveData<Event<LoginResult>?>()
    val updateProfileResult: LiveData<Event<LoginResult>?> = _updateProfileResult

    private val _currentUserId = MutableLiveData<String?>(Firebase.auth.currentUser?.uid)
    val currentUserId: LiveData<String?> = _currentUserId

    private val _currentUserProfile = MutableLiveData<User?>()
    val currentUserProfile: LiveData<User?> = _currentUserProfile

    init {
        fetchCurrentUserProfile()
    }

    fun fetchCurrentUserProfile() {
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            userRepository.getUserById(uid) { user ->
                _currentUserProfile.postValue(user)
            }
        } else {
            _currentUserProfile.value = null
        }
    }

    fun login(email: String, password: String) {
        val auth = Firebase.auth
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        _currentUserId.value = uid
                        fetchCurrentUserProfile()
                        _loginResult.value = LoginResult.Success
                    } else {
                        val exception = task.exception
                        val message = when (exception) {
                            is FirebaseAuthInvalidUserException -> "No account found with this email."
                            is FirebaseAuthInvalidCredentialsException -> "Incorrect password."
                            else -> exception?.message ?: "Login failed. Please try again."
                        }
                        _loginResult.value = LoginResult.Error(message)
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
                        _currentUserId.value = uid
                        val user = User(id = uid, email = email, username = username, profilePicture = null)
                        userRepository.upsertUser(user, profileBitmap) { success ->
                            if (success) {
                                fetchCurrentUserProfile()
                                _registrationResult.value = LoginResult.Success 
                            } else {
                                _registrationResult.value = LoginResult.Error("Failed to save user to database")
                            }
                        }
                    } else {
                        val exception = task.exception
                        val message = if (exception is FirebaseAuthUserCollisionException)
                            "This email is already registered."
                        else
                            exception?.message ?: "Registration failed"
                        
                        _registrationResult.value = LoginResult.Error(message)
                    }
                }
        } else {
            _registrationResult.value = LoginResult.EmptyFields
        }
    }

    fun updateProfile(newUsername: String?, newProfileBitmap: Bitmap?) {
        val uid = _currentUserId.value ?: run {
            _updateProfileResult.value = Event(LoginResult.Error("User not logged in"))
            return
        }

        userRepository.getUserById(uid) { user ->
            if (user == null) return@getUserById

            val updatedUser = user.copy(username = newUsername ?: user.username)
            userRepository.upsertUser(updatedUser, newProfileBitmap) { success ->
                if (success) {
                    fetchCurrentUserProfile()
                    _updateProfileResult.value = Event(LoginResult.Success)
                } else {
                    _updateProfileResult.value = Event(LoginResult.Error("Failed to update profile"))
                }
            }
        }
    }

    fun clearUpdateProfileResult() {
        _updateProfileResult.value = null
    }

    fun isUserLoggedIn(): Boolean = _currentUserId.value != null
    
    fun logout() {
        Firebase.auth.signOut()
        _currentUserId.value = null
        _currentUserProfile.value = null
    }
}
