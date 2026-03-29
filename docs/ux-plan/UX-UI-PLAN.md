# Accierta — UX/UI Master Plan

## 1. Design Philosophy

Accierta is NOT a betting site. It's a **social game scoreboard** for friend groups. Every design decision follows from this:

- **Intimate, not corporate** — avatars, first names, custom currencies, inside jokes
- **Alive, not static** — countdowns tick, bets appear in real-time, leaderboards animate
- **Playful but polished** — microinteractions and emoji make it fun; clean typography keeps it elegant
- **Mobile-native feel** — bottom sheets, swipe gestures, haptic-like animations, no desktop patterns forced into mobile

---

## 2. Information Architecture

### Two-level navigation model

The app operates at two levels:
1. **App level** — selecting which group to interact with
2. **Group level** — all activity within a group (porras, ranking, debts, etc.)

Most users will have 1-3 groups. The primary experience is **inside a group**.

### Screen map

```
AUTH FLOW (no bottom nav)
├── WelcomePage — splash with brand, "Entrar" / "Crear cuenta" buttons
├── LoginPage — email + password, link to register/reset
├── RegisterPage — email + password, shows invitation context if token present
├── VerifyEmailPage — token input + resend button
└── ResetPasswordPage — email request / new password form

MAIN APP (bottom nav visible)
├── GroupsPage — list of groups + create group FAB
│   └── CreateGroupSheet — bottom sheet: name + currency
│
├── [GROUP CONTEXT] — selected group frames all tabs below
│   ├── Tab: Inicio (Home)
│   │   ├── Activity feed — recent events in the group
│   │   ├── Quick stats — your position, open porras count
│   │   └── Action cards — "Create porra", urgent items
│   │
│   ├── Tab: Porras
│   │   ├── Status filter pills — All / Open / Pending / Resolved
│   │   ├── Porra cards list — sortable by deadline
│   │   ├── CreatePorraSheet — multi-step bottom sheet
│   │   └── PorraDetailPage — full screen push
│   │       ├── Info section — title, description, type, stake, dates
│   │       ├── Bets section — list of bets (or hidden state)
│   │       ├── Bet form — type-specific input
│   │       ├── Resolution section — propose, dispute, vote
│   │       └── Management — delete (creator only)
│   │
│   ├── Tab: Ranking
│   │   ├── Podium — top 3 with large avatars + confetti moments
│   │   ├── Full ranking list — scrollable
│   │   └── Metric switcher — pills for ranking criteria
│   │
│   ├── Tab: Deudas
│   │   ├── Summary card — net balance ("Te deben 15€" / "Debes 8€")
│   │   ├── Tabs: Debo / Me deben / Consolidado
│   │   └── Debt cards with action buttons
│   │
│   └── Tab: Perfil (contextual to group)
│       ├── User card — avatar, email, language
│       ├── Group stats — wins, ratio, streak, money
│       ├── History — resolved porras with outcome
│       ├── Group members list
│       ├── Invite action
│       └── Leave group (danger zone)

STANDALONE (pushed full screen, no bottom nav)
├── InvitationPage — accept/register from invite link
├── PorraDetailPage — when accessed from anywhere
└── SettingsPage — language, notifications, about
```

---

## 3. Navigation & Interaction Patterns

### Bottom Tab Bar (5 tabs)

| Position | Icon | Label | Screen |
|----------|------|-------|--------|
| 1 | `Home` | Inicio | Group feed / activity |
| 2 | `Dices` | Porras | Porra list + create |
| 3 | `Trophy` | Ranking | Leaderboard |
| 4 | `Coins` | Deudas | Debt management |
| 5 | `User` | Perfil | Profile + group settings |

- Active: primary color icon + label + subtle top indicator line
- Inactive: `text-muted` color
- Height: 64px + safe area inset
- Tab switch: crossfade content (150ms)

### Group Selector

When user has multiple groups, a **horizontal pill selector** appears below the header:
- Scrollable horizontal list of group names as pills
- Active group has primary background
- Tapping switches context, all tabs update
- If only 1 group, selector is hidden

### Header

- **Height:** 56px fixed top
- **Left:** Group name (or "Accierta" on groups list)
- **Right:** Notification bell (with unread badge) + group avatar
- On scroll: header gets subtle backdrop blur + border

### Creation Flows (Bottom Sheets)

All creation flows use **bottom sheets** that slide up from the bottom:
- **Create Group** — single-step: name + currency picker
- **Create Porra** — multi-step wizard:
  - Step 1: Type selector (visual cards: Binary / Numeric / Multiple Choice)
  - Step 2: Details (title, description, stake)
  - Step 3: Options (type-specific: options list, tolerance slider)
  - Step 4: Dates (betting deadline, resolution date)
  - Step 5: Settings (visibility toggle, bet modification toggle)
  - Step 6: Review & confirm
