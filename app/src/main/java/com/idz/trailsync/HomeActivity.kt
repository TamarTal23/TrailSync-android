package com.idz.trailsync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.idz.trailsync.features.home.HomeViewModel

class HomeActivity : AppCompatActivity() {
    var navController: NavController? = null
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val toolbar: Toolbar = findViewById(R.id.top_bar_container)
        setSupportActionBar(toolbar)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        setupDrawerFilters(drawerLayout)

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

                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
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

    private fun setupDrawerFilters(drawerLayout: DrawerLayout) {
        val priceEditText = findViewById<EditText>(R.id.drawerPriceEditText)
        val minDaysEditText = findViewById<EditText>(R.id.drawerMinDaysEditText)
        val maxDaysEditText = findViewById<EditText>(R.id.drawerMaxDaysEditText)
        val btnApply = findViewById<MaterialButton>(R.id.btnApplyFiltersDrawer)
        val btnClear = findViewById<MaterialButton>(R.id.btnClearFiltersDrawer)

        btnApply?.setOnClickListener {
            homeViewModel.applyAdvancedFilters(
                priceEditText?.text?.toString(),
                minDaysEditText?.text?.toString(),
                maxDaysEditText?.text?.toString()
            )
            hideKeyboard()
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnClear?.setOnClickListener {
            priceEditText?.text = null
            minDaysEditText?.text = null
            maxDaysEditText?.text = null
            homeViewModel.clearFilters()
            hideKeyboard()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() ?: super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
