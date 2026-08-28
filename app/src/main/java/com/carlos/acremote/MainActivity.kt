package com.carlos.acremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.acremote.ui.theme.ACRemoteTheme
import com.carlos.acremote.ui.theme.RemotePalette
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
        initial = AcPreferencesState(
            marca = null, modelo = null, tempC = null, modo = null,
            turbo = null, ledEquipoOn = null, onboardingCompleto = false
        )
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
            initialModo = prefsState.modo ?: AcModes.FRIO,
            initialTurbo = prefsState.turbo ?: false,
            initialLedEquipoOn = prefsState.ledEquipoOn ?: true
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(RemotePalette.panelTop, RemotePalette.panelMid, RemotePalette.panelBottom),
                    center = Offset.Zero,
                    radius = 1400f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = marca.uppercase(), color = RemotePalette.textMuted, fontSize = 11.sp)
                    Text(text = modelo, color = RemotePalette.textPrimary, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (uiState.power) "Encendido" else "Apagado",
                        color = RemotePalette.textMuted,
                        fontSize = 10.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (uiState.power) RemotePalette.accentCool else RemotePalette.textMuted)
                    )
                }
            }

            LcdDisplay(
                power = uiState.power,
                tempC = uiState.tempC,
                modo = uiState.modo,
                turbo = uiState.turbo,
                ledEquipoOn = uiState.ledEquipoOn
            )

            TemperatureControl(
                tempC = uiState.tempC,
                enabled = uiState.power,
                onIncrease = viewModel::increaseTemp,
                onDecrease = viewModel::decreaseTemp
            )

            PowerButton(isOn = uiState.power, onClick = viewModel::togglePower)

            ModeSelector(
                selected = uiState.modo,
                enabled = uiState.power,
                onSelect = viewModel::setModo
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    RemoteToggleRow(
                        icon = Icons.Filled.Lightbulb,
                        label = "LED equipo",
                        checked = uiState.ledEquipoOn,
                        enabled = uiState.power,
                        accentColor = RemotePalette.accentCool,
                        accentSurface = RemotePalette.accentCoolSurface,
                        onCheckedChange = { viewModel.toggleLedEquipo() }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    RemoteToggleRow(
                        icon = Icons.Filled.Bolt,
                        label = "Turbo",
                        checked = uiState.turbo,
                        enabled = uiState.power,
                        accentColor = RemotePalette.accentTurbo,
                        accentSurface = RemotePalette.accentTurboSurface,
                        onCheckedChange = { viewModel.toggleTurbo() }
                    )
                }
            }

            uiState.lastMessage?.let { message ->
                Text(text = message, color = RemotePalette.textMuted, fontSize = 12.sp)
            }
        }
    }
}
