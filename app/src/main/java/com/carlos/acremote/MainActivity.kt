package com.carlos.acremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.carlos.acremote.ui.theme.ACRemoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ACRemoteTheme {
                val hasIrEmitter = IrEmitterChecker.hasIrEmitter(applicationContext)
                HomeScreen(hasIrEmitter = hasIrEmitter)
            }
        }
    }
}

@Composable
fun HomeScreen(hasIrEmitter: Boolean) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (hasIrEmitter) {
                    "IR blaster detectado. Listo para configurar tu A/C."
                } else {
                    "Este dispositivo no tiene IR blaster. La app no puede funcionar aquí."
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
