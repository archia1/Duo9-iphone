package com.fold.iphoneduo
import android.app.Activity
import android.os.Bundle
import android.content.Intent

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val intent = Intent(this, DuoOverlayService::class.java)
            if (DuoOverlayService.isRunning) stopService(intent) else startService(intent)
        } catch(e: Exception) {}
        finish()
    }
}
