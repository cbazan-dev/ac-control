package com.carlos.acremote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElectraAcEncoderTest {

    @Test
    fun `arma los bytes de estado correctamente (power on, 24 grados, frio)`() {
        val bytes = ElectraAcEncoder.buildStateBytes(power = true, tempC = 24, modo = AcModes.FRIO)

        // Calculado a mano según el struct de ir_Electra.h:
        // byte0 = 0xC3 (fijo)
        // byte1 = swingOff(0b111) | (temp(24-8=16) << 3) = 7 | 128 = 135
        // byte2 = swingOff(0b111) << 5 = 224
        // byte4 = fanAuto(0b101) << 5 = 160
        // byte6 = modoCool(0b001) << 5 = 32
        // byte9 = power(1) << 5 = 32
        // byte11 = lightToggleOff = 8
        // byte12 = suma(byte0..byte11) mod 256 = 786 mod 256 = 18
        val esperado = intArrayOf(0xC3, 135, 224, 0, 160, 0, 32, 0, 0, 32, 0, 8, 18)

        assertEquals(esperado.toList(), bytes.toList())
    }

    @Test
    fun `el checksum es la suma de los primeros 12 bytes mod 256`() {
        val bytes = ElectraAcEncoder.buildStateBytes(power = false, tempC = 18, modo = AcModes.VENTILADOR)
        val sumaEsperada = bytes.take(12).sum() and 0xFF

        assertEquals(sumaEsperada, bytes[12])
    }

    @Test
    fun `el patron tiene longitud par y arranca con el header`() {
        val pattern = ElectraAcEncoder.buildPattern(power = true, tempC = 22, modo = AcModes.AUTO)

        assertTrue(pattern.size % 2 == 0)
        assertEquals(9166, pattern[0])
        assertEquals(4470, pattern[1])
        // header (2) + 13 bytes * 8 bits * 2 (mark+space) + footer mark/gap (2)
        assertEquals(2 + 13 * 8 * 2 + 2, pattern.size)
    }

    @Test
    fun `la temperatura se clampea al rango soportado por el protocolo`() {
        val bajo = ElectraAcEncoder.buildStateBytes(power = true, tempC = 5, modo = AcModes.FRIO)
        val alto = ElectraAcEncoder.buildStateBytes(power = true, tempC = 40, modo = AcModes.FRIO)

        // temp mínima (16) -> raw 8 -> byte1 = 7 | (8 << 3) = 71
        assertEquals(71, bajo[1])
        // temp máxima (32) -> raw 24 -> byte1 = 7 | (24 << 3) = 199
        assertEquals(199, alto[1])
    }

    @Test
    fun `turbo prende el bit 6 del byte 5 y se mantiene mientras este activo`() {
        val bytes = ElectraAcEncoder.buildStateBytes(power = true, tempC = 24, modo = AcModes.FRIO, turbo = true)

        // byte5 = turbo(1) << 6 = 64; byte12 = 786 + 64 = 850 mod 256 = 82
        val esperado = intArrayOf(0xC3, 135, 224, 0, 160, 64, 32, 0, 0, 32, 0, 8, 82)
        assertEquals(esperado.toList(), bytes.toList())
    }

    @Test
    fun `toggleLight manda el pulso de LED (0x15) solo en esa transmision`() {
        val conToggle = ElectraAcEncoder.buildStateBytes(power = true, tempC = 24, modo = AcModes.FRIO, toggleLight = true)
        val sinToggle = ElectraAcEncoder.buildStateBytes(power = true, tempC = 24, modo = AcModes.FRIO)

        // byte11 = 0x15 = 21; byte12 = 786 - 8 + 21 = 799 mod 256 = 31
        assertEquals(21, conToggle[11])
        assertEquals(31, conToggle[12])
        // el resto de las transmisiones (default) siguen mandando el valor "sin toggle"
        assertEquals(8, sinToggle[11])
    }
}
