package com.carlos.acremote

/**
 * Patrón de ejemplo para probar el emisor IR (Fase 1).
 * No corresponde a ningún comando real de A/C todavía: eso llega
 * con la base de códigos IR (Fase 2).
 */
object SamplePatterns {
    const val FREQUENCY_HZ = 38000

    val TEST_PATTERN = intArrayOf(
        9000, 4500,
        560, 560, 560, 1690, 560, 560, 560, 1690,
        560, 560, 560, 560, 560, 560, 560, 560,
        560, 39000
    )
}
