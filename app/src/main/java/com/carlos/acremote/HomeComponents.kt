package com.carlos.acremote

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.acremote.ui.theme.RemotePalette

private fun modoIcon(modo: String) = when (modo) {
    AcModes.VENTILADOR -> Icons.Filled.Air
    AcModes.AUTO -> Icons.Filled.Autorenew
    else -> Icons.Filled.AcUnit
}

private fun modoLabel(modo: String) = when (modo) {
    AcModes.VENTILADOR -> "Ventilador"
    AcModes.AUTO -> "Auto"
    else -> "Frío"
}

@Composable
fun LcdDisplay(power: Boolean, tempC: Int, modo: String, turbo: Boolean, ledEquipoOn: Boolean) {
    val contentAlpha = if (power) 1f else 0.3f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RemotePalette.lcdBackground)
            .border(1.dp, RemotePalette.border, RoundedCornerShape(22.dp))
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().alpha(contentAlpha),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = modoIcon(modo),
                    contentDescription = null,
                    tint = if (power) RemotePalette.accentCool else RemotePalette.textMuted,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (power) modoLabel(modo) else "En espera",
                    color = RemotePalette.textMuted,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(text = "LED", color = RemotePalette.textFaint, fontSize = 9.sp)
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (power && ledEquipoOn) RemotePalette.accentCool else RemotePalette.textMuted)
                    )
                }
                if (power && turbo) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .background(RemotePalette.accentTurboSurface)
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = RemotePalette.accentTurbo, modifier = Modifier.size(12.dp))
                        Text(text = "TURBO", color = RemotePalette.accentTurbo, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().alpha(contentAlpha),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = if (power) "$tempC" else "--",
                color = if (power) RemotePalette.accentCool else RemotePalette.textMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 64.sp,
                lineHeight = 64.sp
            )
            if (power) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "°C",
                    color = RemotePalette.accentCool,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
fun PowerButton(isOn: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .shadow(
                elevation = if (isOn) 18.dp else 0.dp,
                shape = CircleShape,
                ambientColor = RemotePalette.accentCool,
                spotColor = RemotePalette.accentCool
            )
            .clip(CircleShape)
            .background(if (isOn) RemotePalette.accentCoolSurface else RemotePalette.surface)
            .border(1.dp, if (isOn) RemotePalette.accentCool else RemotePalette.border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.PowerSettingsNew,
                contentDescription = if (isOn) "Apagar" else "Encender",
                tint = if (isOn) RemotePalette.accentCool else RemotePalette.textPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun RemoteRoundButton(onClick: () -> Unit, enabled: Boolean, contentDescription: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(RemotePalette.surface)
            .border(1.dp, RemotePalette.border, CircleShape)
            .alpha(if (enabled) 1f else 0.4f),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(icon, contentDescription = contentDescription, tint = RemotePalette.textPrimary)
        }
    }
}

@Composable
fun TemperatureControl(
    tempC: Int,
    enabled: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(26.dp)) {
        RemoteRoundButton(onClick = onDecrease, enabled = enabled, contentDescription = "Bajar temperatura", icon = Icons.Filled.Remove)
        Text(text = "TEMP", color = RemotePalette.textMuted, fontSize = 10.sp, letterSpacing = 1.5.sp)
        RemoteRoundButton(onClick = onIncrease, enabled = enabled, contentDescription = "Subir temperatura", icon = Icons.Filled.Add)
    }
}

@Composable
fun ModeSelector(
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(RemotePalette.lcdBackground)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AcModes.ALL.forEach { modo ->
            val isSelected = selected == modo && enabled
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (isSelected) RemotePalette.accentCoolSurface else Color.Transparent)
                    .alpha(if (enabled) 1f else 0.4f)
                    .then(if (enabled) Modifier.clickable { onSelect(modo) } else Modifier)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = modoIcon(modo),
                    contentDescription = null,
                    tint = if (isSelected) RemotePalette.accentCool else RemotePalette.textMuted,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = modoLabel(modo),
                    color = if (isSelected) RemotePalette.accentCool else RemotePalette.textMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * @param onLongClick acción secundaria opcional (mantener apretado). La usa el
 *        toggle de LED para corregir el desfase con el equipo sin mandar IR.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    statusOnLabel: String,
    statusOffLabel: String,
    checked: Boolean,
    enabled: Boolean,
    accentColor: Color,
    accentSurface: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(if (checked && enabled) accentSurface else RemotePalette.surface)
            .border(1.dp, if (checked && enabled) accentColor else RemotePalette.border, RoundedCornerShape(15.dp))
            .alpha(if (enabled) 1f else 0.4f)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked && enabled) accentColor else RemotePalette.textMuted,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(text = label, color = RemotePalette.textPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
            Text(
                text = if (checked) statusOnLabel else statusOffLabel,
                color = RemotePalette.textMuted,
                fontSize = 10.sp
            )
        }
        Switch(
            checked = checked,
            // La fila entera maneja el toque; el switch es solo indicador.
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = accentSurface,
                checkedBorderColor = accentColor
            )
        )
    }
}
