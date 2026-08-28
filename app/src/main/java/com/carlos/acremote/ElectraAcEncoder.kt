package com.carlos.acremote

/**
 * Protocolo Electra_AC (usado por los equipos AUX/YKR — incluye el control
 * Sankey YKR-P/001E). A diferencia de un control NEC típico, no manda un
 * pulso fijo por botón: cada transmisión lleva el estado completo del
 * equipo (power, modo, temperatura, fan) en un paquete de 13 bytes con
 * checksum. Portado desde IRremoteESP8266 (ir_Electra.h / ir_Electra.cpp).
 */
object ElectraAcEncoder {
    const val FREQUENCY_HZ = 38000

    private const val HDR_MARK = 9166
    private const val HDR_SPACE = 4470
    private const val BIT_MARK = 646
    private const val ONE_SPACE = 1647
    private const val ZERO_SPACE = 547
    private const val MESSAGE_GAP = 100000

    private const val STATE_LENGTH = 13

    private const val MODE_AUTO = 0b000
    private const val MODE_COOL = 0b001
    private const val MODE_FAN = 0b110

    private const val FAN_AUTO = 0b101
    private const val SWING_OFF = 0b111
    private const val LIGHT_TOGGLE_OFF = 0x08

    private const val TEMP_MIN = 16
    private const val TEMP_MAX = 32
    private const val TEMP_DELTA = 8

    fun buildPattern(power: Boolean, tempC: Int, modo: String): IntArray =
        encodeToPulses(buildStateBytes(power, tempC, modo))

    private fun modeToElectra(modo: String): Int = when (modo) {
        AcModes.FRIO -> MODE_COOL
        AcModes.VENTILADOR -> MODE_FAN
        AcModes.AUTO -> MODE_AUTO
        else -> MODE_AUTO
    }

    internal fun buildStateBytes(power: Boolean, tempC: Int, modo: String): IntArray {
        val raw = IntArray(STATE_LENGTH)
        raw[0] = 0xC3 // valor fijo (stateReset en la librería original)

        val tempRaw = (tempC.coerceIn(TEMP_MIN, TEMP_MAX) - TEMP_DELTA) and 0x1F
        raw[1] = (SWING_OFF and 0x07) or ((tempRaw and 0x1F) shl 3)
        raw[2] = (SWING_OFF and 0x07) shl 5
        raw[4] = (FAN_AUTO and 0x07) shl 5
        raw[6] = (modeToElectra(modo) and 0x07) shl 5
        raw[9] = if (power) (1 shl 5) else 0
        raw[11] = LIGHT_TOGGLE_OFF

        var sum = 0
        for (i in 0 until STATE_LENGTH - 1) sum += raw[i]
        raw[STATE_LENGTH - 1] = sum and 0xFF

        return raw
    }

    internal fun encodeToPulses(stateBytes: IntArray): IntArray {
        val pulses = ArrayList<Int>(2 + stateBytes.size * 8 * 2 + 2)
        pulses.add(HDR_MARK)
        pulses.add(HDR_SPACE)
        for (byteVal in stateBytes) {
            for (bitIndex in 0 until 8) { // LSB primero, por byte
                val bit = (byteVal shr bitIndex) and 1
                pulses.add(BIT_MARK)
                pulses.add(if (bit == 1) ONE_SPACE else ZERO_SPACE)
            }
        }
        pulses.add(BIT_MARK)
        pulses.add(MESSAGE_GAP)
        return pulses.toIntArray()
    }
}
