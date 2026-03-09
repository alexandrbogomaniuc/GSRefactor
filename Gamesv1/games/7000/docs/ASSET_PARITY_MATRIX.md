## Asset Parity Matrix

Scope:
- Base branch for this audit: `codex/qa/7000-beta3-visual-parity-20260309-1410`
- Donor source of truth for archive availability: `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/`
- Current runtime providers inspected on this branch:
  - `Gamesv1/games/7000/assets/providers/openai/runtime/`
  - `Gamesv1/games/7000/assets/providers/nanobanana/runtime/`

Legend:
- `YES` = category exists in usable form.
- `PARTIAL` = category exists, but only as a reduced or generic subset.
- `NO` = category is absent for current runtime use.

| Category | Donor archive available | OpenAI runtime provider | NanoBanana runtime provider | Game currently consumes in runtime | Evidence / gap |
| --- | --- | --- | --- | --- | --- |
| Preloader background / wordmark / bar treatment | `YES` | `PARTIAL` | `PARTIAL` | `PARTIAL` | Donor has `image/startScreen1.222f7cdf.png`, `image/startScreen2.d0b00680.png`, `image/logo.2ea1fa99.png`, `svg/logo-menu.4a9c7cb3.svg`. OpenAI and NanoBanana only ship the three scene backgrounds plus a shared wordmark URL; no runtime bar art exists. The game currently uses a custom text lockup and code-drawn bar in [LoadScreen.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/app/screens/LoadScreen.ts). |
| Reel cabinet / frame | `YES` | `PARTIAL` | `PARTIAL` | `PARTIAL` | Donor has `image/slot.d8edf336.png`, `anims_v5/slot_render.atlas`, `anims_v5/slot_render.png`, `anims_v5/border2.*`, `anims_v5/border_small.*`. OpenAI ships `reel-frame-panel` plus separators; NanoBanana only ships `reel-frame-panel`. The game consumes only `uiAtlas.reel-frame-panel` in [CrazyRoosterSlotMachine.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/slots/CrazyRoosterSlotMachine.ts) and draws the rest in code. |
| Jackpot plaques | `YES` | `PARTIAL` | `PARTIAL` | `PARTIAL` | Donor has `image/img_mini_coin.e12ee50e.png`, `image/img_minor_coin.fea1c83b.png`, `image/img_major_coin.b2d60eb9.png`, `image/img_grand_coin.68e52468.png`, plus `anims_v5/icon_coin_*`. OpenAI and NanoBanana only ship `coin-multiplier-2x/3x/5x/10x` in `atlas_symbols.json`. The game uses those coins in [Beta3VisualChrome.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/app/screens/main/Beta3VisualChrome.ts), not dedicated plaques. |
| Mascot / topper art | `YES` | `PARTIAL` | `PARTIAL` | `PARTIAL` | Donor has `anims_v5/gold_chicken.*` and dedicated staged topper imagery in the archive. OpenAI and NanoBanana only expose `symbol-9-rooster`. The game uses the rooster symbol as a stand-in topper in [Beta3VisualChrome.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/app/screens/main/Beta3VisualChrome.ts). |
| Symbol atlas completeness | `YES` | `YES` | `YES` | `YES` | Donor archive has `anims_v5/icons_slot.*` and individual `icon_*` files. Both runtime providers now expose `symbol-0-egg` through `symbol-9-rooster`, `collector-symbol`, and the multiplier coin frames. The game consumes the full symbol set in [CrazyRoosterSymbol.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/slots/CrazyRoosterSymbol.ts). |
| Buy bonus tile | `YES` | `PARTIAL` | `NO` | `PARTIAL` | Donor has `image/btn_buy_bonus.1d85b9e0.png`, `anims_v5/buy_bonus_render.*`, and `buy_bonus_blitz/power/ultimate`. OpenAI ships `button-buybonus` and `button-buybonus-pressed`; NanoBanana ships no buy-bonus UI art on this branch. The game currently uses `collector-symbol` as a surrogate in [Beta3VisualChrome.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/app/screens/main/Beta3VisualChrome.ts). |
| Action buttons / bottom chrome | `YES` | `PARTIAL` | `NO` | `NO` | Donor archive has `image/hold_for_turbo.9418c1be.png`, donor runtime `ui_keys.json` includes `button-spin`, `button-spin-pressed`, `button-autoplay`, `button-buybonus`, `button-buybonus-pressed`, `button-bet-plus`, `button-bet-minus`, `payline-pill`, separators. OpenAI ships that same subset. NanoBanana ships only `reel-frame-panel`. The game still uses generic shell buttons via [PremiumTemplateHud.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/packages/ui-kit/src/hud/PremiumTemplateHud.ts) and [Button.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/packages/ui-kit/src/ui/Button.ts). |
| Boost / lightning VFX | `YES` | `YES` | `PARTIAL` | `PARTIAL` | Donor has `anims_v5/lightning_path2.*`, `anims_v5/lightning_path.json`, `anims_v5/fx_fire.json`, `anims_v5/fx_fireball.json`, `anims_v5/fx_render.*`. OpenAI ships `lightning-arc-01..06`, `collector-ring`, `spark-burst-01..03`. NanoBanana ships only `lightning-arc-01`. The game uses only `lightning-arc-01` in [LightningArcFx.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/fx/LightningArcFx.ts). |
| Big win / total win overlays | `YES` | `NO` | `NO` | `NO` | Donor has `anims_v5/big_win_landscape.json`, `big_win_mobile.json`, `mega_win_landscape.json`, `mega_win_mobile.json`, `total_win_landscape.json`, `total_win_mobile.json`, `win_popup.*`, `totalwin_tab.json`. Neither runtime provider ships equivalent assets. The game uses shared shell/UI-kit overlays (`WowVfxOrchestrator`, `WinCounter`, `WinHighlight`) instead of provider art. |
| Bonus tutorial / feature-entry overlays | `YES` | `NO` | `NO` | `NO` | Donor has `anims_v5/bonus_game_tutorial.json` and `buy_bonus_blitz.json`, `buy_bonus_power.json`, `buy_bonus_ultimate.json`. Neither runtime provider ships equivalent feature-entry/tutorial overlays. The game currently emits generic cues such as `hold-and-win-frame` from [demoRuntime.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/app/runtime/demoRuntime.ts). |
| Ambient particles / environmental motion | `YES` | `PARTIAL` | `NO` | `PARTIAL` | Donor has `anims_v5/coin_fly.*`, `coins_render.*`, `dots.json`, `fx_render.*`, `shadow.281fa756.png`. OpenAI only supplies spark/collector ring primitives; NanoBanana has no ambient runtime pack on this branch. The game currently uses code-driven glow and [ParticleBurst.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/fx/ParticleBurst.ts). |
| SFX coverage | `YES` | `NO` | `NO` | `PARTIAL` | Donor archive has 92 audio files in `media/`, including reel, bonus, lightning, coin, modal, and background music coverage. Neither runtime provider ships donor-grade audio. The game uses generic shell hover/press cues and `bgm-main.mp3` from [assetKeys.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/app/assets/assetKeys.ts). |

