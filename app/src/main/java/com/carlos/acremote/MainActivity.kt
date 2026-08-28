package com.carlos.acremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.acremote.ui.theme.ACRemoteTheme

// Marca/modelo por defecto hasta que exista la selección/onboarding (Fase 4).
private const val DEFAULT_MARCA = "Sankey"
private const val DEFAULT_MODELO = "YKR-P/001E"

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
    val repository = remember { IrCodeRepository(context) }
    val viewModel: AcViewModel = viewModel(
        factory = AcViewModelFactory(transmitter, repository, DEFAULT_MARCA, DEFAULT_MODELO)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = if (hasIrEmitter) {
                    "$DEFAULT_MARCA / $DEFAULT_MODELO"
                } else {
                    "Este dispositivo no tiene IR blaster. La app no puede funcionar aquí."
                },
                style = MaterialTheme.typography.bodyLarge
            )

            if (hasIrEmitter) {
                PowerButton(isOn = uiState.power, onClick = viewModel::togglePower)

                TemperatureControl(
                    tempC = uiState.tempC,
                    enabled = uiState.power,
                    onIncrease = viewModel::increaseTemp,
                    onDecrease = viewModel::decreaseTemp
                )

                ModeSelector(
                    selected = uiState.modo,
                    enabled = uiState.power,
                    onSelect = viewModel::setModo
                )

                uiState.lastMessage?.let { message ->
                    Text(text = message, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
