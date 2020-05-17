package com.example.mysecuredapp

import android.app.Application
import timber.log.Timber

class PetApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }
}