- **Invite Member** — email input + generate link button
- **Place Bet** — type-specific input + confirm

Bottom sheet behavior:
- Drag handle at top (pill shape, 36x4px, centered)
- Swipe down to dismiss
- Background dimmed (black 40% opacity)
- Max height: 90% of viewport
- Border radius: radius-lg (16px) on top corners

### Page Transitions

- Tab switch: crossfade (150ms)
- Push to detail: slide from right (250ms, ease-out)
- Bottom sheet: slide up (300ms, ease-out)
- Modal dismiss: slide down (250ms, ease-in)

### Pull to Refresh

- Available on all list views (porras, ranking, debts, history)
- Elastic overscroll animation
- Refreshes data from API

### Swipe Actions

- Debt cards: swipe right to reveal "Mark paid" / "Request payment"
- Notification items: swipe left to dismiss

---

## 4. Screen-by-Screen Design Specs

### 4.1 Auth Flow

#### WelcomePage
```
┌─────────────────────────────┐
│                             │
│                             │
│         [App Logo]          │  Large avatar "A" with gradient
│         ACCIERTA            │  Brand name, heading font
│                             │
│   "Tu grupo, tu porra,     │  Tagline, text-secondary
│    tu pique semanal."       │
│                             │
│                             │
│  ┌───────────────────────┐  │
│  │       Entrar           │  │  Primary button, full width
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │     Crear cuenta       │  │  Secondary button, full width
│  └───────────────────────┘  │
│                             │
│   ¿Olvidaste tu contraseña? │  Ghost link, text-sm
│                             │
└─────────────────────────────┘
```
- No tabs, no clutter. Two clear paths.
- Logo has subtle entrance animation (scale 0.8→1 + fade, 500ms)

#### LoginPage
```
┌─────────────────────────────┐
│  ← Volver                   │  Ghost back button
│                             │
│  Bienvenido de vuelta       │  text-2xl, heading font
│  Entra en tu grupo          │  text-secondary
│                             │
│  Email                      │
│  ┌───────────────────────┐  │
│  │ tu@email.com           │  │  Field with label
│  └───────────────────────┘  │
│                             │
│  Contraseña                 │
│  ┌───────────────────────┐  │
│  │ ••••••••    👁         │  │  Password with toggle visibility
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │       Entrar           │  │  Primary button
│  └───────────────────────┘  │
│                             │
│  ¿No tienes cuenta?         │
│  Crear cuenta →             │  Link to register
│                             │
│  ¿Olvidaste tu contraseña?  │  Ghost link
│                             │
└─────────────────────────────┘
```

#### RegisterPage
```
┌─────────────────────────────┐
│  ← Volver                   │
│                             │
│  Crea tu cuenta             │  text-2xl
│  (Te invitó María a         │  Shown only if invitation token
│   "Porras del Curro")       │  present — text-secondary + accent
│                             │
│  Email                      │
│  ┌───────────────────────┐  │
│  │                         │  │
│  └───────────────────────┘  │
│                             │
│  Contraseña                 │
│  ┌───────────────────────┐  │
│  │          👁             │  │  Min 8 chars indicator below
│  └───────────────────────┘  │
│  Mínimo 8 caracteres       │  text-xs, text-muted
│                             │
│  ┌───────────────────────┐  │
│  │    Crear cuenta         │  │  Primary button
│  └───────────────────────┘  │
│                             │
│  ¿Ya tienes cuenta?         │
│  Entrar →                   │  Link to login
│                             │
└─────────────────────────────┘
```

#### VerifyEmailPage
```
┌─────────────────────────────┐
│                             │
│         ✉️                  │  Large icon
│                             │
│  Verifica tu email          │  text-2xl
│  Hemos enviado un código    │  text-secondary
│  a tu@email.com             │
│                             │
│  Token de verificación      │
│  ┌───────────────────────┐  │
│  │                         │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │   Verificar email       │  │  Primary
│  └───────────────────────┘  │
│                             │
│  ¿No lo recibiste?          │
│                             │
│  Email para reenviar        │
│  ┌───────────────────────┐  │
│  │                         │  │
│  └───────────────────────┘  │
│  Reenviar código →          │  Ghost button
│                             │
└─────────────────────────────┘
```

### 4.2 Groups Page

