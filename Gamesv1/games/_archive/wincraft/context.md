# PROJECT: WinCraft (ID: 5001)
# DOCUMENT: Master Context & Source of Truth

## 1. VISION
> A new slot game "WinCraft" developed under the Gamesv1 architecture, conceptually based on the reference video and the mine-slot web game. Game ID is 50001.

## 2. TECH STACK & ENVIRONMENT
* **OS:** Windows 11
* **Languages/Tools:** TypeScript, PixiJS v8, HTML/CSS, GSAP, Web Audio API
* **Dev Server:** Vite on `http://localhost:5173/`
* **Mock Backend:** WebSocket on `ws://localhost:6001` (run with `npm run mock`)

## 3. STATUS
### ✅ Completed
* Initial directory setup, reference video, game description
* Scaffold game clone in `e:\Dev\GSRefactor\Gamesv1\WinCraft\Development`
* Implement PixiJS UI (5x3 Top Reels + 5x6 Mining Grid), Physics, VFX
* **Phase 1 "Fix & Play" — COMPLETE:**
  - Fixed missing assets, WebSocket subprotocol, debug panel, Spin/Buy Bonus buttons
  - Full SPIN→MINING→SETTLE cycle verified
* **Phase 2 "Visual Overhaul" — COMPLETE:**
  - MINE SLOT logo, 2 floating ghasts, animated chicken with win reaction
  - Procedural Minecraft pixel-art clouds (6 clouds at different depths/speeds)
* **Phase 3 "Animation & VFX Polish" — COMPLETE:**
  - Block-type specific debris particles (7 types)
  - Sparkle trails, impact flash, squash & stretch, crack overlays
  - Animated win counter, golden win burst particles, boosted screen shake
* **Phase 4 "Audio Design" — COMPLETE (2026-02-26):**
  - AudioManager singleton using Web Audio API synthesis (zero audio files needed)
  - 17 distinct synthesized sounds:
    * Block breaks: dirt, grass, stone, redstone, gold, diamond (frequency-tuned)
    * Pickaxe: metallic clang hit + descending whoosh drop
    * Reels: ticker spin + thunk stop
    * Chest: creaky wood open + sparkle pop
    * Wins: ascending C-major chimes (3 levels: small/big/mega with shimmer)
    * UI: button click, bet change pop, coin pickup pling
  - Mute/Unmute toggle via sound button (#btn-sound) with SVG icon swap
  - Audio initialized on user gesture (CLICK TO START) for browser autoplay policy
  - getSoundForBlockType() maps breakVfx types to sound names

### 🚧 In Progress (Current Focus)
* **Awaiting user QA/feedback** — User will test and report bugs

### 📅 Backlog
* Phase 5: Gameplay Features (TNT explosions, bonus round, day/night cycle)
* Phase 6: Mobile & Performance optimization
* Bug fixes from user QA

## 4. ARCHIVE / NOTES
* Mock server: `cd Development && npm run mock`
* Dev server: `cd Development && npm run dev`
* Debug panel toggle: press backtick (`) key
* AudioManager.getInstance().play('soundName') — singleton, lazy init
* VFXManager public: playBlockBreak, playImpactFlash, showFloatingText, playWinBurst
* Environment.ts: onWin() for chicken animation
* MiningGrid: getBlockVfxType(), getBlockTypeId(), _blockTypeIds[][]
* Sound button is #btn-sound, already in HTML with SVG speaker icon
