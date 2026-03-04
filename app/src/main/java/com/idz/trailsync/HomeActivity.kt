package com.idz.trailsync

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.widget.Toolbar

class HomeActivity : AppCompatActivity() {
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val toolbar: Toolbar = findViewById(R.id.top_app_bar)
        setSupportActionBar(toolbar)

        val navHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.main_nav_host) as? NavHostFragment
        navController = navHostFragment?.navController
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_bar)

        navController?.let {
            NavigationUI.setupWithNavController(bottomNavigationView, it)

            it.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == R.id.postDetailsFragment) {
                    supportActionBar?.hide()
                } else {
                    supportActionBar?.show()
                }
            }

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