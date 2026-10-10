package com.echo.app

import android.app.Application
import android.content.Context
import android.util.Log

/**
 * Application entry point.
 *
 * Installs a global uncaught exception handler that saves the last stack trace to
 * private shared preferences, so [MainActivity] can show it on the next launch.
 * The original handler is still called afterwards.
 */
class EchoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("EchoCrash", "Fatal crash caught: ${exception.message}", exception)
            
            try {
                val prefs = getSharedPreferences("echo_crash_logs", Context.MODE_PRIVATE)
                prefs.edit().putString("last_crash", android.util.Log.getStackTraceString(exception)).commit()
            } catch (e: Exception) {
                Log.e("EchoCrash", "Failed to save crash log", e)
            }
            
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}
