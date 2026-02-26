# PROJECT: WinCraft (ID: 5001)
# DOCUMENT: Master Context & Source of Truth

## 1. VISION
> A new slot game "WinCraft" developed under the Gamesv1 architecture, conceptually based on the reference video and the mine-slot web game. Game ID is 50001.

## 2. TECH STACK & ENVIRONMENT
* **OS:** Windows 11
* **Languages/Tools:** TypeScript, PixiJS, HTML/CSS (Based on your standard slot-template environment)

## 3. STATUS
### ✅ Completed
* Initial directory setup for WinCraft
* Received reference video (`IMG_6014.MP4`)
* Investigate `mine-slot` reference game URL (`https://mine-slot.inout.games/...`).
* Create game description, examples, screenshots in `game_description_and_examples` to let next AI agent reproduce UI/logic.
* Scaffold the new game clone in `e:\Dev\GSRefactor\Gamesv1\WinCraft\Development`.
* Update configuration and build processes for Game ID 50001
* Implement PixiJS UI and cascading reel logic (5x3 Top Reels + 5x7 Bottom Mining Grid).
* Implement Physics (Gravity drop, Bounce.Out), Pickaxe destruction logic, VFX logic.
* Perfect the Minecraft inventory aesthetic sizing and shading (drawRect 3D shadows).
* Embed a dynamic High-Definition Environment with "Wow factor" Parallax, Drifting Clouds (SCREEN blend), Glowing Spirit Deer, and High-Altitude Dragons.

### 🚧 In Progress (Current Focus)
* [Awaiting user input on the next major development phase. Likely integrating the final payout/multiplier server logic or polishing sound effects.]

### 📅 Backlog
* Advanced Sound Effects / BGM implementation.

## 4. ARCHIVE / NOTES
* All game-related assets and docs strictly reside in `e:\Dev\GSRefactor\Gamesv1\WinCraft\`.
* Using `chrome-devtools` server via MCP is crucial to inspect `window` properties on the running instance `http://localhost:5173`.
* Utilizing PIXI.js WebGL Blend Modes (`screen`, `multiply`, `add`) coupled with GSAP timelines creates massive, zero-overhead visual improvements for HD assets.
