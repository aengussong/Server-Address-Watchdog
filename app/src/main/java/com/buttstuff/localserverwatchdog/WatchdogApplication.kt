package com.buttstuff.localserverwatchdog

import android.app.Application

class WatchdogApplication: Application() {

    companion object {
        //todo remove after/if DI will be added
        lateinit var appContext: Application
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}
