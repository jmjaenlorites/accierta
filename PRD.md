# PRD — App de Porras con Predicciones

> **Nota:** Este PRD es el punto de partida conceptual. Las decisiones de diseño detalladas y las especificaciones formales están en `docs/specs/`. Las decisiones arquitectónicas en `docs/adr/`.

## Resumen

Aplicación web multijugador para hacer porras entre amigos. Cada grupo crea predicciones sobre cualquier evento, apuesta puntos ficticios, y compite por ver quién tiene mejor criterio a lo largo del tiempo. El dinero real lo gestiona el grupo fuera de la app.

---

## Problema

Las porras entre amigos se hacen por WhatsApp y se pierden. No hay historial, no hay leaderboard, no hay forma de saber quién acierta más a lo largo del tiempo. La resolución es caótica y siempre hay alguien que no se entera del resultado.

---

## Usuarios

Grupos pequeños y cerrados de amigos (3–8 personas). Uso esporádico pero recurrente — aparece cuando hay algo sobre lo que apostar.

---

## Flujo general

1. Alguien crea un grupo y comparte el link o código de invitación
2. Cualquier miembro crea una porra
3. Los demás apuestan antes de la fecha límite
4. Cuando llega la fecha de resolución, se activa el sistema de consenso
5. Se reparten los puntos y se actualiza el leaderboard

---

## Entidades principales

### Grupo
- Cerrado, acceso por link o código
- Auth requerida (email o similar)
- Todos los miembros tienen el mismo rol — cualquiera puede crear porras

### Porra
Tiene tres tipos:

| Tipo | Descripción | Ejemplo |
|------|-------------|---------|
| **Binaria** | Pasa o no pasa | ¿Ganará España el mundial? |
| **Numérica** | Valor exacto o más cercano | ¿Cuántos usuarios tendrá X en junio? |
| **Opción múltiple** | Elegir entre varias opciones | ¿Quién ganará la liga? A/B/C |

Campos al crear una porra:
- Título y descripción
- Tipo (binaria / numérica / opción múltiple)
- Opciones (si aplica)
- Tolerancia (solo tipo numérico) — margen para considerar que alguien acertó
- Fecha límite de apuesta
- Fecha de resolución
- Visibilidad de apuestas — por defecto visible, configurable al crear

### Apuesta
- Cada miembro apuesta antes de la fecha límite
- Todos apuestan la misma cantidad de puntos (definida por grupo o por porra, a decidir en implementación)
- No se puede modificar una apuesta una vez confirmada
- Si la visibilidad está activada, puedes ver las apuestas de los demás en tiempo real

---

## Sistema de resolución — consenso con disputa

Inspirado en mercados de predicciones reales. Diseñado para ser justo sin requerir un árbitro central.

### Flujo de resolución

```
Llega la fecha de resolución
        ↓
Cualquier miembro propone el resultado
        ↓
Período de espera: 48h
        ↓
¿Alguien disputa?
   ↓ No          ↓ Sí
Se acepta    Va a votación del grupo
                  ↓
           Mayoría decide
                  ↓
           Se acepta el resultado ganador
```

### Cancelación

- Cualquier miembro puede proponer cancelar una porra
- Sigue el mismo sistema de consenso que la resolución
- Si se cancela, se devuelven los puntos a todos los participantes

### Casos especiales — tipo numérico

- Si alguien está dentro del margen de tolerancia → gana
- Si nadie está dentro del margen → se devuelven los puntos a todos
- Si hay empate (dos o más personas igual de cercanas) → se splitea el bote entre ellos

---

## Reparto de puntos

- **Winner takes all** — el ganador o ganadores se llevan todo el bote
- **Split en empate** — si hay varios ganadores, el bote se divide a partes iguales
- **Todos apuestan lo mismo** — no hay proporcionalidad, solo quién acierta

---

## Leaderboard y perfil

Cada miembro tiene:
- Puntos actuales
- Historial de porras (acertadas, falladas, canceladas)
- Ratio de acierto global
- Racha actual

El leaderboard del grupo muestra el ranking en tiempo real.

---

## Vistas principales

| Vista | Descripción |
|-------|-------------|
| **Home del grupo** | Porras activas, próximas a cerrar, pendientes de resolución |
| **Porra detalle** | Apuestas visibles, estado, countdown a fechas clave |
| **Leaderboard** | Ranking del grupo con stats de cada miembro |
| **Historial** | Todas las porras resueltas con el resultado |
| **Perfil** | Stats personales y evolución en el tiempo |

---

## Notificaciones (a definir en implementación)

- Porra creada
- Quedan 24h para apostar
- Fecha límite de apuesta alcanzada
- Alguien propone resolución
- Alguien disputa una resolución
- Resolución aceptada / bote repartido

---

## Arquitectura técnica

### Stack sugerido

| Capa | Tecnología |
|------|------------|
| Backend | Node.js / Go |
| Base de datos | PostgreSQL |
| Real time | WebSockets |
| Auth | JWT + magic link o similar |
| Frontend | React |
| Deploy | Railway / Render / Fly.io |

### Por qué event sourcing encaja aquí

Cada acción relevante es un evento inmutable:

- `PorraCreada`
- `ApuestaRealizada`
- `ResolucionPropuesta`
- `ResolucionDisputada`
- `VotoEmitido`
- `ResolucionAceptada`
- `PorraCancelada`
- `PuntosSaldados`

El estado actual (saldo, leaderboard, resultado de una porra) se deriva de los eventos. Esto permite:
- Auditoría completa de cada porra
- Rebobinar y entender qué pasó en cualquier momento
- CQRS natural: escrituras como comandos, lecturas como proyecciones

### Real time

Websockets para:
- Actualización del leaderboard en tiempo real
- Notificaciones de nuevas apuestas (si visibilidad activada)
- Countdown sincronizado a fechas límite
- Actualizaciones del proceso de resolución

---

## Fuera del MVP

- Porras públicas (fuera de grupos cerrados)
- Torneos o ligas con varias rondas
- Integración con eventos reales (APIs de deportes, etc.)
- Resolución automática para ciertos tipos de eventos
- App móvil nativa
- Exportar historial
- Expulsión de miembros

---

## Plan de construcción — MVP

| Fase | Tiempo estimado | Qué se construye |
|------|----------------|------------------|
| 1 | 1h | Auth + creación de grupos + invitación |
| 2 | 2h | Crear porra (3 tipos) + apostar |
| 3 | 2h | Sistema de resolución con consenso |
| 4 | 1h | Leaderboard + historial |
| 5 | 1h | Real time con WebSockets |
| 6 | 1h | UI pulida + deploy |

**Total estimado: ~8h**