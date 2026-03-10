# 21 Runtime Asset Slot Contract

## Scope

- This is the donor-driven presentation slot contract our original game would need to reproduce the donor's presentation depth with original art.
- `Current game status` is assessed against the committed beta provider contract in `Gamesv1/games/7000/src/app/assets/providerPackRegistry.ts` on this donor-doc branch.
- Status values:
  - `HAS`: the current game/provider contract already exposes this slot or a very close equivalent.
  - `PARTIAL`: the current game has a generic or code-only substitute, but not a donor-depth slot.
  - `MISSING`: no current committed slot/contract coverage.

## Core shell slots

| Slot id | Purpose | Required state transitions | Current game status | Evidence / note |
|---|---|---|---|---|
| `preloader.wordmark` | Main brand/logo shown on preload and intro. | `show -> pulse/idle -> fade_out` | HAS | `Gamesv1/games/7000/src/app/assets/providerPackRegistry.ts` requires wordmark; donor evidence: `desktop_001_intro.png` |
| `preloader.background.desktop` | Landscape preload/intro background. | `show -> hold -> fade_out` | HAS | `Gamesv1/games/7000/src/app/assets/providerPackRegistry.ts` requires desktop background |
| `preloader.background.landscape` | Safe-crop landscape background. | `show -> hold -> fade_out` | HAS | `Gamesv1/games/7000/src/app/assets/providerPackRegistry.ts` requires landscape background |
| `preloader.background.portrait` | Portrait preload/intro background. | `show -> hold -> fade_out` | HAS | `Gamesv1/games/7000/src/app/assets/providerPackRegistry.ts` requires portrait background |
| `intro.card.boost` | Feature card/surface that teases the boost mechanic on the gate screen. | `show -> idle -> hide` | MISSING | Donor intro gate and local-only `startScreen1/2` art |
| `intro.card.collect` | Feature card/surface that teases the collect mechanic on the gate screen. | `show -> idle -> hide` | MISSING | Donor intro gate and local-only `startScreen1/2` art |
| `cabinet.frame` | Main reel cabinet border and mask frame. | `idle -> highlighted -> settle` | HAS | `Gamesv1/games/7000/src/app/assets/providerPackRegistry.ts` requires `uiAtlas.reel-frame-panel` |
| `cabinet.separator.vertical` | Vertical reel separators. | static | HAS | `runtime/ui_keys.json` has `reel-separator-vertical` |
| `cabinet.separator.horizontal` | Horizontal row separators or line accents. | static / subtle pulse | HAS | `runtime/ui_keys.json` has `reel-separator-horizontal` |
| `cabinet.paylinePill` | Small payline count / pill accent. | `idle -> highlighted` | HAS | `runtime/ui_keys.json` has `payline-pill` |
| `hud.balancePanel` | Skinned donor-like balance surface if provider-specific. | `idle -> update` | PARTIAL | Current shell renders balance, but provider contract does not require a donor-specific panel frame |
| `hud.betPanel` | Skinned bet selection surface. | `idle -> update -> disabled` | PARTIAL | Current shell renders bet UI, but provider contract does not require a donor-specific panel frame |
| `hud.lastWinPanel` | Surface for `LAST WIN` readout. | `hidden -> show -> update -> hide` | PARTIAL | Last-win text is visible in evidence; no dedicated provider slot exists yet |
| `hud.cornerBrand` | Small persistent logo while gameplay is active. | `show -> static` | PARTIAL | Gameplay logo visible, but current provider contract treats wordmark as preload-only |

## Topper and jackpot slots

