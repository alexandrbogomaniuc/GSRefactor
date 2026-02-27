# Phase 1: Fix & Play — Make WinCraft Playable

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Fix all blockers preventing the WinCraft slot game from being playable — get the mock server running, fix missing assets, hide the debug panel, and verify a full spin→mine→settle cycle.

**Architecture:** The game uses PixiJS v8 for canvas rendering, a WebSocket mock server (`mock-server.js`) on port 6001 for simulated backend, and a Vite dev server on port 5174 for the frontend. The SlotEngine state machine drives all gameplay through states: INIT → IDLE → SPINNING → REEL_RESOLVE → MINING_PHASE → PAYOUT → SETTLED.

**Tech Stack:** TypeScript, PixiJS v8, Vite, WebSocket (ws library), GSAP, Node.js

---

## Task 1: Fix Missing Background Asset (404 Error)

**Files:**
- Copy from: `e:\Dev\GSRefactor\Gamesv1\WinCraft\game_description_and_examples\minecraft_hd_background.png`
- Copy to: `e:\Dev\GSRefactor\Gamesv1\WinCraft\Development\public\assets\minecraft_hd_background.png`

**Step 1: Copy the missing HD background image**

The `UIManager.initPixi()` loads `/assets/minecraft_hd_background.png` (line 94 of UIManager.ts) but this file doesn't exist in `public/assets/`. It exists in `game_description_and_examples/`.

Run:
```powershell
Copy-Item "e:\Dev\GSRefactor\Gamesv1\WinCraft\game_description_and_examples\minecraft_hd_background.png" "e:\Dev\GSRefactor\Gamesv1\WinCraft\Development\public\assets\minecraft_hd_background.png"
```
Expected: File copied successfully, no output.

**Step 2: Verify asset exists**

Run:
```powershell
Test-Path "e:\Dev\GSRefactor\Gamesv1\WinCraft\Development\public\assets\minecraft_hd_background.png"
```
Expected: `True`

---

## Task 2: Create Buy Bonus Background Asset

**Files:**
- Create: `e:\Dev\GSRefactor\Gamesv1\WinCraft\Development\public\assets\buy_bonus_bg.png`

The `style.css` (line 210) references `/assets/buy_bonus_bg.png` which doesn't exist.

**Step 1: Generate a golden Minecraft-themed "Buy Bonus" button background**

Use the `generate_image` tool to create a pixel-art golden shopping basket button, Minecraft-style, 140x140px, with a warm gold/amber color palette on a wooden plank background.

**Step 2: Save to the assets directory**

Move the generated image to `public/assets/buy_bonus_bg.png`.

---

## Task 3: Hide Debugger Panel Behind Keyboard Shortcut

**Files:**
- Modify: `e:\Dev\GSRefactor\Gamesv1\WinCraft\Development\src\main.ts`
- Modify: `e:\Dev\GSRefactor\Gamesv1\WinCraft\Development\src\style.css`

**Step 1: Set debug panel to hidden by default in CSS**

In `style.css`, find the `.debugger-panel` rule (around line 459) and add `display: none;`:

```css
/* BEFORE */
.debugger-panel {
  width: 300px;
  display: flex;
  flex-direction: column;
  position: absolute;
  right: 20px;
  top: 20px;
  max-height: 90vh;
}

/* AFTER */
.debugger-panel {
  width: 300px;
  display: none; /* Hidden by default - toggle with backtick key */
  flex-direction: column;
  position: absolute;
  right: 20px;
  top: 20px;
  max-height: 90vh;
  z-index: 999;
}
```

**Step 2: Add keyboard toggle in main.ts**

Add this code at the bottom of `main.ts` (after line 222):