```
┌─────────────────────────────┐
│  Accierta            🔔(3)  │  Header
├─────────────────────────────┤
│                             │
│  Mis grupos                 │  text-2xl
│                             │
│  ┌───────────────────────┐  │  GroupCard
│  │ 🟣 Porras del Curro    │  │  Group name + avatar
│  │                         │  │
│  │ 👥 6 miembros  💰 €    │  │  Members + currency
│  │ 🎲 3 abiertas  ⏳ 2 pend│  │  Open porras + pending
│  │ 💸 Debes 15€            │  │  Debt summary (danger color)
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │  GroupCard
│  │ 🔵 Fantasy Liga        │  │
│  │                         │  │
│  │ 👥 4 miembros  💰 🍺   │  │
│  │ 🎲 1 abierta   ✅ 0 pend│  │
│  │ 🏆 Te deben 3🍺        │  │  (win color)
│  └───────────────────────┘  │
│                             │
│                             │
│                    [+]      │  FAB button, primary color
│                             │
├─────────────────────────────┤
│  🏠  🎲  🏆  💰  👤       │  Bottom nav (disabled until group selected)
└─────────────────────────────┘
```

- Group cards are tappable, navigate to group home
- FAB opens Create Group bottom sheet
- Cards show at-a-glance metrics pulled from insights API
- Empty state: friendly illustration + "Create your first group" CTA

### 4.3 Group Home (Inicio tab)

```
┌─────────────────────────────┐
│  Porras del Curro    🔔(2)  │  Header with group name
│  [Curro] [Fantasy] [+]     │  Group pills (if multiple groups)
├─────────────────────────────┤
│                             │
│  ┌───────────────────────┐  │  Summary card
│  │  Tu posición: #2       │  │  Large rank number (mono font)
│  │  Racha: 🔥 3           │  │  Streak
│  │  Neto: +25€            │  │  Net money (win color)
│  └───────────────────────┘  │
│                             │
│  ⚡ Necesita tu atención    │  Section title
│  ┌───────────────────────┐  │
│  │ 🎲 ¿Quién paga hoy?   │  │  Porra you haven't bet on
│  │    ⏰ 2h 15m            │  │  Countdown (danger, pulsing)
│  │    [Apostar →]          │  │
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │ 💰 Pedro te debe 10€   │  │  Pending debt
│  │    [Reclamar pago →]   │  │
│  └───────────────────────┘  │
│                             │
│  🎲 Porras abiertas        │  Section title
│  ┌───────────────────────┐  │
│  │ PorraCard (compact)    │  │  See component spec below
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │ PorraCard (compact)    │  │
│  └───────────────────────┘  │
│                             │
│  Ver todas →                │  Link to Porras tab
│                             │
├─────────────────────────────┤
│  🏠  🎲  🏆  💰  👤       │  Bottom nav
└─────────────────────────────┘
```

Key features:
- **"Needs attention" section** — urgency-driven: porras about to close, pending actions
- **Personal summary card** — your rank, streak, net money at a glance
- **Open porras preview** — max 3, with "See all" link to Porras tab

### 4.4 Porras Tab

```
┌─────────────────────────────┐
│  Porras del Curro    🔔     │  Header
├─────────────────────────────┤
│                             │
│  [Todas] [Abiertas] [Pendientes] [Resueltas]  │  Filter pills, horizontal scroll
│                             │
│  ┌───────────────────────┐  │  PorraCard
│  │ 🎲 ¿Quién gana la Liga?│  │  Title
│  │ Opción múltiple         │  │  Type pill
│  │                         │  │
│  │ 🎯 5 apuestas  💎 10€  │  │  Bets count + stake
│  │ ⏰ 2d 14h 30m          │  │  Countdown (color-coded)
│  │                         │  │
│  │ [ABIERTA]               │  │  Status pill
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │  PorraCard
│  │ 🔢 Goles en el derbi   │  │
│  │ Numérica · ±15%        │  │  Type + tolerance
│  │                         │  │
│  │ 🎯 3 apuestas  💎 5€   │  │
│  │ ⏰ 5h 20m  ⚠️          │  │  Warning icon < 24h
│  │                         │  │
│  │ [ABIERTA]  [Ya apostaste ✓] │  Status + your bet indicator
│  └───────────────────────┘  │
│                             │
│  ... more cards ...         │
│                             │
│                    [+]      │  FAB to create porra
│                             │
├─────────────────────────────┤
│  🏠  🎲  🏆  💰  👤       │
└─────────────────────────────┘
```

### 4.5 Create Porra (Multi-step Bottom Sheet)

**Step 1: Type**
```
━━━━━  (drag handle)

Crear porra                    Step 1 of 5

¿Qué tipo de porra?

┌───────────────────────────┐
│  ⚡ Binaria                │   Sí o no
│  "¿Ganará España?"        │   Example in text-muted
└───────────────────────────┘

┌───────────────────────────┐
│  🔢 Numérica               │   Valor exacto o más cercano
│  "¿Cuántos goles?"        │
└───────────────────────────┘

┌───────────────────────────┐
│  📋 Opción múltiple        │   Elegir entre opciones
│  "¿Quién gana la liga?"   │
└───────────────────────────┘
```

