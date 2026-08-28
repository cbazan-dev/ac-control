package com.carlos.acremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.acremote.ui.theme.ACRemoteTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ACRemoteTheme {
                val hasIrEmitter = IrEmitterChecker.hasIrEmitter(applicationContext)
                AcRemoteApp(hasIrEmitter = hasIrEmitter)
            }
        }
    }
}

@Composable
fun AcRemoteApp(hasIrEmitter: Boolean) {
    if (!hasIrEmitter) {
        MessageScreen("Este dispositivo no tiene IR blaster. La app no puede funcionar aquí.")
        return
    }

    val context = LocalContext.current
    val repository = remember { IrCodeRepository(context) }
    val preferencesRepository = remember { AcPreferencesRepository(context) }
    val scope = rememberCoroutineScope()

    val prefsState by preferencesRepository.state.collectAsState(
        initial = AcPreferencesState(marca = null, modelo = null, tempC = null, modo = null, onboardingCompleto = false)
    )

    if (!prefsState.onboardingCompleto) {
        OnboardingScreen(
            repository = repository,
            onFinished = { marca, modelo ->
                scope.launch { preferencesRepository.guardarDispositivo(marca, modelo) }
            }
        )
        return
    }

    val marca = prefsState.marca ?: return
    val modelo = prefsState.modelo ?: return

    val transmitter = remember { IrTransmitter(context) }
    val viewModel: AcViewModel = viewModel(
        factory = AcViewModelFactory(
            transmitter = transmitter,
            repository = repository,
            preferencesRepository = preferencesRepository,
            marca = marca,
            modelo = modelo,
            initialTempC = prefsState.tempC ?: 24,
            initialModo = prefsState.modo ?: AcModes.FRIO
        )
    )

    HomeScreen(marca = marca, modelo = modelo, viewModel = viewModel)
}

@Composable
fun MessageScreen(message: String) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun HomeScreen(marca: String, modelo: String, viewModel: AcViewModel) {
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
            Text(text = "$marca / $modelo", style = MaterialTheme.typography.bodyLarge)

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
