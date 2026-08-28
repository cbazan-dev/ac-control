package com.carlos.acremote

const val PROTOCOLO_RAW = "raw"
const val PROTOCOLO_ELECTRA = "electra"

data class IrDevice(
    val marca: String,
    val modelo: String,
    val frecuenciaHz: Int,
    val protocolo: String = PROTOCOLO_RAW,
    val comandos: Map<String, IntArray>
)