**Step 2: Details**
```
━━━━━

Crear porra                    Step 2 of 5

Título *
┌───────────────────────────┐
│ ¿Quién gana la Liga?      │
└───────────────────────────┘

Descripción (opcional)
┌───────────────────────────┐
│ La Liga 2025-2026...       │
│                            │
└───────────────────────────┘

Apuesta (en €) *
┌───────────────────────────┐
│ 10                         │
└───────────────────────────┘

        [← Anterior]  [Siguiente →]
```

**Step 3: Options** (type-specific)
- BINARY: no step needed (skip)
- NUMERIC: tolerance slider (5%—100%, default 15%)
- MULTIPLE_CHOICE: dynamic option list with "Add option" button (min 2)

**Step 4: Dates**
```
━━━━━

Crear porra                    Step 4 of 5

Fecha límite de apuesta *
┌───────────────────────────┐
│ 📅 25 mar 2026, 18:00     │   Native datetime picker
└───────────────────────────┘

Fecha de resolución *
┌───────────────────────────┐
│ 📅 30 mar 2026, 22:00     │   Must be after deadline
└───────────────────────────┘

        [← Anterior]  [Siguiente →]
```

**Step 5: Settings**
```
━━━━━

Crear porra                    Step 5 of 5

Visibilidad de apuestas
┌─────────────────────────────┐
│ 👁 Visibles                  │  Toggle
│ Los demás ven tu apuesta    │
│ al momento de apostar       │
└─────────────────────────────┘

Modificar apuesta
┌─────────────────────────────┐
│ ✏️ Permitir modificar       │  Toggle
│ Los participantes pueden    │
│ cambiar su apuesta antes    │
│ de la fecha límite          │
└─────────────────────────────┘

        [← Anterior]  [Crear porra ✓]
```

### 4.6 Porra Detail Page (Full Screen Push)

```
┌─────────────────────────────┐
│  ← Porras                   │  Back button
│                             │
│  ¿Quién gana la Liga?      │  text-2xl, heading
│  Opción múltiple · 10€     │  Type pill + stake
│                             │
│  La Liga 2025-2026, a ver   │  Description, text-secondary
│  quién acierta esta vez     │
│                             │
│  ┌─────────┬─────────┐     │
│  │ ⏰ Cierre │ 📅 Resoluc│     │  Two columns
│  │ 2d 14h   │ 30 mar   │     │  Countdown (mono font)
│  └─────────┴─────────┘     │
│                             │
│  ┌───────────────────────┐  │  Bet section
│  │  Tu apuesta            │  │
│  │  (no has apostado aún) │  │  or shows your bet
│  │                         │  │
│  │  Selecciona tu opción: │  │
│  │  ○ Real Madrid          │  │
│  │  ○ Barcelona            │  │
│  │  ○ Atlético             │  │
│  │  ○ Otro                 │  │
│  │                         │  │
│  │  [Apostar 10€ →]       │  │  Primary button
│  └───────────────────────┘  │
│                             │
│  Apuestas (4 de 6)         │  Section title
│  ┌───────────────────────┐  │
│  │ 🟣 María    Barcelona  │  │  (if visible)
│  │ 🔵 Pedro    Real Madrid │  │
│  │ 🟢 Ana      Atlético   │  │
│  │ 🟡 Carlos   Barcelona  │  │
│  └───────────────────────┘  │
│  o                          │
│  ┌───────────────────────┐  │
│  │ 🔒 Apuestas ocultas    │  │  (if hidden)
│  │    4 de 6 han apostado │  │  Only count shown
│  └───────────────────────┘  │
│                             │
│  --- (after resolution) --- │
│                             │
│  📋 Resolución              │  Section
│  ┌───────────────────────┐  │
│  │ María propone:          │  │
│  │ "Barcelona"             │  │  Proposed result
│  │                         │  │
│  │ ⏰ 47h 30m para disputar│  │  48h countdown
│  │                         │  │
│  │ [Disputar]  [Aceptar]  │  │  Danger + ghost buttons
│  └───────────────────────┘  │
│                             │
│  --- (if voting) ---        │
│                             │
│  🗳️ Votación               │  Section
│  ┌───────────────────────┐  │
│  │ Disputa de Pedro        │  │
│  │ "No fue Barcelona,     │  │
│  │  el campeonato se anuló"│  │
│  │                         │  │
│  │ Votos: 2 de 4          │  │  Progress indicator
│  │ ⏰ 23h para votar      │  │
│  │                         │  │
│  │ Tu voto:               │  │
│  │ ○ Barcelona             │  │
│  │ ○ Real Madrid           │  │
│  │ ○ Cancelar porra       │  │
│  │                         │  │
│  │ [Votar →]              │  │
│  └───────────────────────┘  │
│                             │
│  --- (creator zone) ---     │
│  ┌───────────────────────┐  │
│  │ ⚠️ Zona de peligro     │  │
│  │ [Eliminar porra]       │  │  Danger button
│  └───────────────────────┘  │
│                             │
└─────────────────────────────┘
```

