package com.idz.trailsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.idz.trailsync.model.Model
import com.idz.trailsync.ui.theme.TrailSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrailSyncTheme {

            }
        }

        Model.shared.getAllUsers { users ->
            println(users)
        }
    }
}