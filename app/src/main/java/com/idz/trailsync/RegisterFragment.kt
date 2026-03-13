package com.idz.trailsync

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.imageview.ShapeableImageView
import android.media.ExifInterface
import android.graphics.Matrix


class RegisterFragment : Fragment() {
    private var profileBitmap: Bitmap? = null
    private lateinit var imageView: ShapeableImageView
    private val authenticationViewModel: AuthenticationViewModel by viewModels()

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val exif = inputStream?.let { ExifInterface(it) }
                val bitmap =
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                val orientation = exif?.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val rotatedBitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                    else -> bitmap
                }
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
        val signUpButton: Button = view.findViewById(R.id.buttonSignUp)
        val registerProgressBar: ProgressBar = view.findViewById(R.id.registerProgressBar)
        val pickProfilePictureButton: ImageButton = view.findViewById(R.id.buttonPickProfilePicture)
        val emailInputLayout =
            view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.emailInputLayout)
        val passwordInputLayout =
            view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.passwordInputLayout)
        val confirmPasswordInputLayout =
            view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.confirmPasswordInputLayout)

        pickProfilePictureButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInputLayout.error = "Invalid email address"
                } else {
                    emailInputLayout.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.length < 6) {
                    passwordInputLayout.error = "Password must be at least 6 characters"
                } else {
                    passwordInputLayout.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                val password = passwordEditText.text.toString()
                if (confirmPassword != password) {
                    confirmPasswordInputLayout.error = "Passwords do not match"
                } else {
                    confirmPasswordInputLayout.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        signUpButton.setOnClickListener {
            signUpButton.isEnabled = false
            registerProgressBar.visibility = View.VISIBLE
            val email = emailEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT)
                    .show()
                registerProgressBar.visibility = View.GONE
                signUpButton.isEnabled = true
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid email address",
                    Toast.LENGTH_SHORT
                )
                    .show()
                registerProgressBar.visibility = View.GONE
                signUpButton.isEnabled = true
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                )
                    .show()
                registerProgressBar.visibility = View.GONE
                signUpButton.isEnabled = true
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT)
                    .show()
                registerProgressBar.visibility = View.GONE
                signUpButton.isEnabled = true
                return@setOnClickListener
            }
            if (profileBitmap == null) {
                Toast.makeText(
                    requireContext(),
                    "Please select a profile picture",
                    Toast.LENGTH_SHORT
                ).show()
                registerProgressBar.visibility = View.GONE
                signUpButton.isEnabled = true
                return@setOnClickListener
            }

            authenticationViewModel.register(email, username, password, profileBitmap)
        }

        authenticationViewModel.registrationResult.observe(viewLifecycleOwner, Observer { result ->
            signUpButton.isEnabled = true
            registerProgressBar.visibility = View.GONE
            when (result) {
                is LoginResult.Success -> {
                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT)
                        .show()
                    val intent = android.content.Intent(requireActivity(), HomeActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }

                is LoginResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }

                is LoginResult.EmptyFields -> {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        })

        return view
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
