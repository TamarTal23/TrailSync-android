package com.idz.trailsync

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.idz.trailsync.features.profile.UserFormViewModel

class LoginFragment : Fragment() {
    private val authenticationViewModel: AuthenticationViewModel by activityViewModels()
    private val userFormViewModel: UserFormViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        
        val emailInputLayout: TextInputLayout = view.findViewById(R.id.emailInputLayout)
        val passwordInputLayout: TextInputLayout = view.findViewById(R.id.passwordInputLayout)
        val emailEditText: EditText = view.findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = view.findViewById(R.id.editTextPassword)
        val loginButton: MaterialButton = view.findViewById(R.id.buttonLogin)
        val signUpTextView: TextView = view.findViewById(R.id.textSignUp)

        userFormViewModel.setRegistration(false)

        signUpTextView.setOnClickListener {
            (requireActivity() as? AuthenticationActivity)?.showRegisterFragment()
        }

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userFormViewModel.updateEmail(s.toString())
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

        userFormViewModel.validationState.observe(viewLifecycleOwner) { state ->
            emailInputLayout.error = state.emailError
            passwordInputLayout.error = state.passwordError
            loginButton.isEnabled = state.isValid
        }

        val loadingDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 6f
            centerRadius = 24f
            setColorSchemeColors(android.graphics.Color.WHITE)
            setBounds(0, 0, 100, 100)
        }

        loginButton.setOnClickListener {
            loginButton.text = ""
            loginButton.icon = loadingDrawable
            loadingDrawable.start()
            loginButton.isEnabled = false

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            authenticationViewModel.login(email, password)
        }

        authenticationViewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
            loadingDrawable.stop()
            loginButton.icon = null
            loginButton.text = "Login"
            loginButton.isEnabled = true

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
        return view
    }
}