```typescript
// Debug Panel Toggle (Backtick key)
document.addEventListener('keydown', (e) => {
  if (e.key === '`') {
    const panel = document.querySelector<HTMLDivElement>('.debugger-panel');
    if (panel) {
      panel.style.display = panel.style.display === 'none' ? 'flex' : 'none';
    }
  }
});
```

**Step 3: Verify visually**

- Open `http://localhost:5174/` in browser
- Confirm debug panel is NOT visible by default
- Press backtick (`) key → debug panel should appear
- Press again → should hide

---

## Task 4: Start Mock WebSocket Server

**Files:**
- Run: `e:\Dev\GSRefactor\Gamesv1\WinCraft\Development\mock-server.js`

**Step 1: Start the mock server**

The mock server uses `ws` library on port 6001. It is configured with an ESM `import` statement, and `package.json` has `"type": "module"`.

Run in a SEPARATE terminal:
```powershell
cd e:\Dev\GSRefactor\Gamesv1\WinCraft\Development
npm run mock
```
Expected output: `🤖 [GS Mock Websocket Server] Running on ws://localhost:6001`

**Step 2: Verify the dev server is also running**

In the primary terminal:
```powershell
cd e:\Dev\GSRefactor\Gamesv1\WinCraft\Development
npm run dev
```
Expected: Vite starts on `http://localhost:5174/`

**Step 3: Verify both are running**

Open `http://localhost:5174/` in browser. After clicking "CLICK TO START":
- The status should change from `🔴 Disconnected` to showing connection logs
- The `BALANCE` should update from `$0.00` to `$1,000.50`
- The SPIN button should become enabled

---

## Task 5: Fix the WebSocket Subprotocol Negotiation

**Files:**
- Modify: `e:\Dev\GSRefactor\Gamesv1\WinCraft\Development\mock-server.js`

**Step 1: Verify if the subprotocol causes connection failure**

The client in `GSWebSocketClient.ts` (line 42) connects with subprotocol `"abs.gs.v1"`:
```typescript
this.ws = new WebSocket(this.config.wssUrl, "abs.gs.v1");
```

But the mock server (line 4) creates a plain WebSocketServer without accepting that subprotocol:
```javascript
const wss = new WebSocketServer({ port: 6001 });
```

This CAN cause a WebSocket handshake failure. Update `mock-server.js` line 4:

```javascript
// BEFORE
const wss = new WebSocketServer({ port: 6001 });

// AFTER
const wss = new WebSocketServer({ 
    port: 6001,
    handleProtocols: (protocols) => {
        // Accept any subprotocol the client requests (for dev/mock purposes)
        return protocols.values().next().value || '';
    }
});
```

**Step 2: Restart the mock server**

Kill and restart `npm run mock`. Refresh the browser at `http://localhost:5174/`.

Expected: Connection should succeed. Console should show:
- `[GS Network] Connected. Sending Authentication Handshake (GAME_READY)...`
- `[GS Network] Session accepted by GS Orchestrator.`
- `Platform Status: 🟢 Connected`

---

## Task 6: Verify Full Spin-to-Settle Cycle

**Files:** No changes — this is a verification task.

**Step 1: Open the game**

Navigate to `http://localhost:5174/`. Click "CLICK TO START".

**Step 2: Verify IDLE state**

- Status: `🟢 Connected`
- Balance: `$1,000.50`
- Engine State: `IDLE`
- SPIN button is **enabled** (not greyed out)

**Step 3: Click SPIN**

- Reels should start spinning (blur animation)
- Console shows `[SlotEngine] Initiating Spin`
- Server responds with `BET_ACCEPTED` (within 300ms)
- Reels stop with staggered timing
- Mining phase begins (pickaxe drops, block breaking, gravity)
- Settle is sent automatically
- State returns to IDLE

**Step 4: Click SPIN again**

- Repeat to confirm the cycle is repeatable
- Balance should update after each round

**Step 5: Open debug panel with backtick key**

- Verify all state transitions are logged in the debug panel
- Confirm no error messages

---

## Task 7: Fix the Game Title and Favicon

**Files:**
- Modify: `e:\Dev\GSRefactor\Gamesv1\WinCraft\Development\index.html`

**Step 1: Update the HTML title and metadata**

```html
<!-- BEFORE -->
<title>slot-template</title>

<!-- AFTER -->
<title>WinCraft — Mine & Win!</title>
```

Also update the favicon link to use the game logo:
```html
<!-- BEFORE -->
<link rel="icon" type="image/svg+xml" href="/vite.svg" />

<!-- AFTER -->
<link rel="icon" type="image/png" href="/assets/logo.png" />
```

---

## Definition of Done (Phase 1)

- [ ] No 404 errors in browser console for any asset
- [ ] Debug panel is hidden by default, toggleable with backtick key
- [ ] Mock server connects successfully — `🟢 Connected` shown
- [ ] Balance updates to `$1,000.50` on connection
- [ ] Full SPIN → REEL_RESOLVE → MINING_PHASE → PAYOUT → SETTLED cycle works
- [ ] Multiple consecutive spins work without errors
- [ ] Game title shows "WinCraft" instead of "slot-template"
