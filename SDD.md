# SDD — App de Control de Aire Acondicionado por IR
### Documento de diseño para desarrollo con Claude Code

**Dispositivo objetivo:** Xiaomi Redmi Note 14 Pro+ (IR blaster nativo, HyperOS/Android)
**Plataforma:** Android nativo (Kotlin)
**Autor:** Carlos
**Estado:** Draft v1

---

## 1. Resumen ejecutivo

App Android que usa el IR blaster físico del teléfono (`ConsumerIrManager`) para emular el control remoto de un aire acondicionado. Sirve como proyecto de portafolio de IoT/Android nativo.

---

## 2. Objetivo y alcance

**MVP (v1):**
- Encender / apagar el A/C
- Subir / bajar temperatura
- Cambiar modo (frío, ventilador, auto)
- Guardar un "perfil" de A/C configurado una sola vez

**Fuera de alcance v1** (backlog v2):
- Múltiples equipos guardados
- Programación por horario
- Widget de pantalla de inicio
- Modo "aprendizaje" (capturar señal de un control físico existente)

---

## 3. Requisitos funcionales

| ID | Requisito |
|----|-----------|
| RF-01 | La app detecta si el dispositivo tiene IR blaster (`hasIrEmitter()`) y avisa si no lo tiene |
| RF-02 | El usuario selecciona marca/modelo de A/C de una lista o base de códigos IR |
| RF-03 | El usuario puede enviar comandos: power, temp+, temp-, modo, velocidad de ventilador |
| RF-04 | La app persiste el último estado enviado (temperatura, modo) para mostrarlo en UI |
| RF-05 | Pantalla de prueba/calibración: enviar un pulso IR y confirmar visualmente que se emitió |

## 4. Requisitos no funcionales

- **Compatibilidad:** minSdk 21+ (ConsumerIrManager existe desde API 19)
- **Sin conexión:** debe funcionar 100% offline (no requiere backend)
- **Latencia:** envío de comando IR en <200ms percibido
- **Fallback:** si el modelo exacto de A/C no está en la base de códigos, permitir input manual de código IR crudo (pattern de pulsos)

---

## 5. Arquitectura técnica

```
UI (Jetpack Compose)
   │
   ▼
ViewModel (estado de A/C: temp, modo, power)
   │
   ▼
IrCommandRepository ──► IrCodeDatabase (JSON local: marca → comandos → patrón de pulsos)
   │
   ▼
ConsumerIrManager (Android API) ──► Hardware IR blaster
```

**Stack:**
- Kotlin + Jetpack Compose
- `ConsumerIrManager` (`android.hardware.ConsumerIrManager`) — API nativa, sin librerías externas necesarias
- Persistencia local: DataStore (preferencias simples) — no hace falta Room para el MVP
- Base de códigos IR: archivo JSON embebido en `assets/`, estructura por marca/modelo

**Nota técnica clave:** `ConsumerIrManager` requiere el patrón de pulsos IR en microsegundos (`int[]`), no "códigos" en el sentido de control remoto universal. Vas a necesitar una base de datos de patrones (hay proyectos open-source como bases de datos de códigos IR de A/C, ej. formato usado por IRremoteESP8266, que puedes portar).

---

## 6. Modelo de datos (ejemplo)

```json
{
  "marca": "LG",
  "modelo": "generico_frio_1",
  "frecuencia_hz": 38000,
  "comandos": {
    "power_on": [9000, 4500, 560, 560, ...],
    "power_off": [9000, 4500, 560, 1690, ...],
    "temp_up": [...],
    "temp_down": [...]
  }
}
```

---

## 7. Pantallas / flujos

1. **Onboarding:** verifica IR blaster → selecciona marca de A/C
2. **Home:** control tipo "remoto" — power grande, +/- temperatura, selector de modo
3. **Configuración:** cambiar marca/modelo, input manual de código IR
4. **Test IR:** botón "probar señal" para verificar apuntando al equipo

---

## 8. Permisos requeridos

- `android.permission.TRANSMIT_IR` — normal permission, se declara en manifest, no requiere prompt en runtime

---

## 9. Plan de trabajo por fases (flujo spec-driven con Claude Code)

La idea del SDD es que cada fase se la pases a Claude Code como una **spec cerrada**, revises el resultado, y avances a la siguiente. No le pidas "hazme la app completa" de una vez.

### Fase 0 — Setup
```
Prompt sugerido:
"Crea un proyecto Android nativo en Kotlin con Jetpack Compose, package
com.carlos.acremote. Configura minSdk 21, agrega el permiso TRANSMIT_IR
en el manifest, y crea una función que detecte si el dispositivo tiene
IR blaster usando ConsumerIrManager.hasIrEmitter()."
```

### Fase 1 — Envío de señal IR básica
```
Prompt sugerido:
"Implementa un IrTransmitter que reciba una frecuencia en Hz y un array
de int con el patrón de pulsos, y lo envíe con ConsumerIrManager.transmit().
Agrega un botón de prueba en la UI que dispare un patrón de ejemplo."
```

### Fase 2 — Base de códigos IR
```
Prompt sugerido:
"Crea un archivo JSON en assets/ir_codes.json con la estructura [pega el
modelo de datos de la sección 6]. Implementa un IrCodeRepository que lea
el JSON y exponga los comandos por marca/modelo."
```

### Fase 3 — UI de control (Home)
```
Prompt sugerido:
"Crea la pantalla Home en Compose: botón power circular, controles +/-
de temperatura, selector de modo (chips). Conecta cada acción al
IrTransmitter usando el comando correspondiente del repositorio."
```

### Fase 4 — Onboarding y persistencia
```
Prompt sugerido:
"Agrega DataStore para guardar la marca/modelo seleccionado y el último
estado (temp, modo). Crea la pantalla de onboarding que se muestra solo
la primera vez."
```

### Fase 5 — Input manual de código IR (fallback)
```
Prompt sugerido:
"Agrega una pantalla donde el usuario pueda pegar un patrón de pulsos IR
en formato texto separado por comas y guardarlo como comando custom."
```

**Tip de flujo:** al terminar cada fase, corré la app en un emulador o dispositivo real (el emulador no tiene IR físico, así que la transmisión real solo se puede probar en tu Redmi) y confirmá antes de pasar a la siguiente fase. Si usás Claude Code con git, hacé commit al cierre de cada fase — así podés revertir si una fase rompe algo.

---

## 10. Estrategia de pruebas

- **Unitarias:** parsing del JSON de códigos, lógica de estado del ViewModel
- **Manual en dispositivo real:** cada comando IR debe probarse apuntando físicamente al A/C (no hay forma de automatizar esto sin hardware receptor IR)
- **Caso borde:** qué pasa si el usuario no tiene su marca en la base → debe caer en el flujo de input manual

---

## 11. Riesgos

| Riesgo | Mitigación |
|--------|------------|
| No existe base de códigos IR pública para tu marca exacta de A/C | Flujo de input manual (Fase 5) como fallback |
| `ConsumerIrManager` no está disponible en todos los fabricantes (algunos lo bloquean por software aunque el hardware exista) | Validar con `hasIrEmitter()` en el primer arranque y avisar claramente si falla |
| Patrones de pulsos incorrectos no dan error, simplemente no funcionan | Pantalla de "test IR" (RF-05) para debug rápido |

---

## Cómo usar este documento

Guardalo en la raíz del repo como `SDD.md` o `specs/design.md`. Al iniciar una sesión de Claude Code, referenciá la fase específica en la que estás trabajando en vez de pegar todo el documento — así el contexto se mantiene enfocado.
