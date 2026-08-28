package com.carlos.acremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    val context = LocalContext.current
    val transmitter = remember { IrTransmitter(context) }
    var lastResult by remember { mutableStateOf<String?>(null) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (hasIrEmitter) {
                    "IR blaster detectado. Listo para configurar tu A/C."
                } else {
                    "Este dispositivo no tiene IR blaster. La app no puede funcionar aquí."
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                enabled = hasIrEmitter,
                onClick = {
                    val sent = transmitter.transmit(SamplePatterns.FREQUENCY_HZ, SamplePatterns.TEST_PATTERN)
                    lastResult = if (sent) "Señal IR de prueba enviada" else "No se pudo enviar: sin IR blaster"
                }
            ) {
                Text("Probar señal IR")
            }

            lastResult?.let { result ->
                Text(text = result, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
