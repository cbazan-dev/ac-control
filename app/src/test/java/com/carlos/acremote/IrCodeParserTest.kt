package com.carlos.acremote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IrCodeParserTest {

    private val sampleJson = """
        [
          {
            "marca": "LG",
            "modelo": "generico_frio_1",
            "frecuencia_hz": 38000,
            "comandos": {
              "power_on": [9000, 4500, 560, 560],
              "power_off": [9000, 4500, 560, 1690]
            }
          },
          {
            "marca": "Sankey",
            "modelo": "generico_1",
            "frecuencia_hz": 38000,
            "comandos": {
              "power_on": [9000, 4500, 560, 560]
            }
          }
        ]
    """.trimIndent()

    @Test
    fun `parsea todos los dispositivos del JSON`() {
        val devices = IrCodeParser.parse(sampleJson)
        assertEquals(2, devices.size)
    }

    @Test
    fun `parsea marca, modelo y frecuencia correctamente`() {
        val devices = IrCodeParser.parse(sampleJson)
        val lg = devices.first { it.marca == "LG" }

        assertEquals("generico_frio_1", lg.modelo)
        assertEquals(38000, lg.frecuenciaHz)
    }

    @Test
    fun `parsea el patron de pulsos de cada comando`() {
        val devices = IrCodeParser.parse(sampleJson)
        val lg = devices.first { it.marca == "LG" }

        assertTrue(lg.comandos.containsKey("power_on"))
        assertArrayEquals(intArrayOf(9000, 4500, 560, 560), lg.comandos["power_on"])
    }

    private fun assertArrayEquals(expected: IntArray, actual: IntArray?) {
        assertEquals(expected.toList(), actual?.toList())
    }
}
