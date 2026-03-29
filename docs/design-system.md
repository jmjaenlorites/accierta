# Design System — Accierta

## Brand personality

Accierta (from the Spanish "acertar" — to get it right, with an Italian twist) is a betting app for friend groups. The tone is **fun and competitive but refined** — think "friendly rivalry at a nice dinner", not "sports betting site".

### Design principles

1. **Playful but polished.** Microinteractions, animations, and emoji make it fun. Clean typography, generous spacing, and restrained color use keep it elegant.
2. **Mobile-first, always.** Every screen is designed for a phone first. Desktop is a wider version, not a different app.
3. **Real-time feels alive.** Countdowns tick, bets appear, leaderboards update. The app should feel like a live game, not a static page.
4. **Friends, not strangers.** The UI should feel intimate and personal — avatars, first names, inside jokes (custom currency). Never cold or corporate.
5. **Anti-betting-site.** No flashing banners, no aggressive CTAs, no dark patterns. This is the opposite of a gambling site. Think of it as a scoreboard for your friend group.

---

## Color palette

### Light mode

| Token | Hex | Usage |
|-------|-----|-------|
| `--primary` | `#6C5CE7` | Primary actions, links, active states. A warm purple that feels playful yet sophisticated. |
| `--primary-hover` | `#5A4BD1` | Hover state for primary |
| `--secondary` | `#00CEC9` | Secondary actions, success states, winning highlights |
| `--secondary-hover` | `#00B5B0` | Hover state for secondary |
| `--accent` | `#FDCB6E` | Attention-grabbers: deadlines approaching, streaks, badges |
| `--danger` | `#E17055` | Errors, losses, debt indicators |
| `--background` | `#FAFAFA` | Page background |
| `--surface` | `#FFFFFF` | Cards, panels, modals |
| `--surface-hover` | `#F5F5F5` | Hover state for interactive surfaces |
| `--text-primary` | `#2D3436` | Main text |
| `--text-secondary` | `#636E72` | Descriptions, labels, metadata |
| `--text-muted` | `#B2BEC3` | Placeholders, disabled states |
| `--border` | `#E0E0E0` | Dividers, card borders |

### Dark mode

| Token | Hex | Usage |
|-------|-----|-------|
| `--primary` | `#A29BFE` | Lighter purple for dark backgrounds |
| `--primary-hover` | `#B8B3FF` | Hover |
| `--secondary` | `#55EFC4` | Lighter teal |
| `--secondary-hover` | `#7DFFD4` | Hover |
| `--accent` | `#FFEAA7` | Warm yellow, slightly muted |
| `--danger` | `#FAB1A0` | Softer red-orange |
| `--background` | `#1A1A2E` | Deep blue-black (not pure black — warmer) |
| `--surface` | `#16213E` | Cards, panels |
| `--surface-hover` | `#1E2D50` | Hover |
| `--text-primary` | `#F5F5F5` | Main text |
| `--text-secondary` | `#A0A8B4` | Descriptions |
| `--text-muted` | `#5C6370` | Placeholders |
| `--border` | `#2D3748` | Dividers |

### Semantic colors (both modes)

| Token | Light | Dark | Usage |
|-------|-------|------|-------|
| `--win` | `#00B894` | `#55EFC4` | Winning states, positive money |
| `--loss` | `#D63031` | `#FF7675` | Losing states, negative money |
| `--pending` | `#FDCB6E` | `#FFEAA7` | Waiting states, approaching deadlines |
| `--neutral` | `#636E72` | `#A0A8B4` | Cancelled, inactive |

---

## Typography

### Font stack

- **Headings:** `"Plus Jakarta Sans", sans-serif` — geometric, modern, slightly rounded. Feels friendly but professional.
- **Body:** `"Inter", sans-serif` — excellent readability on screens, especially mobile. Widely used, well-hinted.
- **Monospace (numbers, stats):** `"JetBrains Mono", monospace` — for leaderboard numbers, countdowns, money amounts. Numbers should feel precise.

All via Google Fonts.

### Scale

| Token | Size | Weight | Usage |
|-------|------|--------|-------|
| `--text-xs` | 0.75rem (12px) | 400 | Timestamps, fine print |
| `--text-sm` | 0.875rem (14px) | 400 | Labels, secondary text |
| `--text-base` | 1rem (16px) | 400 | Body text |
| `--text-lg` | 1.125rem (18px) | 500 | Subtitles, card titles |
| `--text-xl` | 1.25rem (20px) | 600 | Section headings |
| `--text-2xl` | 1.5rem (24px) | 700 | Page titles |
| `--text-3xl` | 2rem (32px) | 800 | Hero numbers (leaderboard rank, money amounts) |

---

## Spacing and layout

### Grid

- **Mobile:** single column, 16px horizontal padding
- **Tablet (768px+):** 2 columns where appropriate
- **Desktop (1024px+):** max-width 480px centered for app-like feel (or 2-3 columns for dashboards)

### Spacing scale

Base unit: 4px. Use multiples: 4, 8, 12, 16, 24, 32, 48, 64.

