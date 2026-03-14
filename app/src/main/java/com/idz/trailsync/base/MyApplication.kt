package com.idz.trailsync.base

import android.app.Application
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize

class MyApplication: Application() {
    object Globals {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        Globals.context = applicationContext
        
        // Initialize Firebase and App Check for Debug environment
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }
}
