# Vertical Slice Implementation Plan

## Architecture & Modules
To separate engine, game, and theme/config, we will structure the code as follows:

1. **Config (Theme/Timings)**
   - `src/game/config/GameConfig.ts`: Centralizes all sizes, timing (spin time, stagger), and easing values. 

2. **Game Core (Slots)**
   - `src/game/slots/Symbol.ts`: Represents a single cell on the reels. Will generate placeholder graphics based on a symbol ID.
   - `src/game/slots/Reel.ts`: Manages a single column of symbols. Implements "infinite spin" logic by wrapping symbols from bottom to top, and applies an elastic easing upon stopping.
   - `src/game/slots/SlotMachine.ts`: Orchestrates 5 reels. Exposes `spin()` and `stop()` methods with proper stagger delays.

3. **Visual FX (Layered)**
   - `src/game/fx/Particles.ts`: Simple particle burst system for wins using cheap `Graphics` or standard `Sprite`.
   - `src/game/fx/WinHighlight.ts`: A mock visual highlight (colored glows/rects) drawn over winning symbols.

4. **UI**
   - `src/app/ui/WinCounter.ts`: An overlay that animates numbers ticking up during a big win.

5. **Screen Setup (Main)**
   - `src/app/screens/main/MainScreen.ts`: We will gut the boilerplate screen and replace it with three explicit layers: `reelsLayer`, `fxLayer`, and `uiLayer`, plugging our SlotMachine, FX, and WinCounter into them.

## Execution Steps
1. Add `GameConfig.ts` & `Symbol.ts`.
2. Add `Reel.ts` & `SlotMachine.ts`.
3. Add FX and UI overlay scripts.
4. Wire it all up in `MainScreen.ts`.
5. Create small Git commits.
