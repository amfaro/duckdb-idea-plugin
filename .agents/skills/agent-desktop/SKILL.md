---
name: agent-desktop
description: Use when an agent needs to observe or control native desktop apps with structured accessibility data instead of screenshots, browser automation, or coordinate guessing. Load before using `agent-desktop`, `npx agent-desktop`, macOS Accessibility automation, desktop snapshots, element refs like `@e1`, or UI actions such as click/type/press/scroll outside a browser.
---

# Agent Desktop

Use `agent-desktop` for native desktop automation through OS accessibility trees. It gives structured JSON, deterministic element refs, and recovery hints, so prefer it over screenshot reading or coordinate clicks when the target is a desktop app.

Source: `https://github.com/lahfir/agent-desktop`.

## When to Use

Use for:

- observing Finder, System Settings, Slack, Xcode, Safari, or other GUI apps
- clicking, typing, selecting, scrolling, or pressing keys in native apps
- reading menus, sheets, popovers, alerts, notifications, or clipboard state
- checking UI state after a tool changed a desktop app
- minimizing token cost with accessibility skeletons instead of full screenshots

Do not use when:

- a direct CLI/API is safer and available
- web automation through Playwright/browser tools is the real target
- the requested action is destructive and the user has not confirmed it
- Accessibility permission is missing and cannot be requested in the session

## Install or Invoke

Preferred install:

```bash
npm install -g agent-desktop
```

No-install invocation:

```bash
npx agent-desktop snapshot --app Finder -i
```

Check platform and permission state:

```bash
agent-desktop status
agent-desktop permissions
agent-desktop permissions --request
```

macOS requires Accessibility permission for the terminal app running the agent: **System Settings > Privacy & Security > Accessibility**.

## Core Loop

Use observe → decide → act → observe. Re-observe after every action because refs are refreshed by snapshots and may become stale.

```bash
agent-desktop snapshot --app Finder -i --compact
agent-desktop click @e3
agent-desktop snapshot --app Finder -i --compact
```

For dense apps, use progressive skeleton traversal to save tokens:

```bash
# 1. Shallow overview. Containers with children get refs.
agent-desktop snapshot --skeleton --app Slack -i --compact

# 2. Drill into the relevant region.
agent-desktop snapshot --root @e3 -i --compact

# 3. Act on an element from the drill-down.
agent-desktop click @e12

# 4. Re-drill or snapshot to verify state.
agent-desktop snapshot --root @e3 -i --compact
```

## Snapshot Commands

```bash
agent-desktop snapshot --app Safari -i
agent-desktop snapshot --surface menu
agent-desktop snapshot --surface alert -i --compact
agent-desktop snapshot --root @e3 -i --compact
agent-desktop find --role button --app TextEdit
agent-desktop get @e3 value
agent-desktop is @e7 checked
agent-desktop list-surfaces --app Notes
```

Useful `snapshot` flags:

- `--app <NAME>`: filter to one app; default is focused app
- `-i` / `--interactive-only`: include only interactive elements
- `--compact`: omit empty structural nodes
- `--skeleton`: shallow overview with drillable container refs
- `--root <REF>`: traverse a region found in a prior snapshot
- `--surface <TYPE>`: `window`, `focused`, `menu`, `menubar`, `sheet`, `popover`, or `alert`
- `--include-bounds`: include pixel bounds only when needed; avoid coordinate-first automation

## Common Actions

Prefer ref-based actions over coordinates:

```bash
agent-desktop click @e3
agent-desktop double-click @e3
agent-desktop right-click @e3
agent-desktop type @e5 "quarterly report"
agent-desktop set-value @e5 "new value"
agent-desktop clear @e5
agent-desktop focus @e5
agent-desktop select @e9 "Option B"
agent-desktop toggle @e12
agent-desktop check @e12
agent-desktop uncheck @e12
agent-desktop scroll @e1 down 3
agent-desktop scroll-to @e20
agent-desktop press cmd+s
agent-desktop press escape
```

Use mouse coordinates only as a fallback after accessibility actions cannot target the element:

```bash
agent-desktop hover --xy 500,300
agent-desktop mouse-click --xy 500,300
```

## App, Window, Notifications, Clipboard

```bash
agent-desktop launch Safari
agent-desktop list-apps
agent-desktop list-windows --app Finder
agent-desktop focus-window w-4521
agent-desktop resize-window w-4521 800 600
agent-desktop move-window w-4521 100 100
agent-desktop minimize w-4521
agent-desktop maximize w-4521

agent-desktop list-notifications --app Slack --limit 5
agent-desktop dismiss-notification 1
agent-desktop notification-action 1 --action "Reply"

agent-desktop clipboard-get
agent-desktop clipboard-set "copied"
```

## Wait and Batch

Use waits instead of fixed sleeps when possible:

```bash
agent-desktop wait --element @e3 --timeout 5000
agent-desktop wait --window "Save" --timeout 10000
agent-desktop wait --text "Loading complete" --app Safari
agent-desktop wait --menu --timeout 3000
```

Batch only after each command is individually understood. Use `--stop-on-error` so later actions do not run against unexpected UI state:

```bash
agent-desktop batch '[
  {"command": "click", "args": {"ref_id": "@e2"}},
  {"command": "type", "args": {"ref_id": "@e5", "text": "hello"}},
  {"command": "press", "args": {"combo": "return"}}
]' --stop-on-error
```

## JSON and Error Handling

Commands return structured JSON. Check `ok` before trusting `data`.

Success shape:

```json
{
  "version": "1.0",
  "ok": true,
  "command": "click",
  "data": { "action": "click" }
}
```

Error shape:

```json
{
  "version": "1.0",
  "ok": false,
  "command": "click",
  "error": {
    "code": "STALE_REF",
    "message": "Element at @e7 no longer matches the last snapshot",
    "suggestion": "Run 'snapshot' to refresh refs, then retry"
  }
}
```

Recovery patterns:

- `PERM_DENIED`: run `agent-desktop permissions --request`, then ask user to grant Accessibility access if needed
- `STALE_REF`: run a fresh `snapshot`, find the element again, retry once
- `ELEMENT_NOT_FOUND`: broaden the snapshot or search with `find`
- `APP_NOT_FOUND`: run `list-apps` or launch/focus the target app
- `TIMEOUT`: inspect current UI with `snapshot`; do not blindly increase timeout

## Safety Rules

- Confirm before destructive app actions: deleting files, sending messages, making purchases, force quitting, dismissing many notifications, overwriting clipboard with sensitive data.
- Do not assume refs survive UI changes. Snapshot again.
- Prefer `--app` to avoid acting on the wrong focused app.
- Prefer `--skeleton` + `--root` for dense apps to avoid flooding context.
- Prefer AX/ref actions over `--xy` coordinates.
- If an action fails, follow the JSON `suggestion` before inventing a workaround.
