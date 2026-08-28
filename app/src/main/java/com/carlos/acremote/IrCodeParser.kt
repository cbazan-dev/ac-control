package com.carlos.acremote

import org.json.JSONArray

/**
 * Parsea el JSON de la base de códigos IR (ver sección 6 del SDD).
 * Separado del repositorio para poder testearlo sin depender de Context/assets.
 */
object IrCodeParser {

    fun parse(json: String): List<IrDevice> {
        val devicesArray = JSONArray(json)
        return (0 until devicesArray.length()).map { i ->
            val deviceObj = devicesArray.getJSONObject(i)
            val comandosObj = deviceObj.getJSONObject("comandos")

            val comandos = mutableMapOf<String, IntArray>()
            val keys = comandosObj.keys()
            while (keys.hasNext()) {
                val comandoNombre = keys.next()
                val patternArray = comandosObj.getJSONArray(comandoNombre)
                comandos[comandoNombre] = IntArray(patternArray.length()) { idx -> patternArray.getInt(idx) }
            }

            IrDevice(
                marca = deviceObj.getString("marca"),
                modelo = deviceObj.getString("modelo"),
                frecuenciaHz = deviceObj.getInt("frecuencia_hz"),
                comandos = comandos
            )
        }
    }
}
