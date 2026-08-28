package com.carlos.acremote

import android.content.Context
import android.hardware.ConsumerIrManager

/**
 * RF-01: detecta si el dispositivo tiene IR blaster disponible.
 */
object IrEmitterChecker {

    fun hasIrEmitter(context: Context): Boolean {
        val irManager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
            ?: return false
        return irManager.hasIrEmitter()
    }
}
