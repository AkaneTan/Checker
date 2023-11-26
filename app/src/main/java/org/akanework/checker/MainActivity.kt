package org.akanework.checker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.akanework.checker.ui.theme.CheckerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val contentResolver = contentResolver
            val activity = this
            CheckerTheme {
                CheckerApp(contentResolver, activity)
            }
        }
    }
}