| Slot id | Purpose | Required state transitions | Current game status | Evidence / note |
|---|---|---|---|---|
| `topper.mascot.idle` | Ambient mascot above the cabinet. | `idle_loop` | MISSING | Donor `gold_chicken.idle` |
| `topper.mascot.react_collect` | Short mascot response when collect resolves. | `enter -> hold -> exit` | MISSING | Donor `action1` / `action2` chain |
| `topper.mascot.boost_start` | Entry into the strongest boost state. | `enter` | MISSING | Donor `action3_start` |
| `topper.mascot.boost_loop` | Sustained boost/jackpot/high-energy loop. | `loop` | MISSING | Donor `action3_loop` |
| `topper.mascot.boost_finish` | Exit from the strongest state back to idle. | `exit -> idle` | MISSING | Donor `action3_finish` |
| `topper.mascot.jackpot` | Jackpot-specific reaction, if split from boost. | `enter -> loop -> exit` | MISSING | INFERENCE from mascot/jackpot co-location |
| `topper.jackpot.mini` | Persistent MINI plaque/readout. | `idle -> update -> celebrate` | MISSING | Visible above cabinet in idle |
| `topper.jackpot.minor` | Persistent MINOR plaque/readout. | `idle -> update -> celebrate` | MISSING | Visible above cabinet in idle |
| `topper.jackpot.major` | Persistent MAJOR plaque/readout. | `idle -> update -> celebrate` | MISSING | Visible above cabinet in idle |
| `topper.jackpot.grand` | Persistent GRAND plaque/readout. | `idle -> update -> celebrate` | MISSING | Visible above cabinet in idle |

## Control slots

| Slot id | Purpose | Required state transitions | Current game status | Evidence / note |
|---|---|---|---|---|
| `controls.spin.normal` | Main spin button default state. | `normal -> pressed -> disabled -> normal` | HAS | `runtime/ui_keys.json` has `button-spin` |
| `controls.spin.pressed` | Main spin button pressed/down state. | `pressed` | HAS | `runtime/ui_keys.json` has `button-spin-pressed` |
| `controls.bet.minus` | Bet decrement control. | `normal -> pressed -> disabled` | HAS | `runtime/ui_keys.json` has `button-bet-minus` |
| `controls.bet.plus` | Bet increment control. | `normal -> pressed -> disabled` | HAS | `runtime/ui_keys.json` has `button-bet-plus` |
| `controls.buyBonus.normal` | Buy Bonus entry control default. | `normal -> pressed -> disabled` | HAS | `runtime/ui_keys.json` has `button-buybonus` |
| `controls.buyBonus.pressed` | Buy Bonus pressed state. | `pressed` | HAS | `runtime/ui_keys.json` has `button-buybonus-pressed` |
| `controls.autoplay.normal` | Autoplay entry control default. | `normal -> pressed -> running` | HAS | `runtime/ui_keys.json` has `button-autoplay` |
| `controls.autoplay.pressed` | Autoplay pressed state. | `pressed` | HAS | `runtime/ui_keys.json` has `button-autoplay-pressed` |
| `controls.stop.run` | Explicit stop-state button during autoplay/run. | `hidden -> show -> hide` | MISSING | Reconciled autoplay shows `X`, but current provider contract has no dedicated stop slot |
| `controls.turbo.hold` | Hold-for-turbo affordance. | `idle -> held -> release` | MISSING | Visible donor control exists; current provider contract has no dedicated slot |
| `controls.menu` | Burger/menu button. | `idle -> pressed -> active` | MISSING | Visible in gameplay, not covered by provider contract |
| `controls.settings` | Settings entry point. | `idle -> pressed -> active` | MISSING | Visible lower-left gear/settings entry |
| `controls.historyEntry` | My Bet History entry affordance. | `idle -> active` | MISSING | Present in menu evidence |
| `controls.soundToggle` | Sound on/off row within settings. | `off -> on` | MISSING | Visible settings popup |
| `controls.musicToggle` | Music on/off row within settings. | `off -> on` | MISSING | Visible settings popup |

## Symbol and value slots

| Slot id | Purpose | Required state transitions | Current game status | Evidence / note |
|---|---|---|---|---|
| `symbols.base.{cherry, lemon, orange, plum, grape, watermelon, bell, bar, seven}` | Main reel symbols used for line wins. | `enter -> idle -> settle` | PARTIAL | Current provider contract exposes only a reduced symbol set; donor uses more symbol identities |
| `symbols.feature.bonusCoin` | Bonus Coin used for bonus trigger and value carrier. | `enter -> idle -> emphasize -> settle` | PARTIAL | Current contract has generic `symbol-7-coin`, not a donor-depth bonus slot |
| `symbols.feature.chickenCoin` | Collect coin in main/bonus play. | `enter -> idle -> emphasize -> settle` | PARTIAL | Current contract has generic coin/bolt/rooster analogs only |
| `symbols.feature.superChickenCoin` | Stronger collector/boost coin. | `enter -> idle -> emphasize -> settle` | PARTIAL | Donor has a distinct super coin state |
| `symbols.feature.multiplier.{x2,x3,x5,x7,x10,x15}` | Multiplier-carrying coin states. | `enter -> idle -> emphasize -> resolve` | MISSING | Donor raw archive has dedicated packages; current contract does not |
| `symbols.feature.jackpot.{mini,minor,major,grand}` | Jackpot-carrying coin states. | `enter -> idle -> attach -> resolve` | MISSING | Donor raw archive has dedicated jackpot coin packages |

