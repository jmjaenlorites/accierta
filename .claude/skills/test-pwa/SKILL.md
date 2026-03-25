---
name: test-pwa
description: How to validate PWA frontend functionality in a real browser using the Chrome MCP server. Use when implementing or modifying user-facing features — views, forms, real-time updates, notifications, responsive layout. Covers navigation, form interaction, WebSocket verification, and visual validation.
user-invocable: false
---

# Testing the PWA in Browser

## Access

The React PWA runs in Docker. **Check your port first** — it may differ in worktrees:

```bash
grep -A2 'frontend' docker-compose.override.yml 2>/dev/null || echo "Using default: 3000"
```

Default: `http://localhost:3000`

## Using Chrome MCP

The project has a Chrome MCP server configured. Use it to interact with the browser programmatically — navigate, click, fill forms, read page content, and take screenshots.

## What to test for each view

### Authentication views
1. Registration form: fill email + password, submit, verify success message
2. Login form: fill credentials, submit, verify redirect to group list
3. Email verification: simulate clicking the verification link
4. Password reset: request reset, verify email sent message

### Group views
1. Create group: fill name + initial balance, submit, verify group appears in list
2. Group home: verify porras list shows correct status badges (OPEN, CLOSED, RESOLVED)
3. Invitation: generate link, verify link is displayed/copyable
4. Leave group: verify confirmation dialog, verify redirect after leaving

### Porra views
1. Create porra form: test all 3 types (binary, numeric, multiple choice), verify validation
2. Place bet: verify correct input for each type, verify confirmation
3. Porra detail: verify bets visibility (hidden vs visible), countdown timer
4. Resolution flow: propose result, verify 48h countdown, dispute button

### Leaderboard
1. Verify ranking order matches balance
2. Verify stats (won, lost, accuracy) are correct
3. Verify real-time update after a resolution

### Real-time updates
1. Open porra detail in browser
2. Place a bet via curl (see test-api skill)
3. Verify the browser updates without refresh (WebSocket)
4. Same for leaderboard: resolve a porra via API, verify leaderboard updates

## Testing checklist per view

```
View: [name]
- [ ] Page loads without errors (check browser console)
- [ ] All data displays correctly
- [ ] Forms validate input (try submitting invalid data)
- [ ] Success actions redirect or show confirmation
- [ ] Error states show meaningful messages
- [ ] Responsive: test at mobile width (375px) and desktop (1280px)
- [ ] Real-time: if applicable, verify WebSocket updates work
```

## Screenshots

Use Chrome MCP to take screenshots after key actions. This helps verify layout and visual state without needing a human to look at the screen.
