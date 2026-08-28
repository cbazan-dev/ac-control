package com.carlos.acremote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * RF-02: onboarding que se muestra solo la primera vez, para elegir marca/modelo.
 */
@Composable
fun OnboardingScreen(
    repository: IrCodeRepository,
    onFinished: (marca: String, modelo: String) -> Unit
) {
    val marcas = remember { repository.getBrands() }
    var marcaSeleccionada by remember { mutableStateOf<String?>(null) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Elegí la marca de tu aire acondicionado", style = MaterialTheme.typography.titleLarge)
            marcas.forEach { marca ->
                Button(
                    onClick = { marcaSeleccionada = marca },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(marca)
                }
            }

            marcaSeleccionada?.let { marca ->
                Text("Elegí el modelo", style = MaterialTheme.typography.titleLarge)
                val modelos = remember(marca) { repository.getModels(marca) }
                modelos.forEach { modelo ->
                    Button(
                        onClick = { onFinished(marca, modelo) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(modelo)
                    }
                }
            }
        }
    }
}
