package com.idz.trailsync

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.LinearLayout

class HomeActivity : AppCompatActivity() {
    var navController: NavController? = null

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
            val authenticationViewModel = AuthenticationViewModel()

            authenticationViewModel.logout()

            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
            finish()
        }

        navController?.let {
            NavigationUI.setupWithNavController(bottomNavigationView, it)

            toolbar.navigationIcon = null

            it.addOnDestinationChangedListener { _, destination, _ ->
                toolbar.navigationIcon = null
                if (destination.id == R.id.profileFragment) {
                    logout.visibility = View.VISIBLE
                } else {
                    logout.visibility = View.GONE
                }
            }

            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            bottomNavigationView.setOnItemSelectedListener { item ->
                it.popBackStack(it.graph.startDestinationId, false)
                NavigationUI.onNavDestinationSelected(item, it)
                true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() ?: super.onSupportNavigateUp()
    }
}