### 4.7 Ranking Tab

```
┌─────────────────────────────┐
│  Ranking             🔔     │
│  [Curro] [Fantasy]          │  Group pills
├─────────────────────────────┤
│                             │
│  [Dinero] [Ganadas] [Ratio] [Racha]  │  Metric filter pills
│                             │
│  ┌───────────────────────┐  │  PODIUM
│  │       🥇              │  │
│  │   🟣 María            │  │  64px avatar
│  │   +45€  🔥5           │  │  Value + streak
│  │                         │  │
│  │  🥈          🥉       │  │  Smaller avatars
│  │ 🔵 Pedro   🟢 Ana     │  │
│  │ +30€       +22€       │  │
│  └───────────────────────┘  │
│                             │
│  4. 🟡 Carlos    +15€      │  Regular list
│  5. 🔴 Luis      +8€      │
│  6. 🟠 Tú        +5€  ←   │  "You" indicator
│  7. 🟤 Sara      -3€      │
│  8. ⬜ Jorge (salió) -10€  │  "Left" indicator, muted
│                             │
├─────────────────────────────┤
│  🏠  🎲  🏆  💰  👤       │
└─────────────────────────────┘
```

### 4.8 Debts Tab

```
┌─────────────────────────────┐
│  Deudas              🔔     │
├─────────────────────────────┤
│                             │
│  ┌───────────────────────┐  │  Net balance card
│  │  Balance neto          │  │
│  │  +15€                  │  │  Large mono number, win/loss color
│  │  Te deben más de lo    │  │
│  │  que debes             │  │  text-secondary explanation
│  └───────────────────────┘  │
│                             │
│  [Debo] [Me deben] [Neto]  │  Tab pills
│                             │
│  --- TAB: Debo ---          │
│  ┌───────────────────────┐  │
│  │ ↗️ Pedro               │  │  Arrow + member name
│  │ 10€ · "Goles derbi"   │  │  Amount + porra title
│  │ ⏳ Pendiente            │  │  Status
│  │              [Ya pagué] │  │  Action button
│  └───────────────────────┘  │
│                             │
│  --- TAB: Me deben ---      │
│  ┌───────────────────────┐  │
│  │ ↙️ María               │  │
│  │ 25€ · "Liga"           │  │
│  │ 💬 Pago solicitado     │  │  Status (pending color)
│  │           [Confirmar ✓] │  │  Confirm button
│  └───────────────────────┘  │
│                             │
│  --- TAB: Neto ---          │
│  ┌───────────────────────┐  │
│  │ Pedro     Debes  5€   │  │  Consolidated
│  │ María     Te debe 25€ │  │
│  │ Carlos    En paz ✓    │  │  Zero balance
│  └───────────────────────┘  │
│                             │
├─────────────────────────────┤
│  🏠  🎲  🏆  💰  👤       │
└─────────────────────────────┘
```

### 4.9 Profile Tab

```
┌─────────────────────────────┐
│  Perfil              ⚙️     │  Settings gear icon
├─────────────────────────────┤
│                             │
│  ┌───────────────────────┐  │
│  │     🟣                 │  │  Large avatar (64px)
│  │  Juan Martínez         │  │  Display name / email
│  │  juan@email.com        │  │  text-secondary
│  └───────────────────────┘  │
│                             │
│  📊 Estadísticas           │  Section
│  ┌──────┬──────┬──────┐   │
│  │  12  │  8   │ 67%  │   │  Metric cards
│  │Porras│Ganadas│Ratio │   │
│  └──────┴──────┴──────┘   │
│  ┌──────┬──────┬──────┐   │
│  │ +45€ │ 🔥5  │ 3    │   │
│  │ Neto │Racha │Pend. │   │
│  └──────┴──────┴──────┘   │
│                             │
│  📜 Historial              │  Section
│  ┌───────────────────────┐  │
│  │ ✅ Liga 2025           │  │  Won
│  │    Ganaste 30€         │  │  win color
│  │    hace 2 días         │  │
│  ├───────────────────────┤  │
│  │ ❌ Goles derbi         │  │  Lost
│  │    Perdiste 10€        │  │  loss color
│  │    hace 5 días         │  │
│  ├───────────────────────┤  │
│  │ ⊘ Oscar 2026          │  │  Cancelled
│  │    Cancelada            │  │  neutral color
│  │    hace 1 sem           │  │
│  └───────────────────────┘  │
│                             │
│  👥 Miembros (6)           │  Section
│  🟣 María · 🔵 Pedro      │  Avatar row
│  🟢 Ana · 🟡 Carlos       │
│  Ver todos →               │
│                             │
│  ┌───────────────────────┐  │
│  │  👋 Salir del grupo    │  │  Ghost danger button
│  └───────────────────────┘  │
│                             │
├─────────────────────────────┤
│  🏠  🎲  🏆  💰  👤       │
└─────────────────────────────┘
```