| Token | Value | Usage |
|-------|-------|-------|
| `--space-1` | 4px | Tight spacing (icon + label) |
| `--space-2` | 8px | Between related elements |
| `--space-3` | 12px | Inside small components |
| `--space-4` | 16px | Standard padding, gap between cards |
| `--space-6` | 24px | Section spacing |
| `--space-8` | 32px | Major section breaks |
| `--space-12` | 48px | Page section gaps |

### Border radius

| Token | Value | Usage |
|-------|-------|-------|
| `--radius-sm` | 6px | Buttons, inputs |
| `--radius-md` | 12px | Cards, panels |
| `--radius-lg` | 16px | Modals, bottom sheets |
| `--radius-full` | 9999px | Avatars, badges, pills |

---

## Components

### Cards

Cards are the primary container. Every porra, debt, and member is a card.

```
┌──────────────────────────────┐
│  [Avatar] Title         [Tag]│  ← header: flex, space-between
│  Description text            │  ← body: text-secondary
│                              │
│  [Stat] [Stat] [Stat]       │  ← footer: stats row
│  ──────────────────────────  │
│  [Action button]             │  ← optional CTA
└──────────────────────────────┘
```

- Surface color background, 1px border, radius-md
- Subtle shadow: `0 1px 3px rgba(0,0,0,0.08)` (light) / `0 1px 3px rgba(0,0,0,0.3)` (dark)
- Interactive cards: hover lifts shadow slightly

### Buttons

| Variant | Usage | Style |
|---------|-------|-------|
| Primary | Main CTA (place bet, create porra) | Solid primary color, white text |
| Secondary | Alternative actions (view details) | Outlined, primary color border+text |
| Ghost | Tertiary actions (cancel, back) | No border, text only, hover shows surface |
| Danger | Destructive (leave group, delete) | Solid danger color |

All buttons: radius-sm, 44px minimum touch target (mobile accessibility), medium font weight.

### Tags / pills

Used for porra status, types, and outcomes.

| Status | Color | Label |
|--------|-------|-------|
| OPEN | `--primary` | Abierta |
| CLOSED | `--pending` | Cerrada |
| PENDING_RESOLUTION | `--accent` | En resolución |
| RESOLVED | `--win` | Resuelta |
| CANCELLED | `--neutral` | Cancelada |
| WON | `--win` | Ganada |
| LOST | `--loss` | Perdida |

Pill style: background at 15% opacity, text in full color, radius-full, text-xs, uppercase.

### Avatars

- Generated from user initials + deterministic color based on userId hash
- Sizes: 24px (inline), 32px (list), 40px (profile), 64px (leaderboard top 3)
- Radius-full (circle)

### Countdown timers

Used for betting deadlines and resolution waiting periods.

- Monospace font (JetBrains Mono)
- Format: `2d 14h 30m` (days, hours, minutes)
- Color transitions: `--text-secondary` → `--pending` (< 24h) → `--danger` (< 1h)
- Subtle pulse animation when < 1h

### Debt indicators

| State | Icon | Color | Text |
|-------|------|-------|------|
| You owe | Arrow up-right | `--danger` | "Debes 10€ a Alice" |
| Owed to you | Arrow down-left | `--win` | "Bob te debe 10€" |
| Payment requested | Clock | `--pending` | "Pago pendiente de confirmar" |
| Settled | Check circle | `--neutral` | "Pagado" |

---

## Icons

**Library:** Lucide React

Lucide is lightweight, consistent, and has excellent coverage for the features we need. It's tree-shakeable and works well with AI agents (descriptive names, easy to search).

Key icons:
- Navigation: `Home`, `Trophy`, `Bell`, `User`, `Settings`
- Porras: `Dices`, `Target`, `ListChecks`, `Hash`
- Actions: `Plus`, `Send`, `Check`, `X`, `Edit`, `Trash2`
- Money: `Coins`, `ArrowUpRight`, `ArrowDownLeft`, `Handshake`
- Status: `Clock`, `AlertCircle`, `CheckCircle`, `XCircle`
- Social: `Users`, `UserPlus`, `Link`, `Share2`

---

## Animations and microinteractions

### Principles

- Animations serve a purpose: feedback, state change, or delight
- Duration: 150-300ms for UI transitions, 500ms+ for celebrations
- Easing: `ease-out` for entrances, `ease-in` for exits
- Never block interaction — animations are non-blocking

### Specific animations

| Trigger | Animation | Duration |
|---------|-----------|----------|
| Bet placed | Card slides in from right + subtle scale bounce | 300ms |
| Bet modified | Card shakes gently (left-right wiggle) | 200ms |
| Countdown < 1h | Gentle pulse (scale 1.0 → 1.02) | 1000ms, infinite |
| Porra resolved | Confetti burst on winner's screen | 1500ms |
| Debt settled | Checkmark draws itself (stroke animation) | 500ms |
| Leaderboard change | Rows animate to new positions (slide up/down) | 400ms |
| New notification | Badge bounces once | 300ms |
| Page transitions | Fade + slight slide (8px) | 200ms |
| Pull to refresh | Elastic overscroll | native |
| Tab switch | Content crossfade | 150ms |

### Confetti