## FX and overlay slots

| Slot id | Purpose | Required state transitions | Current game status | Evidence / note |
|---|---|---|---|---|
| `fx.boardBorder.primary` | Large energized frame around the cabinet. | `enter -> loop -> exit` | MISSING | Donor `border.json` |
| `fx.boardBorder.secondary` | Small or sub-panel border accent. | `enter -> loop -> exit` | MISSING | Donor `border_small.json` |
| `fx.fire.back` | Back-layer flame sheet behind the mascot/cabinet. | `start -> idle -> finish` | MISSING | Donor `fx_fire.start_back / idle_back / finish_back` |
| `fx.fire.front` | Front-layer flame sheet in front of the mascot/cabinet. | `start -> idle -> finish` | MISSING | Donor `fx_fire.start_front / idle_front / finish_front` |
| `fx.lightning.path` | Lightning arc / spark hit path. | `action` | PARTIAL | Current provider contract has lightning arc frames but not the full donor-style state machine |
| `fx.coinFly` | Multi-stage coin travel between board and destination. | `launch -> travel -> arrive -> complete` | MISSING | Donor `coin_fly.json` |
| `fx.fireball` | Projectile / burst accent. | `spawn -> loop -> despawn` | MISSING | Donor `fx_fireball.json` |
| `overlay.buyBonus.card.blitz` | First buy-bonus tier card art/idle package. | `show -> idle -> hide` | MISSING | Donor `buy_bonus_blitz.json` |
| `overlay.buyBonus.card.power` | Second buy-bonus tier card art/idle package. | `show -> idle -> hide` | MISSING | Donor `buy_bonus_power.json` |
| `overlay.buyBonus.card.ultimate` | Third buy-bonus tier card art/idle package. | `show -> idle -> hide` | MISSING | Donor `buy_bonus_ultimate.json` |
| `overlay.rules` | Rules modal skin. | `show -> hide` | MISSING | Visible rules panel in committed evidence |
| `overlay.howTo` | How-to modal skin and content frame. | `show -> hide` | MISSING | Visible how-to panel in committed evidence |
| `overlay.menu` | Menu list panel / dimmer. | `show -> hide` | MISSING | Menu open evidence |
| `overlay.settings` | Compact settings popup skin. | `show -> hide` | MISSING | Settings popup evidence |
| `overlay.bonusTutorial` | Tutorial / explainer overlay for bonus mode. | `show -> step1-4 -> hide` | MISSING | Donor `bonus_game_tutorial.json` |
| `overlay.bigWin` | Big win overlay package. | `enter -> hold -> exit` | MISSING | Donor `big_win_*` packages |
| `overlay.megaWin` | Mega win overlay package. | `enter -> hold -> exit` | MISSING | Donor `mega_win_*` packages |
| `overlay.totalWin` | Total win overlay package. | `enter -> phase2 -> phase3 -> exit` | MISSING | Donor `total_win_*` packages |
| `overlay.totalWinTab` | Total-summary tab treatment. | `start -> idle -> finish` | MISSING | Donor `totalwin_tab.json` |
| `overlay.totalWinDots` | Count-step or meter helper for summary progression. | `idle_n -> add/remove` | MISSING | Donor `dots.json` |

## Contract conclusion

- The current committed provider contract is enough for a lightweight provider skin, not for donor-depth presentation parity.
- The biggest missing areas are topper states, jackpot plaques, feature-symbol depth, buy-bonus card packages, layered FX, and win overlays.
- If Engineering wants donor-grade staging with original assets, this slot contract needs to become the source of truth rather than the current minimal atlas validation set.
