package com.idz.trailsync

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.idz.trailsync.features.profile.UserFormViewModel
import com.idz.trailsync.utils.BitmapUtils

class RegisterFragment : Fragment() {
    private var profileBitmap: Bitmap? = null
    private lateinit var imageView: ShapeableImageView
    private val authenticationViewModel: AuthenticationViewModel by activityViewModels()
    private val userFormViewModel: UserFormViewModel by activityViewModels()
    private val bitmapUtils = BitmapUtils()

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val rotatedBitmap = bitmapUtils.getRotatedBitmap(uri, requireActivity().contentResolver)
            if (rotatedBitmap != null) {
                imageView.setImageBitmap(rotatedBitmap)
                profileBitmap = rotatedBitmap
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        imageView = view.findViewById(R.id.profileImageView)
        val emailEditText: EditText = view.findViewById(R.id.editTextEmail)
        val usernameEditText: EditText = view.findViewById(R.id.editTextUsername)
        val passwordEditText: EditText = view.findViewById(R.id.editTextPassword)
        val confirmPasswordEditText: EditText = view.findViewById(R.id.editTextConfirmPassword)
        val signUpButton: MaterialButton = view.findViewById(R.id.buttonSignUp)
        val pickProfilePictureButton: ImageButton = view.findViewById(R.id.buttonPickProfilePicture)
        
        val emailInputLayout = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.emailInputLayout)
        val usernameInputLayout = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.usernameInputLayout)
        val passwordInputLayout = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.passwordInputLayout)
        val confirmPasswordInputLayout = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.confirmPasswordInputLayout)

        userFormViewModel.setRegistration(true)

        pickProfilePictureButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updateEmail(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updateUsername(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updatePassword(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updateConfirmPassword(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        userFormViewModel.validationState.observe(viewLifecycleOwner) { state ->
            emailInputLayout.error = state.emailError
            usernameInputLayout.error = state.usernameError
            passwordInputLayout.error = state.passwordError
            confirmPasswordInputLayout.error = state.confirmPasswordError
            signUpButton.isEnabled = state.isValid
        }

        val loadingDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 6f
            centerRadius = 24f
            setColorSchemeColors(android.graphics.Color.WHITE)
            setBounds(0, 0, 100, 100)
        }

        signUpButton.setOnClickListener {
            if (profileBitmap == null) {
                Toast.makeText(requireContext(), "Please select a profile picture", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUpButton.text = ""
            signUpButton.icon = loadingDrawable
            loadingDrawable.start()
            signUpButton.isEnabled = false

            val email = userFormViewModel.formState.value?.email ?: ""
            val username = userFormViewModel.formState.value?.username ?: ""
            val password = userFormViewModel.formState.value?.password ?: ""

            authenticationViewModel.register(email, username, password, profileBitmap)
        }

        authenticationViewModel.registrationResult.observe(viewLifecycleOwner, Observer { result ->
            loadingDrawable.stop()
            signUpButton.icon = null
            signUpButton.text = "Sign Up"
            signUpButton.isEnabled = true

            when (result) {
                is LoginResult.Success -> {
                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                    val intent = android.content.Intent(requireActivity(), HomeActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is LoginResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                is LoginResult.EmptyFields -> {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
        })

        return view
    }
}
