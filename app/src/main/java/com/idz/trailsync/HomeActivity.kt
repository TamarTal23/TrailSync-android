package com.idz.trailsync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.idz.trailsync.features.home.HomeViewModel
import com.idz.trailsync.shared.viewModels.AuthenticationViewModel

class HomeActivity : AppCompatActivity() {
    var navController: NavController? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private val authenticationViewModel: AuthenticationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val toolbar: Toolbar = findViewById(R.id.top_bar_container)
        setSupportActionBar(toolbar)

        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as? NavHostFragment
        navController = navHostFragment?.navController
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_bar)
        val logout = findViewById<LinearLayout>(R.id.logout)
        logout.visibility = View.GONE

        logout.setOnClickListener {
            authenticationViewModel.logout()
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
            finish()
        }

        navController?.let {
            NavigationUI.setupWithNavController(bottomNavigationView, it)

            it.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == R.id.profileFragment) {
                    logout.visibility = View.VISIBLE
                } else {
                    logout.visibility = View.GONE
                }

                if (destination.id == R.id.editProfileFragment) {
                    toolbar.visibility = View.GONE
                } else {
                    toolbar.visibility = View.VISIBLE
                }
            }

            supportActionBar?.setDisplayShowTitleEnabled(false)
            bottomNavigationView.setOnItemSelectedListener { item ->
                it.popBackStack(it.graph.startDestinationId, false)
                NavigationUI.onNavDestinationSelected(item, it)
                true
            }
        }
    }

    fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() ?: super.onSupportNavigateUp()
    }
}
