# 09 Asset Generation Master Brief

## Objective
- Produce fully original high-resolution production assets for a new game inspired by observed mechanics only.

## Target output resolutions
- Base design canvas: `1920x1080` (landscape first).
- Mobile portrait adaptation: `1080x1920` safe composition.
- Export UI-safe variants for 16:9, 19.5:9, and 9:16 crops.

## Atlas grouping
- `atlas_preload_ui`: logo, loading, frame scaffolds, legal/footer marks.
- `atlas_symbols_main`: standard symbols, premium symbols, coin symbols.
- `atlas_symbols_feature`: collect/boost/bonus-only symbols and states.
- `atlas_hud_controls`: spin/turbo/autoplay/buy/settings/history buttons.
- `atlas_vfx`: bursts, streaks, glows, numerics, overlays.
- `atlas_popups`: rules, buy-bonus modal, tab headers, cards.

## Naming conventions
- Symbols: `sym_<family>_<tier>_<state>`
- Coins: `coin_<class>_<valueBand>_<state>`
- HUD: `hud_<control>_<state>`
- VFX: `fx_<event>_<layer>_<variant>_<frame>`
- Audio cues: `sfx_<event>_<intensity>_<variant>`

## Animation frame guidelines
- Symbol idle loops: 12-24 fps authored loops, 20-40 frames.
- Spin-impact and collect hits: 24-30 fps, 8-20 key frames with additive overlay option.
- Big celebratory overlays: 24 fps with separate alpha/light layers for perf control.
- Keep heavy full-screen effects short and layered to support mobile performance gates.

## UI kit requirements
- Deliver icon sets for: spin, turbo, autoplay, buy feature, settings, history, sound.
- Provide normal/hover/pressed/disabled/selected states.
- Include accessibility-friendly contrast variants.
- Include numeric font atlas for balance/bet/win readouts.

## VFX sprite needs
- Collect pulse, boost lightning equivalent, jackpot attach burst, counter pop, trail sweeps.
- Tiered win overlays: small/medium/large/intense.
- Bonus-entry transition pack and modal reveal transitions.

## Donor-reference boundaries
- Reference only: mechanic categories verified in evidence (`Collect`, `Boost`, `Bonus`, `Buy Bonus`, jackpots).
  - Evidence: `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`
- Do not trace, recolor, or retouch donor captures into production assets.