### 4.10 Invitation Page (Standalone)

```
┌─────────────────────────────┐
│                             │
│         [App Logo]          │
│         ACCIERTA            │
│                             │
│  ┌───────────────────────┐  │
│  │  María te invita a     │  │
│  │  "Porras del Curro"    │  │  Group name in heading
│  │                         │  │
│  │  👥 6 miembros · 💰 €  │  │  Group info
│  └───────────────────────┘  │
│                             │
│  --- If logged in ---       │
│  ┌───────────────────────┐  │
│  │   Unirme al grupo →    │  │  Primary button
│  └───────────────────────┘  │
│                             │
│  --- If not logged in ---   │
│  ┌───────────────────────┐  │
│  │   Crear cuenta →        │  │  Primary button
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │   Ya tengo cuenta →     │  │  Secondary button
│  └───────────────────────┘  │
│                             │
│  ⏰ Expira en 6 días       │  │  Expiry info
│                             │
└─────────────────────────────┘
```

---

## 5. Component Library Spec

### Core Components

| Component | Props | Description |
|-----------|-------|-------------|
| `AppShell` | children | Full layout: header + content + bottom nav |
| `Header` | title, leftAction?, rightActions? | Fixed top bar, 56px |
| `BottomNav` | activeTab, groupId? | 5-tab fixed bottom bar |
| `BottomSheet` | isOpen, onClose, title?, children | Slide-up modal with drag handle |
| `GroupPills` | groups, activeId, onSelect | Horizontal scrollable group selector |
| `Button` | variant (primary/secondary/ghost/danger), size, loading?, fullWidth? | Main action button |
| `IconButton` | icon, variant, size, badge? | Round icon-only button |
| `Pill` | variant (open/pending/resolved/cancelled/loss/win/neutral), size? | Status badge |
| `Avatar` | name, size (sm/md/lg/xl), color? | Initials-based avatar |
| `Card` | children, interactive?, onClick? | Surface container |
| `SectionTitle` | title, eyebrow?, action? | Section heading |
| `Field` | label, type, error?, hint? | Text input with label and validation |
| `SelectField` | label, options, error? | Native select with label |
| `TextAreaField` | label, placeholder?, error? | Textarea with label |
| `Toggle` | label, description?, checked, onChange | iOS-style toggle switch |
| `Slider` | label, min, max, value, onChange, unit? | Range slider with value display |
| `CountdownTimer` | deadline | Live countdown with color transitions |
| `MetricCard` | label, value, color? | Key-value stat display |
| `EmptyState` | icon, title, subtitle?, action? | Friendly empty state |
| `Skeleton` | variant (card/text/avatar) | Loading placeholder |
| `Toast` | message, kind (success/error/info) | Notification toast |
| `ConfirmDialog` | title, message, confirmLabel, onConfirm | Confirmation modal |
| `StepIndicator` | current, total | Multi-step progress dots |
| `PullToRefresh` | onRefresh, children | Pull-down refresh wrapper |

### Specialized Components

| Component | Props | Description |
|-----------|-------|-------------|
| `GroupCard` | group, insights | Group summary card for groups list |
| `PorraCard` | porra, compact? | Porra summary with countdown and status |
| `DebtCard` | debt, perspective (debtor/creditor), onAction | Debt with action button |
| `MemberRow` | member, rank?, stats? | Member in a list with optional ranking info |
| `BetForm` | porraType, options?, onSubmit | Type-specific bet input |
| `ResolutionPanel` | resolution, onDispute?, onVote? | Resolution status + actions |
| `PodiumDisplay` | top3 | Animated podium for leaderboard top 3 |
| `NetBalanceBanner` | amount, currency | Large net balance display |
| `ActivityItem` | event | Feed item for group home |
| `InviteSheet` | groupId, onInvite | Bottom sheet for invitation flow |

---

## 6. Animation & Microinteraction Spec

