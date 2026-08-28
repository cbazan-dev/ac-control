package com.carlos.acremote

data class AcUiState(
    val power: Boolean = false,
    val tempC: Int = 24,
    val modo: String = "frio",
    val lastMessage: String? = null
)

object AcModes {
    const val FRIO = "frio"
    const val VENTILADOR = "ventilador"
    const val AUTO = "auto"

    val ALL = listOf(FRIO, VENTILADOR, AUTO)
}