## Current Runtime Consumption Summary

Already consumed on this branch:
- Provider backgrounds via [MainScreen.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/app/screens/main/MainScreen.ts)
- Full symbol atlas plus `collector-symbol` and multiplier coins via [CrazyRoosterSymbol.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/slots/CrazyRoosterSymbol.ts) and [Beta3VisualChrome.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/app/screens/main/Beta3VisualChrome.ts)
- `uiAtlas.reel-frame-panel` via [CrazyRoosterSlotMachine.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/slots/CrazyRoosterSlotMachine.ts)
- `vfxAtlas.lightning-arc-01` via [LightningArcFx.ts](/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/games/7000/src/game/fx/LightningArcFx.ts)

No-op wiring result for this pass:
- On the requested Beta 3 base branch, there are no additional unused NanoBanana hero keys already present in `assets/providers/nanobanana/runtime/` beyond the assets the game is already consuming.
- This pass therefore keeps runtime wiring stable and focuses on documenting the exact missing asset surface.

## Top 5 Missing Art Categories Blocking Premium Quality

1. Preloader-specific art package.
   Missing a provider-grade start-screen background/lockup/bar treatment, so the preloader still reads as themed shell graphics instead of a premium staged composition.
2. Bottom control chrome and buy-bonus tile art.
   The donor has dedicated button/tile assets; current Beta 3 still reads as generic shell controls because NanoBanana lacks runtime button art on this branch.
