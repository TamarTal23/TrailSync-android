package com.idz.trailsync.features.EditProfile

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class FormMode {
    REGISTRATION, LOGIN, EDIT_PROFILE
}

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
    val confirmPassword: String = "",
    val emailTouched: Boolean = false,
    val usernameTouched: Boolean = false,
    val passwordTouched: Boolean = false,
    val confirmPasswordTouched: Boolean = false
)

class UserFormViewModel : ViewModel() {

    private var currentMode: FormMode = FormMode.REGISTRATION

    private val _formState = MutableLiveData(FormState())
    val formState: LiveData<FormState> = _formState

    private val _validationState = MutableLiveData(ValidationState())
    val validationState: LiveData<ValidationState> = _validationState

    fun setMode(mode: FormMode) {
        this.currentMode = mode
        _formState.value = FormState()
        validate()
    }

    fun setRegistration(isRegistration: Boolean) {
        setMode(if (isRegistration) FormMode.REGISTRATION else FormMode.LOGIN)
    }

    fun updateEmail(email: String) {
        _formState.value = _formState.value?.copy(email = email, emailTouched = true)
        validate()
    }

    fun updateUsername(username: String) {
        _formState.value = _formState.value?.copy(username = username, usernameTouched = true)
        validate()
    }

    fun updatePassword(password: String) {
        _formState.value = _formState.value?.copy(password = password, passwordTouched = true)
        validate()
    }

    fun updateConfirmPassword(confirm: String) {
        _formState.value =
            _formState.value?.copy(confirmPassword = confirm, confirmPasswordTouched = true)
        validate()
    }

    fun touchAll() {
        _formState.value = _formState.value?.copy(
            emailTouched = true,
            usernameTouched = true,
            passwordTouched = true,
            confirmPasswordTouched = true
        )
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

        when (currentMode) {
            FormMode.REGISTRATION -> {
                if (form.username.isBlank()) usernameError = "Username required"
                if (form.password.length < 6) passwordError =
                    "Password must be at least 6 characters"
                if (form.password != form.confirmPassword) confirmPasswordError =
                    "Passwords do not match"
            }

            FormMode.LOGIN -> {
                if (form.password.isBlank()) passwordError = "Password required"
            }

            FormMode.EDIT_PROFILE -> {
                if (form.username.isBlank()) usernameError = "Username required"
            }
        }

        val actualIsValid = emailError == null &&
                usernameError == null &&
                passwordError == null &&
                confirmPasswordError == null

        _validationState.value = ValidationState(
            emailError = if (form.emailTouched) emailError else null,
            usernameError = if (form.usernameTouched) usernameError else null,
            passwordError = if (form.passwordTouched) passwordError else null,
            confirmPasswordError = if (form.confirmPasswordTouched) confirmPasswordError else null,
            isValid = actualIsValid
        )
    }

    fun isFormValid(): Boolean {
        return validationState.value?.isValid ?: false
    }
}
