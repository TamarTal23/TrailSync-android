package com.idz.trailsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer

class LoginFragment : Fragment() {
    private val authenticationViewModel: AuthenticationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val emailEditText: EditText = view.findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = view.findViewById(R.id.editTextPassword)
        val loginButton: Button = view.findViewById(R.id.buttonLogin)
        val loginProgressBar: ProgressBar = view.findViewById(R.id.loginProgressBar)
        val signUpTextView: TextView = view.findViewById(R.id.textSignUp)

        signUpTextView.setOnClickListener {
            (requireActivity() as? AuthenticationActivity)?.showRegisterFragment()
        }

        loginButton.setOnClickListener {
            loginButton.isEnabled = false
            loginProgressBar.visibility = View.VISIBLE
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            authenticationViewModel.login(email, password)
        }

        authenticationViewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
            loginButton.isEnabled = true
            loginProgressBar.visibility = View.GONE
            when (result) {
                is LoginResult.Success -> {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = android.content.Intent(requireActivity(), HomeActivity::class.java)
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
            }
        })
        return view
    }
}
