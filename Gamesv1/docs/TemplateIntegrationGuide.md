# 🎰 GS Platform: 3rd Party Game Integration Guide

Welcome to the Game Server (GS) V1 Template! This guide explains how to use the underlying boilerplates to spin up a new HTML5 Slot Game, adjust its math model, and hook up new art assets.

## 1. Scaffolding a New Game
To create a new game, run the provided scaffolding script from the `Gamesv1` root directory:

```powershell
.\CLI-create-game.ps1 -GameName "MyNewSlot" -GameId "my_new_slot_v1"
```

This will duplicate the `slot-template` into a new folder named `MyNewSlot`, update the configuration files, and install the base `npm` dependencies.

## 2. Configuring the Math Model & DB Parameters
1. Navigate to your new game directory: `cd MyNewSlot`
2. Open `public/master-game-config.json` (or wherever your config lives). Update the `RTP`, `minBet`, `maxBet`, and exposure limits to match your Math Specification.
3. Open `config-ui.html` in your browser. This internal tool will read your updated configuration and output the raw SQL `INSERT` statements needed by the Backend team to register this game inside the GS Database.

## 3. Wiring up Art and Assets
By default, the template ships with a generic PIXI.js engine rendering colored squares.

### A. The Loading Screen (Preloader)
Open `index.html` and `src/style.css`.
- Replace the `.bgaming-logo` text with your game's logo SVG.
- The dual-colored wipe mask (`clip-path`) is already built-in. Just drop your logo vectors into the `.logo-outline` and `.logo-fill` sub-containers.

### B. The Reels and Symbols
Open `src/ui/Reel.ts`.
- The `createSymbol()` method currently generates PIXI.Graphics squares.
- **Action Required:** Replace these graphics with `PIXI.Sprite.from('assets/your_symbol.png')`.
- Hook up your sprite atlas in `main.ts` using `PIXI.Assets.load()`.

## 4. Understanding the GS WebSocket Protocol
You **should not** need to edit `src/network/GSWebSocketClient.ts`.
The template strictly enforces the `abs.gs.v1` interface.

- **Idempotency:** The `SlotEngine.ts` generates a UUID for `operationId` during `BET_REQUEST`. It passes that exact same UUID during `SETTLE_REQUEST`.
- **Drops & Reconnects:** If a socket drops during a spin (the `RESERVED` state), the client auto-sends `RECONNECT_REQUEST`. It will listen for `SESSION_SYNC` to either replay the missing result or drop back to `READY` if the server settled it cleanly.

## 5. Development & Testing
Run the local Vite dev server:
```bash
npm run dev
```

Navigate to `http://localhost:5174/?playerId=testuser`

### Using the Mock Server
You must run the local mock GS server to receive valid math evaluation grids.
See `docs/mock-server-readme.md` (if available) or ensure your backend dev environment is running locally on port 6001.

---
**Compliance Reminders:**
- **Do not** tamper with the `cert-hud` in `index.html`. It must always be visible.
- Autoplay loops **must** pause and clear their interval if the WebSocket emits a `SESSION_PAUSE` (Reality Check) or if the player's balance drops below the minimum bet.
