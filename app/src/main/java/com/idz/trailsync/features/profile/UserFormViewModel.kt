package com.idz.trailsync.features.profile

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ValidationState(
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isValid: Boolean = false
)

data class FormState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

class UserFormViewModel : ViewModel() {

    private var isRegistration: Boolean = true

    private val _formState = MutableLiveData(FormState())
    val formState: LiveData<FormState> = _formState

    private val _validationState = MutableLiveData(ValidationState())
    val validationState: LiveData<ValidationState> = _validationState

    fun setRegistration(isRegistration: Boolean) {
        this.isRegistration = isRegistration
        validate()
    }

    fun updateEmail(email: String) {
        _formState.value = _formState.value?.copy(email = email)
        validate()
    }

    fun updateUsername(username: String) {
        _formState.value = _formState.value?.copy(username = username)
        validate()
    }

    fun updatePassword(password: String) {
        _formState.value = _formState.value?.copy(password = password)
        validate()
    }

    fun updateConfirmPassword(confirm: String) {
        _formState.value = _formState.value?.copy(confirmPassword = confirm)
        validate()
    }

    private fun validate() {
        val form = _formState.value ?: return

        var emailError: String? = null
        var usernameError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null

        if (form.email.isBlank()) {
            emailError = "Email required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(form.email).matches()) {
            emailError = "Invalid email"
        }

        if (isRegistration) {
            if (form.username.isBlank()) {
                usernameError = "Username required"
            }

            if (form.password.length < 6) {
                passwordError = "Password must be at least 6 characters"
            }

            if (form.password != form.confirmPassword) {
                confirmPasswordError = "Passwords do not match"
            }
        } else {
            if (form.password.isBlank()) {
                passwordError = "Password required"
            }
        }

        val valid = emailError == null &&
                usernameError == null &&
                passwordError == null &&
                confirmPasswordError == null

        _validationState.value = ValidationState(
            emailError = emailError,
            usernameError = usernameError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            isValid = valid
        )
    }

    fun isFormValid(): Boolean {
        return validationState.value?.isValid ?: false
    }
}
