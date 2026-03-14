package com.idz.trailsync.base

import android.app.Application
import android.content.Context
import com.google.android.libraries.places.api.Places
import com.idz.trailsync.BuildConfig

class MyApplication: Application() {
    object Globals {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        Globals.context = applicationContext
        
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
    }
}