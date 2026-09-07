package com.carlos.acremote

data class AcUiState(
    val power: Boolean = false,
    val tempC: Int = 24,
    val modo: String = "frio",
    val turbo: Boolean = false,
    // Oscilación vertical de la aleta (botón "up/down" del control físico).
    val swingV: Boolean = false,
    // Estado optimista: el protocolo Electra no informa el LED real del equipo,
    // solo permite mandarle un pulso para que lo alterne (ver ElectraAcEncoder).
    val ledEquipoOn: Boolean = true,
    val lastMessage: String? = null
)

object AcModes {
    const val FRIO = "frio"
    const val VENTILADOR = "ventilador"
    const val AUTO = "auto"

    val ALL = listOf(FRIO, VENTILADOR, AUTO)
}
