package com.carlos.acremote.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta del panel de control (Home): siempre oscura, inspirada en un
 * control físico de A/C — a propósito independiente del tema claro/oscuro
 * del sistema, igual que un remoto físico no cambia con la hora del día.
 */
object RemotePalette {
    val panelTop = Color(0xFF262B34)
    val panelMid = Color(0xFF1D212A)
    val panelBottom = Color(0xFF16181F)

    val lcdBackground = Color(0xFF121318)
    val surface = Color(0xFF20242D)
    val surfaceRaised = Color(0xFF262B34)
    val border = Color(0xFF383D48)

    val textPrimary = Color(0xFFE8E9EC)
    val textMuted = Color(0xFF8A8F99)
    val textFaint = Color(0xFF5C616C)

    val accentCool = Color(0xFF38BDE8)
    val accentCoolSurface = Color(0xFF1D3742)
    val accentTurbo = Color(0xFFE8A23C)
    val accentTurboSurface = Color(0xFF3A2C16)
}
