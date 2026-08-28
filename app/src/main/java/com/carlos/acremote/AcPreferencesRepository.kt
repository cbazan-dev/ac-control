package com.carlos.acremote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ac_preferences")

data class AcPreferencesState(
    val marca: String?,
    val modelo: String?,
    val tempC: Int?,
    val modo: String?,
    val turbo: Boolean?,
    val ledEquipoOn: Boolean?,
    val onboardingCompleto: Boolean
)

/**
 * RF-04: persiste la marca/modelo elegidos y el último estado (temp, modo,
 * turbo, LED del equipo) enviado.
 */
class AcPreferencesRepository(private val context: Context) {

    private object Keys {
        val MARCA = stringPreferencesKey("marca")
        val MODELO = stringPreferencesKey("modelo")
        val TEMP_C = intPreferencesKey("temp_c")
        val MODO = stringPreferencesKey("modo")
        val TURBO = booleanPreferencesKey("turbo")
        val LED_EQUIPO_ON = booleanPreferencesKey("led_equipo_on")
        val ONBOARDING_COMPLETO = booleanPreferencesKey("onboarding_completo")
    }

    val state: Flow<AcPreferencesState> = context.dataStore.data.map { prefs ->
        AcPreferencesState(
            marca = prefs[Keys.MARCA],
            modelo = prefs[Keys.MODELO],
            tempC = prefs[Keys.TEMP_C],
            modo = prefs[Keys.MODO],
            turbo = prefs[Keys.TURBO],
            ledEquipoOn = prefs[Keys.LED_EQUIPO_ON],
            onboardingCompleto = prefs[Keys.ONBOARDING_COMPLETO] ?: false
        )
    }

    suspend fun guardarDispositivo(marca: String, modelo: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MARCA] = marca
            prefs[Keys.MODELO] = modelo
            prefs[Keys.ONBOARDING_COMPLETO] = true
        }
    }

    suspend fun guardarEstado(tempC: Int, modo: String, turbo: Boolean, ledEquipoOn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TEMP_C] = tempC
            prefs[Keys.MODO] = modo
            prefs[Keys.TURBO] = turbo
            prefs[Keys.LED_EQUIPO_ON] = ledEquipoOn
        }
    }
}
