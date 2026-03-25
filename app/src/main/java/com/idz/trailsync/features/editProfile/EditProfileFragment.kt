package com.idz.trailsync.features.editProfile

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.idz.trailsync.AuthenticationViewModel
import com.idz.trailsync.LoginResult
import com.idz.trailsync.data.repository.UserRepository
import com.idz.trailsync.databinding.FragmentEditProfileBinding
import com.idz.trailsync.utils.BitmapUtils
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class EditProfileFragment : Fragment() {
    private val userFormViewModel: UserFormViewModel by activityViewModels()
    private val authViewModel: AuthenticationViewModel by activityViewModels()

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private var profileBitmap: Bitmap? = null
    private val bitmapUtils = BitmapUtils()

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val rotatedBitmap =
                bitmapUtils.getRotatedBitmap(uri, requireActivity().contentResolver)

            if (rotatedBitmap != null) {
                binding.profileImageView.setImageBitmap(rotatedBitmap)
                profileBitmap = rotatedBitmap
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        userFormViewModel.setMode(FormMode.EDIT_PROFILE)

        binding.buttonPickProfilePicture.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.editTextEmail.isEnabled = false
        binding.editTextEmail.alpha = 0.6f

        val auth = Firebase.auth

        auth.currentUser?.uid?.let { uid ->
            UserRepository.Companion.shared.getUserById(uid) { user ->
                user?.let {
                    binding.editTextEmail.setText(it.email)
                    binding.editTextUsername.setText(it.username)
                    userFormViewModel.updateEmail(it.email)
                    userFormViewModel.updateUsername(it.username)

                    val url = it.profilePicture
                    url?.let { profileUrl ->
                        if (profileUrl.isNotBlank()) {
                            binding.profileProgressBar.visibility = View.VISIBLE

                            Picasso.get()
                                .load(profileUrl)
                                .resize(120, 120)
                                .centerCrop()
                                .into(
                                    binding.profileImageView,
                                    object : Callback {
                                        override fun onSuccess() {
                                            binding.profileProgressBar.visibility = View.GONE
                                        }

                                        override fun onError(e: Exception?) {
                                            binding.profileProgressBar.visibility = View.GONE
                                        }
                                    })
                        }
                    }
                }
            }
        }

        binding.editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updateUsername(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        userFormViewModel.validationState.observe(viewLifecycleOwner) { state ->
            binding.usernameInputLayout.error = state.usernameError
            binding.buttonSaveChanges.isEnabled = state.isValid
        }

        val loadingDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 6f
            centerRadius = 24f
            setColorSchemeColors(Color.WHITE)
            setBounds(0, 0, 100, 100)
        }

        authViewModel.updateProfileResult.observe(viewLifecycleOwner) { event ->
            val result = event?.getContentIfNotHandled() ?: return@observe

            loadingDrawable.stop()
            binding.buttonSaveChanges.icon = null
            binding.buttonSaveChanges.text = "Save Changes"
            binding.buttonSaveChanges.isEnabled = true

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

        binding.buttonSaveChanges.setOnClickListener {
            userFormViewModel.touchAll()
            if (userFormViewModel.isFormValid()) {
                val username = userFormViewModel.formState.value?.username

                binding.buttonSaveChanges.text = ""
                binding.buttonSaveChanges.icon = loadingDrawable
                loadingDrawable.start()
                binding.buttonSaveChanges.isEnabled = false

                authViewModel.updateProfile(username, profileBitmap)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.clearUpdateProfileResult()
        _binding = null
    }
}