Only on genuinely exciting moments:
- You won a porra
- Your streak reaches 5, 10, 20...
- You reach #1 on the leaderboard for the first time

Use a lightweight confetti library (e.g., `canvas-confetti`). 50 particles, 1.5s duration, no sound.

---

## Layout patterns

### App shell

```
┌─────────────────────────┐
│  Header (logo + notif)  │  ← fixed top, 56px
├─────────────────────────┤
│                         │
│     Content area        │  ← scrollable
│                         │
├─────────────────────────┤
│  Bottom nav (4-5 tabs)  │  ← fixed bottom, 64px + safe area
└─────────────────────────┘
```

### Bottom navigation tabs

| Tab | Icon | Label |
|-----|------|-------|
| Home | `Home` | Inicio |
| Porras | `Dices` | Porras |
| Leaderboard | `Trophy` | Ranking |
| Debts | `Coins` | Deudas |
| Profile | `User` | Perfil |

Active tab: primary color icon + label. Inactive: text-muted.

### Notification center

- Accessed via bell icon in header
- Badge with unread count (red dot + number)
- Slide-in panel from right (mobile) or dropdown (desktop)
- Grouped by date: "Hoy", "Ayer", "Esta semana"
- Each notification: icon + text + timestamp + link to related item
- Swipe to dismiss (mobile)

---

## Responsive breakpoints

| Name | Width | Behavior |
|------|-------|----------|
| Mobile | < 768px | Single column, bottom nav, full-width cards |
| Tablet | 768px - 1023px | 2-column grid, bottom nav |
| Desktop | 1024px+ | Sidebar nav replaces bottom nav, max-width container |

---

## Accessibility

- All interactive elements: minimum 44x44px touch target
- Color contrast: WCAG AA minimum (4.5:1 for text, 3:1 for large text)
- Focus indicators: 2px solid primary, 2px offset
- Screen reader: all icons have aria-labels, status changes announced
- Reduced motion: respect `prefers-reduced-motion` — disable animations, keep content changes

---

## CSS framework

**Tailwind CSS** with a custom theme configuration matching the tokens above.

Why Tailwind:
- AI agents produce better Tailwind output than raw CSS (descriptive class names, well-documented)
- Utility-first pairs well with component-based React
- Custom theme maps directly to our design tokens
- Excellent dark mode support via `dark:` prefix
- Smallest bundle when properly purged

### Tailwind theme sketch

```js
// tailwind.config.js (key overrides)
{
  theme: {
    extend: {
      colors: {
        primary: { DEFAULT: '#6C5CE7', hover: '#5A4BD1' },
        secondary: { DEFAULT: '#00CEC9', hover: '#00B5B0' },
        accent: '#FDCB6E',
        danger: '#E17055',
        win: '#00B894',
        loss: '#D63031',
        pending: '#FDCB6E',
      },
      fontFamily: {
        heading: ['"Plus Jakarta Sans"', 'sans-serif'],
        body: ['"Inter"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      borderRadius: {
        sm: '6px',
        md: '12px',
        lg: '16px',
      },
    },
  },
}
```

---

## Language and copy

### Internationalization (i18n)

The app supports multiple languages. All user-facing text MUST go through the i18n system — no hardcoded strings in components.

**Supported locales:**
- `es-ES` — Spanish (default)
- `en-US` — English

**Implementation:** use `react-i18next` with JSON translation files per locale.

```
src/
  i18n/
    locales/
      es-ES.json
      en-US.json
    i18n.ts          # i18next configuration
```

**Locale detection:** browser language → user preference (stored in localStorage) → fallback to `es-ES`.

**Conventions:**
- Translation keys use dot notation: `porra.status.open`, `debt.action.requestPayment`
- Keys are grouped by feature: `auth.*`, `group.*`, `porra.*`, `resolution.*`, `debt.*`, `leaderboard.*`, `common.*`
- Plurals use i18next plural syntax: `{{count}} porra` / `{{count}} porras`
- Interpolation for dynamic values: `"{{name}} te debe {{amount}} {{currency}}"`
- Dates and numbers use `Intl.DateTimeFormat` and `Intl.NumberFormat` with the active locale

**Language selector:** in profile/settings. Minimal — flag icon + language name.

### Tone and copy guidelines

| | es-ES | en-US |
|---|---|---|
| **Register** | Informal "tú" | Casual "you" |
| **CTAs** | "Apostar", "Proponer resultado", "Confirmar pago" | "Place bet", "Propose result", "Confirm payment" |
| **Errors** | "Vaya, parece que ya has apostado en esta porra" | "Looks like you've already bet on this one" |
| **Empty states** | "Nadie ha apostado todavía. ¿Te atreves?" | "No bets yet. Feeling brave?" |
| **Currency** | Number + symbol: "10 €", "3 🍺" | Same format: "€10", "3 🍺" |
| **Dates** | "25 mar 2026, 14:30" | "Mar 25, 2026, 2:30 PM" |
| **Relative time** | "hace 2h", "en 3 días" | "2h ago", "in 3 days" |

- Keep copy short and direct in both languages
- Never formal or corporate — this is friends talking to friends
- Error messages should be helpful, not blaming
