package com.carlos.acremote

import android.content.Context
import android.hardware.ConsumerIrManager

/**
 * RF-03 (base): envía un patrón de pulsos IR crudo por el emisor del dispositivo.
 */
class IrTransmitter(private val context: Context) {

    /**
     * @param frequencyHz frecuencia de la portadora, en Hz (ej. 38000 para la mayoría de A/C)
     * @param pattern patrón de pulsos en microsegundos: alterna encendido/apagado,
     *                debe tener longitud par
     * @return true si el patrón se envió, false si no hay IR blaster disponible
     */
    fun transmit(frequencyHz: Int, pattern: IntArray): Boolean {
        val irManager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
            ?: return false
        if (!irManager.hasIrEmitter()) return false

        irManager.transmit(frequencyHz, pattern)
        return true
    }
}