3. Jackpot plaques and topper/mascot package.
   The donor presents a staged jackpot/header system. Current runtime only has coin multipliers and the rooster symbol, which is not enough for a premium topper.
4. Big-win / total-win / feature-entry overlay package.
   The donor has dedicated win and tutorial overlays; current runtime falls back to shared shell presentation, which lowers visual parity immediately.
5. Cabinet/side-chrome support art and environmental motion.
   The donor has richer border, slot, shadow, coin-fly, and FX layers. Current runtime still relies heavily on code-drawn chrome and generic particles.

## Exact Runtime Keys / Assets Still Missing For Premium Parity

### Exact keys missing from the current NanoBanana runtime atlases on this branch

`atlas_ui.json` is missing:
- `button-spin`
- `button-spin-pressed`
- `button-autoplay`
- `button-buybonus`
- `button-buybonus-pressed`
- `button-bet-plus`
- `button-bet-minus`
- `payline-pill`
- `reel-separator-horizontal`
- `reel-separator-vertical`

`atlas_vfx.json` is missing:
- `collector-ring`
- `lightning-arc-02`
- `lightning-arc-03`
- `lightning-arc-04`
- `lightning-arc-05`
- `lightning-arc-06`
- `spark-burst-01`
- `spark-burst-02`
- `spark-burst-03`

### Additional runtime assets Engineering needs beyond the current minimal contract

If Engineering wants premium parity, the NanoBanana thread still needs to supply a dedicated staged-presentation layer. The exact runtime assets/keys still missing are:

- Preloader assets:
  - provider-specific preloader background(s) matching the staged donor start screen
  - provider-specific wordmark/lockup asset
  - preloader bar frame asset
  - preloader bar fill/sheen asset

- Topper / jackpot / mascot assets:
  - `jackpot-plaque-mini`
  - `jackpot-plaque-minor`
  - `jackpot-plaque-major`
  - `jackpot-plaque-grand`
  - `mascot-rooster` or equivalent dedicated topper hero
  - optional topper halo/backplate asset

- Buy/controls chrome assets:
  - `buybonus-panel`
  - `hold-for-turbo-banner`
  - dedicated `button-turbo`
  - dedicated `button-sound`
  - dedicated `button-settings`
  - dedicated `button-history`

- Big-win / bonus-entry overlay assets:
  - `overlay-big-win-landscape`
  - `overlay-big-win-portrait`
  - `overlay-mega-win-landscape`
  - `overlay-mega-win-portrait`
  - `overlay-total-win-landscape`
  - `overlay-total-win-portrait`
  - `overlay-bonus-tutorial`
  - `overlay-feature-entry`
  - `overlay-buybonus-entry`

- Ambient/VFX assets:
  - coin-fly / coin-rain burst sheets
  - environmental dust / ember layer
  - feature border electricity / reel electricity loop sheets
  - fire / fireball FX sheets

- Audio assets:
  - reel start / reel loop / reel stop set
  - bonus-entry / buy-bonus / lightning-hit / coin-collect set
  - big-win / total-win / feature tutorial stingers
  - provider-specific BGM variants if premium parity is required
