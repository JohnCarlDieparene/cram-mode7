package com.labactivity.crammode

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Full-screen box showing the PNG only
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_cram_logo), // your splash PNG with background
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds // fills the screen exactly
                )
            }
        }

        // Delay 1.2 seconds, then navigate
        lifecycleScope.launch {
            delay(1200)

            val nextActivity = if (isUserLoggedIn()) {
                HomeComposeActivity::class.java
            } else {
                AuthComposeActivity::class.java
            }

            startActivity(Intent(this@SplashActivity, nextActivity))
            finish()
        }
    }

    // Replace with your real login check
    private fun isUserLoggedIn(): Boolean {
        return false // e.g., FirebaseAuth.getInstance().currentUser != null
    }
}