| Trigger | Animation | Duration | Details |
|---------|-----------|----------|---------|
| Page load | Skeleton → content fade-in | 300ms | Skeleton placeholders while loading |
| Tab switch | Crossfade | 150ms | Content opacity 0→1 |
| Push to detail | Slide from right | 250ms | ease-out |
| Bottom sheet open | Slide up + backdrop fade | 300ms | ease-out, backdrop black/40% |
| Bottom sheet close | Slide down + backdrop fade | 250ms | ease-in |
| Card tap | Scale 0.98 → 1.0 | 150ms | Subtle press feedback |
| Bet placed | Card slide in + bounce | 300ms | From right, scale 1.0→1.05→1.0 |
| Countdown < 1h | Gentle pulse | 1000ms loop | Scale 1.0→1.02→1.0 |
| Countdown < 10m | Faster pulse + danger color | 500ms loop | |
| Porra resolved (you won) | Confetti burst | 1500ms | canvas-confetti, 50 particles |
| Debt settled | Checkmark stroke animation | 500ms | SVG path animation |
| Leaderboard update | Rows slide to new positions | 400ms | Transform translateY |
| Toast appear | Slide down + fade in | 200ms | From top |
| Toast dismiss | Slide up + fade out | 150ms | |
| Notification badge | Bounce once | 300ms | Scale 1→1.3→1 |
| Pull to refresh | Elastic overscroll | native | |
| Empty state | Fade in + slight scale | 400ms | Scale 0.95→1 + opacity |
| Error shake | Horizontal wiggle | 300ms | translateX ±4px |

Respect `prefers-reduced-motion`: disable all animations, keep content changes instant.

---

## 7. Loading & Error States

### Loading
- **Page load:** Skeleton screens matching the layout of expected content
- **Action in progress:** Button shows spinner + disabled state
- **Data refresh:** Pull-to-refresh spinner, or inline spinner for sections

### Errors
- **Form validation:** Inline red text below field + red border
- **API error:** Toast notification (top, auto-dismiss 4s)
- **Network error:** Full-screen retry state with "Reintentar" button
- **Empty state:** Friendly message + illustration + action CTA

### Confirmation Dialogs (for destructive actions)
- Leave group → "¿Seguro que quieres salir? Tus deudas pendientes se mantienen."
- Delete porra → "Esta acción no se puede deshacer."
- Dispute resolution → "Esto abrirá una votación entre todos los participantes."

---

## 8. Real-time Behavior

WebSocket subscriptions per screen:
- **Group Home:** `/group/{id}/porras` for new/changed porras
- **Porra Detail:** `/porra/{id}` for new bets, resolution updates, votes
- **Ranking:** `/group/{id}/leaderboard` for ranking changes
- **Debts:** `/group/{id}/debts` for new debts, payment status
- **Notifications:** `/user/{id}/notifications` always connected

Real-time UX patterns:
- New data slides in with animation (not full page reload)
- Toast notification for important events in other tabs
- Badge count update on bottom nav tab for pending actions
- Leaderboard rows animate to new positions

---

## 9. Implementation Architecture

### File Structure

```
frontend/src/
├── main.tsx                     # Entry point
├── App.tsx                      # Router + providers
├── styles/
│   ├── tokens.css               # Design tokens (CSS variables)
│   ├── base.css                 # Reset + global styles
│   └── animations.css           # Keyframe animations
├── components/
│   ├── layout/
│   │   ├── AppShell.tsx         # Header + content + bottom nav
│   │   ├── Header.tsx           # Fixed top header
│   │   ├── BottomNav.tsx        # 5-tab navigation
│   │   ├── BottomSheet.tsx      # Reusable bottom sheet
│   │   └── GroupPills.tsx       # Group selector
│   ├── ui/
│   │   ├── Button.tsx
│   │   ├── IconButton.tsx
│   │   ├── Pill.tsx
│   │   ├── Avatar.tsx
│   │   ├── Card.tsx
│   │   ├── Field.tsx
│   │   ├── SelectField.tsx
│   │   ├── TextAreaField.tsx
│   │   ├── Toggle.tsx
│   │   ├── Slider.tsx
│   │   ├── CountdownTimer.tsx
│   │   ├── MetricCard.tsx
│   │   ├── EmptyState.tsx
│   │   ├── Skeleton.tsx
│   │   ├── Toast.tsx
│   │   ├── ConfirmDialog.tsx
│   │   ├── StepIndicator.tsx
│   │   └── PullToRefresh.tsx
│   ├── porra/
│   │   ├── PorraCard.tsx
│   │   ├── BetForm.tsx
│   │   ├── ResolutionPanel.tsx
│   │   └── CreatePorraSheet.tsx
│   ├── group/
│   │   ├── GroupCard.tsx
│   │   ├── CreateGroupSheet.tsx
│   │   └── InviteSheet.tsx
│   ├── ranking/
│   │   ├── PodiumDisplay.tsx
│   │   └── MemberRow.tsx
│   ├── debt/
│   │   ├── DebtCard.tsx
│   │   └── NetBalanceBanner.tsx
│   └── feed/
│       └── ActivityItem.tsx
├── pages/
│   ├── auth/
│   │   ├── WelcomePage.tsx
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── VerifyEmailPage.tsx
│   │   └── ResetPasswordPage.tsx
│   ├── GroupsPage.tsx
│   ├── group/
│   │   ├── GroupLayout.tsx      # Tab layout + group context
│   │   ├── GroupHomePage.tsx
│   │   ├── GroupPorrasPage.tsx
│   │   ├── GroupRankingPage.tsx
│   │   ├── GroupDebtsPage.tsx
│   │   └── GroupProfilePage.tsx
│   ├── PorraDetailPage.tsx
│   └── InvitationPage.tsx
├── lib/
│   ├── api.ts                   # API client with auth
│   ├── services.ts              # Service layer
│   ├── session.tsx              # Auth context + token management
│   ├── toasts.tsx               # Toast context
│   ├── stomp.ts                 # WebSocket client
│   ├── format.ts                # Date/number formatting
│   └── types.ts                 # TypeScript types
└── i18n/
    ├── i18n.ts                  # i18next config
    └── locales/
        ├── es-ES.json
        └── en-US.json
```

