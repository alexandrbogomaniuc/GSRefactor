# MineSlot Game Features and Logic Analysis
*Generated from visual analysis of the source URL.*

## 1. Visual Layout & Theme
* **Theme**: Minecraft-inspired mining expedition.
* **Background**: A bright blue sky with pixelated clouds, mountains in the distance, and grass/dirt layers at the bottom. Floating Ghasts (mobs) are visible in the sky.
* **Characters**: A pixel-art chicken stands to the right of the main reel grid.
* **Main Grid (Mining Shaft)**: A 5x5 grid situated "underground". Symbols include various block types:
  * Dirt/Grass
  * Stone
  * Redstone Ore
  * Gold Ore
  * Diamond Ore
* **Inventory Grid (Top)**: A 3x5 grid floating in the sky. This holds special collected items. Visible items include:
  * Pickaxes (Stone, Iron, Diamond) with numbers attached (durability or multiplier?)
  * TNT blocks
  * Eye of Ender symbols marked "BONUS"
  * Maps
* **UI Controls**:
  * **Top Right**: Hamburger Menu (≡) likely for rules/paytable.
  * **Bottom Left Settings**: Gear icon (⚙️) for sound/system settings.
  * **Bottom Left Balance**: Displays current balance (e.g., $1 000 000).
  * **Center Message**: Prompts action (e.g., "PLACE YOUR BET").
  * **Bottom Right Bet**: Bet controller with `+` and `-` buttons (default $3).
  * **Bottom Right Spin**: Large green circular arrows for Spin.
  * **Action Modifiers**: Auto-spin (A icon) and Turbo/Fast-play (>> icon) buttons next to Spin.
  * **Left Side**: A prominent yellow "BUY BONUS" button with a shopping basket icon.

## 2. Core Mechanics
* **Reel System**: The main grid is a 5x5 array. Blocks cascade from the top. Winning combinations of matching connected symbols (Cluster Pays) explode, allowing new blocks to fall simulating "mining."
* **Collection / Inventory System (The Top Grid)**:
  * The 3x5 inventory grid holds collected modifiers.
  * During the game, items like **Pickaxes**, **TNT**, and **Maps** can appear in the main grid or trigger effects.
  * **Pickaxes**: Represent multipliers or extra digging power. The attached numbers (1, 2, 3) likely denote charge or multiplier level.
  * **TNT**: Acts as a massive scatter or wildcard explosion, clearing large sections of the grid to force huge cascades.
* **Bonus Game / Free Spins**:
  * Triggered by collecting a specific number of **"BONUS" Eye of Ender** symbols in a single spin/cascade sequence.
  * Alternatively, players can purchase immediate entry via the heavy yellow **"BUY BONUS"** button on the left of the screen.
* **Bet Limits & Economics**:
  * Standard Bet Selection: Configurable via +/- controls at the bottom right.
  * Minimum Bet: 0.1 USD.
  * Maximum Bet: 20,000 USD.
  * Maximum Win Limit: 100,000 USD per round.

## 3. UI and Logical Flow (For Agent Reproduction)
To reproduce this game, the next AI agent should structure the PixiJS canvas into three main containers:
1. **Background Container**: Parallax scrolling sky, clouds, mountains, and animated Ghasts.
2. **Main HUD/Play Area**:
   - Central 5x5 grid (the actual "reels").
   - A floating 3x5 grid above it representing the "inventory state matrix."
   - The chicken mascot on the right (idle animations, react animations on win).
3. **Controls Footer**:
   - Fixed HTML/CSS overlay or Pixi objects for balance, bet size, and the main spin controls.
   - The "Buy Bonus" container positioned absolute on the left-center screen.
