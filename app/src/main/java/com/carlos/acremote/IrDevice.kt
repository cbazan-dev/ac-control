package com.carlos.acremote

data class IrDevice(
    val marca: String,
    val modelo: String,
    val frecuenciaHz: Int,
    val comandos: Map<String, IntArray>
)
