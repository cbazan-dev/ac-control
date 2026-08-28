package com.carlos.acremote

import android.content.Context

/**
 * RF-02: expone los comandos IR de la base de códigos (assets/ir_codes.json) por marca/modelo.
 */
class IrCodeRepository(private val context: Context) {

    private val devices: List<IrDevice> by lazy {
        val json = context.assets.open("ir_codes.json").bufferedReader().use { it.readText() }
        IrCodeParser.parse(json)
    }

    fun getBrands(): List<String> = devices.map { it.marca }.distinct()

    fun getModels(marca: String): List<String> =
        devices.filter { it.marca.equals(marca, ignoreCase = true) }.map { it.modelo }

    fun getDevice(marca: String, modelo: String): IrDevice? =
        devices.find { it.marca.equals(marca, ignoreCase = true) && it.modelo.equals(modelo, ignoreCase = true) }

    fun getCommand(marca: String, modelo: String, comando: String): IntArray? =
        getDevice(marca, modelo)?.comandos?.get(comando)
}
