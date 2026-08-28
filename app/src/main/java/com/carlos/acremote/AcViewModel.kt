package com.carlos.acremote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TEMP_MIN = 16
private const val TEMP_MAX = 30

class AcViewModel(
    private val transmitter: IrTransmitter,
    private val repository: IrCodeRepository,
    private val preferencesRepository: AcPreferencesRepository,
    private val marca: String,
    private val modelo: String,
    initialTempC: Int,
    initialModo: String,
    initialTurbo: Boolean,
    initialLedEquipoOn: Boolean
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AcUiState(tempC = initialTempC, modo = initialModo, turbo = initialTurbo, ledEquipoOn = initialLedEquipoOn)
    )
    val uiState: StateFlow<AcUiState> = _uiState.asStateFlow()

    fun togglePower() {
        val current = _uiState.value
        val turningOn = !current.power
        // Al apagar, el turbo se corta con el equipo (no persiste al prender de nuevo).
        val newState = current.copy(power = turningOn, turbo = turningOn && current.turbo)
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

    fun toggleTurbo() {
        val current = _uiState.value
        if (!current.power) return
        val newState = current.copy(turbo = !current.turbo)
        applyAndSend(newState, "turbo_toggle", "turbo")
    }

    /**
     * El equipo no informa su LED real: el protocolo solo permite mandarle un
     * pulso para que lo alterne (ver ElectraAcEncoder.buildPattern). Acá solo
     * reflejamos de forma optimista lo que asumimos que pasó.
     */
    fun toggleLedEquipo() {
        val current = _uiState.value
        if (!current.power) return
        val newState = current.copy(ledEquipoOn = !current.ledEquipoOn)
        applyAndSend(newState, "led_toggle", "LED del equipo", toggleLight = true)
    }

    private fun applyAndSend(
        newState: AcUiState,
        rawComando: String,
        descripcion: String,
        toggleLight: Boolean = false
    ) {
        val sent = send(newState, rawComando, toggleLight)
        _uiState.value = newState.copy(
            lastMessage = if (sent) null else "No se pudo enviar el comando de $descripcion para $marca/$modelo"
        )
        viewModelScope.launch {
            preferencesRepository.guardarEstado(newState.tempC, newState.modo, newState.turbo, newState.ledEquipoOn)
        }
    }

    private fun send(state: AcUiState, rawComando: String, toggleLight: Boolean): Boolean {
        val device = repository.getDevice(marca, modelo) ?: return false
        return when (device.protocolo) {
            PROTOCOLO_ELECTRA -> {
                val pattern = ElectraAcEncoder.buildPattern(
                    power = state.power,
                    tempC = state.tempC,
                    modo = state.modo,
                    turbo = state.turbo,
                    toggleLight = toggleLight
                )
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
    private val preferencesRepository: AcPreferencesRepository,
    private val marca: String,
    private val modelo: String,
    private val initialTempC: Int,
    private val initialModo: String,
    private val initialTurbo: Boolean,
    private val initialLedEquipoOn: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AcViewModel(
            transmitter, repository, preferencesRepository, marca, modelo,
            initialTempC, initialModo, initialTurbo, initialLedEquipoOn
        ) as T
    }
}
