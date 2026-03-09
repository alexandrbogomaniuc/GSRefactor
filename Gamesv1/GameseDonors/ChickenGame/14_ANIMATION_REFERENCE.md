# 14 Animation Reference

## Scope

- This file is reference documentation only.
- It summarizes the donor's staged animation system from the local-only donor archive plus already-committed donor evidence.
- Do not copy donor animation files, donor atlas images, or donor audio into runtime.
- Local-only donor prerequisite: `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/` populated per `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`.

## Sources

- Local-only Spine JSON references:
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/gold_chicken.json`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/fx_fire.json`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/lightning_path.json`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/coin_fly.json`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/slot_1.json`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/big_win_landscape.json`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/big_win_mobile.json`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/mega_win_landscape.json`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/mega_win_mobile.json`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/total_win_landscape.json`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/total_win_mobile.json`
- Committed donor references:
  - `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`
  - `Gamesv1/GameseDonors/ChickenGame/10_PROMO_VIDEO_BRIEF.md`

## Gold Chicken

Verified animation states from `gold_chicken.json`:

- `idle`
- `action1`
- `action2`
- `action3_start`
- `action3_loop`
- `action3_finish`

Reference guidance:

- VERIFIED: the donor topper/mascot is not a single loop; it has a staged escalation chain.
- INFERENCE: `idle` is the ambient state between feature moments.
- INFERENCE: `action1` and `action2` are intermediary response beats.
- INFERENCE: `action3_start -> action3_loop -> action3_finish` is the strongest enter / sustain / exit chain and should be mirrored as a three-phase feature reaction in our original game.

## FX Fire

Verified layered states from `fx_fire.json`:

- `start_back`
- `start_front`
- `idle_back`
- `idle_front`
- `finish_back`
- `finish_front`

Reference guidance:

- VERIFIED: the donor fire effect is layered front/back and split into start, loop, and finish phases.
- INFERENCE: the front/back split is intended to bracket the mascot or collector depth layer rather than behave like one flat overlay.

## Lightning Path

Verified state from `lightning_path.json`:

- `action`

Reference guidance:

- VERIFIED: lightning is packaged as a discrete triggered action, not a persistent idle loop.
- INFERENCE: this should be modeled as a short boost/collect hit path attached to a result event, then cleaned up immediately after impact.

## Coin Fly

Verified sequence states from `coin_fly.json`:

- `start_1_1`
- `start_1_2`
- `start_1_3`
- `start_2`
- `start_3`

Verified event from `coin_fly.json`:

- `complete`

Reference guidance:

- VERIFIED: the donor uses multiple coin-flight entry states rather than one generic coin burst.
- INFERENCE: `start_1_1`, `start_1_2`, and `start_1_3` are variant launch beats.
- INFERENCE: `start_2` and `start_3` are follow-through stages toward the collector or win counter destination.
- VERIFIED: `complete` is the safest handoff for counter update, chained cleanup, or next-stage staging.

## Win Overlays

Verified overlay states:

- `big_win_landscape.json`: `action`, `action_coins`, `action_text`
- `big_win_mobile.json`: `action`, `action_coins`, `action_text`
- `mega_win_landscape.json`: `action`, `action_coins`, `action_text`
- `mega_win_mobile.json`: `action`, `action_coins`, `action_text`
- `total_win_landscape.json`: `action`, `action2`, `action3`, `action_coins`, `action_text`
- `total_win_mobile.json`: `action`, `action2`, `action3`, `action_coins`, `action_text`

Reference guidance:

- VERIFIED: the donor runtime supports distinct `big_win`, `mega_win`, and `total_win` surfaces.
- VERIFIED: `total_win` has a deeper staged sequence than `big_win` or `mega_win`.
- INFERENCE: `action` is the entry beat, `action_text` and `action_coins` are typography/coin dressing layers, and `action2`/`action3` are later escalation phases for `total_win`.

## Reel Stop Event Usage

Verified event names from `slot_1.json`:

- `stopReel1`
- `stopReel2`
- `stopReel3`

Verified event placement:

- `spin_speed1_finish`
  - `stopReel1` at `0.4833`
  - `stopReel2` at `0.5667`
  - `stopReel3` at `0.6500`
- `spin_speed1_finish_bonus`
  - `stopReel1` at `0.4833`
  - `stopReel2` at `0.5667`
  - `stopReel3` at `5.0000`

Reference guidance:

- VERIFIED: the donor shell uses per-reel stop events rather than one generic settle callback.
- VERIFIED: the bonus finish timeline holds the final reel stop much longer than the standard finish timeline.
- INFERENCE: our original game should expose reel-stop triggers explicitly so topper reactions, lightning timing, coin flights, and overlay escalation can key off per-reel milestones.

## Recommended Runtime State Model For Our Original Game

Use the donor only as staging inspiration. Replace all art, naming, and branding with original assets.

### Topper / mascot

- `idle`
- `react_collect`
- `react_boost_start`
- `react_boost_loop`
- `react_boost_finish`
- `react_jackpot`
- `react_bigwin`

Mapping note:

- VERIFIED donor reference: `idle`, `action1`, `action2`, `action3_start`, `action3_loop`, `action3_finish`
- INFERENCE: keep the same escalation shape while renaming to gameplay-driven states rather than donor-specific ones.

### Layered FX

- `fire_back_start`
- `fire_front_start`
- `fire_back_loop`
- `fire_front_loop`
- `fire_back_finish`
- `fire_front_finish`
- `lightning_action`
- `coin_fly_launch`
- `coin_fly_travel`
- `coin_fly_complete`

Mapping note:

- VERIFIED donor reference: `start_back`, `start_front`, `idle_back`, `idle_front`, `finish_back`, `finish_front`, `action`, `start_1_1`, `start_1_2`, `start_1_3`, `start_2`, `start_3`, `complete`
- INFERENCE: the safest original runtime shape is a layered FX controller with explicit start/loop/finish lifecycles and a separate coin-flight completion handoff.

### Win overlays

- `big_win_enter`
- `big_win_loop`
- `mega_win_enter`
- `mega_win_loop`
- `total_win_enter`
- `total_win_phase_2`
- `total_win_phase_3`
- `win_overlay_exit`

Mapping note:

- VERIFIED donor reference: `action`, `action2`, `action3`, `action_coins`, `action_text`
- INFERENCE: keep overlay choreography stateful so typography, coins, and escalation can be layered independently.

### Reel event hooks

- `onReelStop1`
- `onReelStop2`
- `onReelStop3`
- `onBonusHoldFinalStop`

Mapping note:

- VERIFIED donor reference: `stopReel1`, `stopReel2`, `stopReel3` in both normal and bonus finish sequences
- INFERENCE: retain explicit reel-stop hooks in engine code instead of collapsing everything into one generic settle callback.

## Implementation Boundary

- This document is for behavior and staging reference only.
- Do not import donor Spine timelines, donor images, or donor audio into production runtime.
- Use this reference to shape original mascot reactions, original layered FX, original win overlays, and original reel-event choreography.
