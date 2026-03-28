package com.idz.trailsync

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.idz.trailsync.shared.viewModels.AuthenticationViewModel

class AuthenticationActivity : AppCompatActivity() {
    private val authenticationViewModel: AuthenticationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (authenticationViewModel.isUserLoggedIn()) {
            val intent = android.content.Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        setContentView(R.layout.activity_authentication)
    }

    // This method can now be removed as navigation is handled within fragments
    fun showRegisterFragment() {
        // Navigation is handled by findNavController().navigate() in LoginFragment
    }
}
