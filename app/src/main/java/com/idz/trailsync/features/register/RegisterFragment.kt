package com.idz.trailsync.features.register

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.idz.trailsync.databinding.FragmentRegisterBinding
import com.idz.trailsync.shared.viewModels.UserFormViewModel
import com.idz.trailsync.shared.viewModels.AuthenticationViewModel
import com.idz.trailsync.HomeActivity
import com.idz.trailsync.shared.viewModels.LoginResult

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var selectedImage: Any? = null
    private val authenticationViewModel: AuthenticationViewModel by activityViewModels()
    private val userFormViewModel: UserFormViewModel by activityViewModels()

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                binding.profileImageView.setImageURI(uri)
                selectedImage = uri
            }
        }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            binding.profileImageView.setImageBitmap(bitmap)
            selectedImage = bitmap
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userFormViewModel.setRegistration(true)

        binding.buttonPickProfilePicture.setOnClickListener {
            showPhotoOptionsPopupMenu(it)
        }

        binding.editTextEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updateEmail(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updateUsername(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updatePassword(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.editTextConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updateConfirmPassword(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        userFormViewModel.validationState.observe(viewLifecycleOwner) { state ->
            binding.emailInputLayout.error = state.emailError
            binding.usernameInputLayout.error = state.usernameError
            binding.passwordInputLayout.error = state.passwordError
            binding.confirmPasswordInputLayout.error = state.confirmPasswordError
        }

        val loadingDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 6f
            centerRadius = 24f
            setColorSchemeColors(android.graphics.Color.WHITE)
            setBounds(0, 0, 100, 100)
        }

        binding.buttonSignUp.setOnClickListener {
            userFormViewModel.touchAll()

            if (selectedImage == null) {
                Toast.makeText(
                    requireContext(),
                    "Please select a profile picture",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (userFormViewModel.isFormValid()) {
                binding.buttonSignUp.text = ""
                binding.buttonSignUp.icon = loadingDrawable
                loadingDrawable.start()
                binding.buttonSignUp.isEnabled = false

                val email = userFormViewModel.formState.value?.email ?: ""
                val username = userFormViewModel.formState.value?.username ?: ""
                val password = userFormViewModel.formState.value?.password ?: ""

                authenticationViewModel.register(requireContext(), email, username, password, selectedImage)
            }
        }

        authenticationViewModel.registrationResult.observe(viewLifecycleOwner, Observer { result ->
            loadingDrawable.stop()
            binding.buttonSignUp.icon = null
            binding.buttonSignUp.text = "Sign Up"
            binding.buttonSignUp.isEnabled = true

            when (result) {
                is LoginResult.Success -> {
                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT)
                        .show()

                    val intent = Intent(requireActivity(), HomeActivity::class.java)
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
    }

    private fun showPhotoOptionsPopupMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add("Take Photo")
        popup.menu.add("Choose from Gallery")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Take Photo" -> {
                    takePhotoLauncher.launch(null)
                    true
                }
                "Choose from Gallery" -> {
                    galleryLauncher.launch("image/*")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
