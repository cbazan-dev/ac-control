package com.carlos.acremote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TEMP_MIN = 16
private const val TEMP_MAX = 30

class AcViewModel(
    private val transmitter: IrTransmitter,
    private val repository: IrCodeRepository,
    private val marca: String,
    private val modelo: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcUiState())
    val uiState: StateFlow<AcUiState> = _uiState.asStateFlow()

    fun togglePower() {
        val current = _uiState.value
        val turningOn = !current.power
        val newState = current.copy(power = turningOn)
        applyAndSend(newState, if (turningOn) "power_on" else "power_off", "power")
    }

    fun increaseTemp() {
        val current = _uiState.value
        if (!current.power) return
        val newState = current.copy(tempC = (current.tempC + 1).coerceAtMost(TEMP_MAX))
        applyAndSend(newState, "temp_up", "temp_up")
    }

    fun decreaseTemp() {
        val current = _uiState.value
        if (!current.power) return
        val newState = current.copy(tempC = (current.tempC - 1).coerceAtLeast(TEMP_MIN))
        applyAndSend(newState, "temp_down", "temp_down")
    }

    fun setModo(modo: String) {
        val current = _uiState.value
        if (!current.power) return
        val newState = current.copy(modo = modo)
        applyAndSend(newState, "modo_$modo", "modo '$modo'")
    }

    private fun applyAndSend(newState: AcUiState, rawComando: String, descripcion: String) {
        val sent = send(newState, rawComando)
        _uiState.value = newState.copy(
            lastMessage = if (sent) null else "No se pudo enviar el comando de $descripcion para $marca/$modelo"
        )
    }

    private fun send(state: AcUiState, rawComando: String): Boolean {
        val device = repository.getDevice(marca, modelo) ?: return false
        return when (device.protocolo) {
            PROTOCOLO_ELECTRA -> {
                val pattern = ElectraAcEncoder.buildPattern(state.power, state.tempC, state.modo)
                transmitter.transmit(ElectraAcEncoder.FREQUENCY_HZ, pattern)
            }
            else -> {
                val pattern = device.comandos[rawComando] ?: return false
                transmitter.transmit(device.frecuenciaHz, pattern)
            }
        }
    }
}

class AcViewModelFactory(
    private val transmitter: IrTransmitter,
    private val repository: IrCodeRepository,
    private val marca: String,
    private val modelo: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AcViewModel(transmitter, repository, marca, modelo) as T
    }
}
