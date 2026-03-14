package com.idz.trailsync.features.Login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.idz.trailsync.databinding.FragmentLoginBinding
import com.idz.trailsync.features.profile.UserFormViewModel
import com.idz.trailsync.AuthenticationActivity
import com.idz.trailsync.AuthenticationViewModel
import com.idz.trailsync.HomeActivity
import com.idz.trailsync.LoginResult

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authenticationViewModel: AuthenticationViewModel by activityViewModels()
    private val userFormViewModel: UserFormViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userFormViewModel.setRegistration(false)

        binding.textSignUp.setOnClickListener {
            (requireActivity() as? AuthenticationActivity)?.showRegisterFragment()
        }

        binding.editTextEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updateEmail(s.toString())
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

        userFormViewModel.validationState.observe(viewLifecycleOwner) { state ->
            binding.emailInputLayout.error = state.emailError
            binding.passwordInputLayout.error = state.passwordError
            binding.buttonLogin.isEnabled = true
        }

        val loadingDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 6f
            centerRadius = 24f
            setColorSchemeColors(android.graphics.Color.WHITE)
            setBounds(0, 0, 100, 100)
        }

        binding.buttonLogin.setOnClickListener {
            userFormViewModel.touchAll()

            if (userFormViewModel.isFormValid()) {
                binding.buttonLogin.text = ""
                binding.buttonLogin.icon = loadingDrawable
                loadingDrawable.start()
                binding.buttonLogin.isEnabled = false

                val email = binding.editTextEmail.text.toString()
                val password = binding.editTextPassword.text.toString()
                authenticationViewModel.login(email, password)
            }
        }

        authenticationViewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
            loadingDrawable.stop()
            binding.buttonLogin.icon = null
            binding.buttonLogin.text = "Login"
            binding.buttonLogin.isEnabled = true

            when (result) {
                is LoginResult.Success -> {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireActivity(), HomeActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }

                is LoginResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }

                is LoginResult.EmptyFields -> {
                    Toast.makeText(
                        requireContext(),
                        "Please enter email and password",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}