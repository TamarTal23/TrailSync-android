package com.idz.trailsync

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.idz.trailsync.features.Login.LoginFragment
import com.idz.trailsync.features.Register.RegisterFragment

class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authenticationViewModel = AuthenticationViewModel()
        if (authenticationViewModel.isUserLoggedIn()) {
            val intent = android.content.Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        setContentView(R.layout.activity_authentication)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, LoginFragment())
                .commit()
        }
    }

    fun showRegisterFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }
}
