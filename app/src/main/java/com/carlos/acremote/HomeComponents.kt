package com.carlos.acremote

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PowerButton(isOn: Boolean, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(96.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Icon(
            imageVector = Icons.Filled.PowerSettingsNew,
            contentDescription = if (isOn) "Apagar" else "Encender",
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun TemperatureControl(
    tempC: Int,
    enabled: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row {
        FilledIconButton(onClick = onDecrease, enabled = enabled) {
            Icon(Icons.Filled.Remove, contentDescription = "Bajar temperatura")
        }
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = "${tempC}°C",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.width(24.dp))
        FilledIconButton(onClick = onIncrease, enabled = enabled) {
            Icon(Icons.Filled.Add, contentDescription = "Subir temperatura")
        }
    }
}

@Composable
fun ModeSelector(
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit
) {
    Row {
        AcModes.ALL.forEachIndexed { index, modo ->
            FilterChip(
                selected = selected == modo,
                enabled = enabled,
                onClick = { onSelect(modo) },
                label = { Text(modo.replaceFirstChar { it.uppercase() }) }
            )
            if (index != AcModes.ALL.lastIndex) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
