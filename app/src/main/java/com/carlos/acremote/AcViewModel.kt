package com.carlos.acremote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
        val turningOn = !_uiState.value.power
        val sent = sendCommand(if (turningOn) "power_on" else "power_off")
        _uiState.update {
            it.copy(
                power = turningOn,
                lastMessage = if (sent) null else "No se encontró el comando de power para $marca/$modelo"
            )
        }
    }

    fun increaseTemp() {
        if (!_uiState.value.power) return
        val sent = sendCommand("temp_up")
        _uiState.update {
            it.copy(
                tempC = (it.tempC + 1).coerceAtMost(TEMP_MAX),
                lastMessage = if (sent) null else "No se encontró el comando temp_up para $marca/$modelo"
            )
        }
    }

    fun decreaseTemp() {
        if (!_uiState.value.power) return
        val sent = sendCommand("temp_down")
        _uiState.update {
            it.copy(
                tempC = (it.tempC - 1).coerceAtLeast(TEMP_MIN),
                lastMessage = if (sent) null else "No se encontró el comando temp_down para $marca/$modelo"
            )
        }
    }

    fun setModo(modo: String) {
        if (!_uiState.value.power) return
        val sent = sendCommand("modo_$modo")
        _uiState.update {
            it.copy(
                modo = modo,
                lastMessage = if (sent) null else "No se encontró el comando de modo '$modo' para $marca/$modelo"
            )
        }
    }

    private fun sendCommand(comando: String): Boolean {
        val pattern = repository.getCommand(marca, modelo, comando) ?: return false
        val device = repository.getDevice(marca, modelo) ?: return false
        return transmitter.transmit(device.frecuenciaHz, pattern)
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
