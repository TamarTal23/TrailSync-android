package com.idz.trailsync.features.profile

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
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.idz.trailsync.AuthenticationViewModel
import com.idz.trailsync.LoginResult
import com.idz.trailsync.R
import com.idz.trailsync.model.Model
import com.idz.trailsync.utils.BitmapUtils
import com.squareup.picasso.Picasso

class EditProfileFragment : Fragment() {
    private val userFormViewModel: UserFormViewModel by activityViewModels()
    private val authViewModel: AuthenticationViewModel by activityViewModels()
    private var profileBitmap: Bitmap? = null

    private val bitmapUtils = BitmapUtils()
    private lateinit var imageView: ShapeableImageView

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val rotatedBitmap =
                bitmapUtils.getRotatedBitmap(uri, requireActivity().contentResolver)

            if (rotatedBitmap != null) {
                imageView.setImageBitmap(rotatedBitmap)
                profileBitmap = rotatedBitmap
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val backButton = view.findViewById<View>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        userFormViewModel.setRegistration(false)

        val emailEditText: EditText = view.findViewById(R.id.editTextEmail)
        val usernameEditText: EditText = view.findViewById(R.id.editTextUsername)
        val usernameInputLayout =
            view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.usernameInputLayout)
        val saveChangesButton: MaterialButton = view.findViewById(R.id.buttonSaveChanges)

        val pickProfilePictureButton: ImageButton = view.findViewById(R.id.buttonPickProfilePicture)
        imageView = view.findViewById(R.id.profileImageView)

        pickProfilePictureButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        emailEditText.isEnabled = false
        emailEditText.alpha = 0.6f

        val auth = Firebase.auth

        auth.currentUser?.uid?.let { uid ->
            Model.shared.getUserById(uid) { user ->
                user?.let {
                    emailEditText.setText(it.email)
                    usernameEditText.setText(it.username)
                    userFormViewModel.updateEmail(it.email)
                    userFormViewModel.updateUsername(it.username)

                    val url = it.profilePicture
                    url?.let { url ->
                        if (url.isNotBlank()) {
                            Picasso.get()
                                .load(url)
                                .resize(120, 120)
                                .centerCrop()
                                .placeholder(R.drawable.user_icon_small)
                                .into(imageView)
                        }
                    }
                }
            }
        }

        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updateUsername(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        userFormViewModel.validationState.observe(viewLifecycleOwner) { state ->
            usernameInputLayout.error = state.usernameError
            saveChangesButton.isEnabled = state.isValid
        }

        val loadingDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 6f
            centerRadius = 24f
            setColorSchemeColors(android.graphics.Color.WHITE)
            setBounds(0, 0, 100, 100)
        }

        authViewModel.updateProfileResult.observe(viewLifecycleOwner) { event ->
            val result = event?.getContentIfNotHandled() ?: return@observe

            loadingDrawable.stop()
            saveChangesButton.icon = null
            saveChangesButton.text = "Save Changes"
            saveChangesButton.isEnabled = true

            when (result) {
                is LoginResult.Success -> {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT)
                        .show()
                    view.post {
                        if (isAdded) {
                            findNavController().navigateUp()
                        }
                    }
                }

                is LoginResult.Error -> {
                    Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }

        saveChangesButton.setOnClickListener {
            if (userFormViewModel.isFormValid()) {
                val username = userFormViewModel.formState.value?.username

                saveChangesButton.text = ""
                saveChangesButton.icon = loadingDrawable
                loadingDrawable.start()
                saveChangesButton.isEnabled = false

                authViewModel.updateProfile(username, profileBitmap)
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.clearUpdateProfileResult()
    }
}