### Key Technical Decisions

1. **No UI framework** — custom components with CSS variables (already established)
2. **CSS approach** — CSS custom properties for tokens, component-scoped CSS classes in `app.css`
3. **State management** — React Context for auth/toasts, local state for pages, props for components
4. **Routing** — React Router 7 with nested routes for group context
5. **Data fetching** — service layer wrapping fetch with JWT injection
6. **WebSocket** — STOMP client for real-time (already implemented)
7. **i18n** — react-i18next (already implemented)
8. **Animations** — CSS keyframes + transitions (no animation library needed)

---

## 10. Implementation Priority

### Phase 1: Foundation (must have first)
1. Component library: all `ui/` and `layout/` components
2. Auth flow: Welcome → Login → Register → Verify → Reset (separate pages, not tabs)
3. App shell: Header + BottomNav + routing structure

### Phase 2: Core Experience
4. Groups page with GroupCards + Create Group sheet
5. Group layout with tab navigation + group pills
6. Group Home page with summary + attention items
7. Porras tab with filter + PorraCards

### Phase 3: Detail & Actions
8. Porra detail page (full redesign with all sections)
9. Create Porra multi-step bottom sheet
10. Bet form (type-specific)

### Phase 4: Social & Competition
11. Ranking tab with podium + full list
12. Debts tab with balance banner + debt cards
13. Profile tab with stats + history

### Phase 5: Polish
14. Invitation page redesign
15. Animations and microinteractions
16. Loading skeletons
17. Confirmation dialogs
18. Real-time WebSocket integration across all pages
19. Empty states with friendly illustrations

---

## 11. i18n Key Structure

```json
{
  "common": {
    "back": "Volver",
    "next": "Siguiente",
    "previous": "Anterior",
    "cancel": "Cancelar",
    "confirm": "Confirmar",
    "save": "Guardar",
    "delete": "Eliminar",
    "loading": "Cargando...",
    "retry": "Reintentar",
    "seeAll": "Ver todo",
    "search": "Buscar"
  },
  "auth": {
    "welcome": { "title": "...", "tagline": "...", "login": "...", "register": "..." },
    "login": { "title": "...", "subtitle": "...", "email": "...", "password": "...", "submit": "...", "noAccount": "...", "forgotPassword": "..." },
    "register": { "title": "...", "invitedBy": "...", "passwordHint": "...", "submit": "...", "hasAccount": "..." },
    "verify": { "title": "...", "subtitle": "...", "token": "...", "submit": "...", "resend": "...", "resendEmail": "..." },
    "reset": { "requestTitle": "...", "newPasswordTitle": "...", "submit": "..." }
  },
  "groups": {
    "title": "Mis grupos",
    "create": { "title": "...", "name": "...", "currency": "...", "submit": "..." },
    "card": { "members": "...", "openPorras": "...", "pending": "...", "youOwe": "...", "owedToYou": "..." },
    "empty": { "title": "...", "subtitle": "..." }
  },
  "home": {
    "position": "Tu posición",
    "streak": "Racha",
    "net": "Neto",
    "attention": "Necesita tu atención",
    "openPorras": "Porras abiertas"
  },
  "porra": {
    "status": { "open": "Abierta", "closed": "Cerrada", "pending": "En resolución", "resolved": "Resuelta", "cancelled": "Cancelada" },
    "type": { "binary": "Binaria", "numeric": "Numérica", "multipleChoice": "Opción múltiple" },
    "create": { "title": "Crear porra", "step": "Paso {{current}} de {{total}}", ... },
    "bet": { "place": "Apostar {{amount}}", "modify": "Modificar apuesta", "alreadyBet": "Ya apostaste", "hidden": "Apuestas ocultas" },
    "resolution": { ... },
    "detail": { ... }
  },
  "ranking": { ... },
  "debts": { ... },
  "profile": { ... },
  "invitation": { ... }
}
```
