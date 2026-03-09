# 14 Animation Reference

## Scope

- This file is reference documentation only.
- It summarizes staged donor animation names from the local-only donor archive and aligns them with already-committed donor evidence.
- Do not copy donor animation files, atlas images, or audio into runtime.
- Local-only donor archive prerequisite: `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/` populated per `Gamesv1/GameseDonors/ChickenGame/LOCAL_ONLY_DONOR_ASSETS.md`.

## Sources

- Local-only animation JSON references:
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
- Committed donor timing reference: `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`
- Reconciled visual evidence for staged feature moments:
  - `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video/video_08_autoplay_probe_FIXED.mp4`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video/video_11_twenty_spins_continuous_FIXED.mp4`
  - `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1c-video-20260305-1643/assets/video/video_10_special_effect_or_collect_state.mp4`

## Gold Chicken

Verified state names from `gold_chicken.json`:

- `idle`
- `action1`
- `action2`
- `action3_start`
- `action3_loop`
- `action3_finish`

Observed structure notes:

- The atlas and skeleton include dedicated lighting and fire-related slots such as `fire`, `lighting_back`, `lighting_back2`, `lighting_back3`, and `lighting_back4`.
- The atlas also includes coin and total-readout elements, which supports a staged feature character rather than a static symbol treatment.

Runtime use guidance:

- VERIFIED: the donor package contains a multi-state top-character animation set rather than a single loop.
- INFERENCE: `idle` is the ambient state between feature escalations.
- INFERENCE: `action1` and `action2` are intermediate escalation beats.
- INFERENCE: `action3_start -> action3_loop -> action3_finish` is the strongest staged cycle and should be treated as an enter / sustain / exit chain in our original runtime.

## FX Fire

Verified state names from `fx_fire.json`:

- `start_back`
- `start_front`
- `idle_back`
- `idle_front`
- `finish_back`
- `finish_front`

Observed structure notes:

- `fx_fire.json` exposes front and back variants for each phase.
- The donor asset is layered, not a single flat flipbook.

Runtime use guidance:

- VERIFIED: the donor package separates fire startup, sustain, and finish.
- INFERENCE: the front/back pairs are intended to bracket the collector or top-character layer so fire can sit both behind and in front of the primary subject.

## Lightning Path

Verified state names from `lightning_path.json`:

- `action`

Observed structure notes:

- The skeleton contains `lightning`, `sparks`, and `sparks2` slots.
- Local-only donor audio also includes `chickenLightningHit_01` through `chickenLightningHit_04` and `slotReelElectricity`, which supports a short, impact-driven electrical effect chain.

Runtime use guidance:

- VERIFIED: lightning is packaged as a discrete triggered action, not a persistent idle loop.
- INFERENCE: this effect should be treated as a transient attach / hit path that fires on a feature or boosted result moment.

## Coin Fly

Verified state names from `coin_fly.json`:

- `start_1_1`
- `start_1_2`
- `start_1_3`
- `start_2`
- `start_3`

Verified event from `coin_fly.json`:

- `complete`

Observed structure notes:

- The skeleton includes `coin_glow` and `coin` slots with multiple path bones.
- Local-only donor audio includes `coinCollectMagnetize`, `coinCollectBounce`, and `coinCollectCount`.

Runtime use guidance:

- VERIFIED: the donor package uses multiple staged coin-flight entries instead of one generic coin burst.
- INFERENCE: `start_1_1`, `start_1_2`, and `start_1_3` are variant first-hop launches.
- INFERENCE: `start_2` and `start_3` are follow-through stages toward the destination counter or collector target.
- VERIFIED: `complete` is the safest handoff point for counter updates or chained effect cleanup.

## Win Overlays

Verified overlay state names:

- `big_win_landscape.json`: `action`, `action_coins`, `action_text`
- `big_win_mobile.json`: `action`, `action_coins`, `action_text`
- `mega_win_landscape.json`: `action`, `action_coins`, `action_text`
- `mega_win_mobile.json`: `action`, `action_coins`, `action_text`
- `total_win_landscape.json`: `action`, `action2`, `action3`, `action_coins`, `action_text`
- `total_win_mobile.json`: `action`, `action2`, `action3`, `action_coins`, `action_text`

Observed structure notes:

- VERIFIED: the donor package ships distinct landscape and mobile overlay variants.
- VERIFIED: `total_win` has a deeper staged sequence than `big_win` or `mega_win`.
- Committed evidence does not prove exact donor thresholds for when each overlay is shown.

Runtime use guidance:

- VERIFIED: the donor runtime supports at least three named win-surface tiers: big, mega, and total.
- INFERENCE: `action` is the entry phase, `action_text` handles typography timing, and `action_coins` runs parallel coin dressing.
- INFERENCE: `total_win` uses `action2` and `action3` as additional escalation beats beyond the simpler big/mega overlays.

## Reel Stop Event Usage

Verified event names from `slot_1.json`:

- `stopReel1`
- `stopReel2`
- `stopReel3`

Verified event placement:

- `spin_speed1_finish` emits:
  - `stopReel1` at `0.4833`
  - `stopReel2` at `0.5667`
  - `stopReel3` at `0.6500`
- `spin_speed1_finish_bonus` emits:
  - `stopReel1` at `0.4833`
  - `stopReel2` at `0.5667`
  - `stopReel3` at `5.0000`

Runtime use guidance:

- VERIFIED: the donor reel shell uses per-reel stop events, not a single generic settle callback.
- VERIFIED: the bonus finish timeline holds the final reel stop much longer than the standard finish timeline.
- INFERENCE: our original game should keep explicit reel stop hooks in the animation/event model so audio, symbol landing effects, and feature checks can be staged per reel.
- Local-only audio filenames that support this cadence:
  - `slotReelStart.*`
  - `slotReelRoll.*`
  - `slotReelStop_2reels.*`
  - `slotReelStop_3ndReel.*`
  - `slotReelStop_3reels.*`

## Recommended Runtime State Model For Our Original Game

Use the donor structure as a staging reference, not as a direct asset or naming dependency.

### Reel shell

- `idle`
- `spin_start`
- `spin_loop`
- `reel_stop_1`
- `reel_stop_2`
- `reel_stop_3`
- `result_hold`
- `bonus_result_hold`

Mapping note:

- VERIFIED donor references: `spin_speed1_start`, `spin_speed1_idle`, `spin_speed1_finish`, `spin_speed1_finish_bonus`, `stopReel1`, `stopReel2`, `stopReel3`
- INFERENCE: rename these to engine-neutral runtime states and event hooks in our implementation.

### Collector or top-character layer

- `idle`
- `escalate_1`
- `escalate_2`
- `feature_enter`
- `feature_loop`
- `feature_exit`

Mapping note:

- VERIFIED donor references: `idle`, `action1`, `action2`, `action3_start`, `action3_loop`, `action3_finish`
- INFERENCE: keep the same progression shape while replacing the donor character/theme entirely.

### Fire and electricity overlays

- `fire_start_back`
- `fire_start_front`
- `fire_idle_back`
- `fire_idle_front`
- `fire_finish_back`
- `fire_finish_front`
- `lightning_action`

Mapping note:

- VERIFIED donor references: `start_back`, `start_front`, `idle_back`, `idle_front`, `finish_back`, `finish_front`, `action`
- INFERENCE: front/back split is the safest way to preserve depth without donor art reuse.

### Coin transfer

- `coin_launch_variant_a`
- `coin_launch_variant_b`
- `coin_launch_variant_c`
- `coin_mid_flight`
- `coin_arrival`
- `coin_complete`

Mapping note:

- VERIFIED donor references: `start_1_1`, `start_1_2`, `start_1_3`, `start_2`, `start_3`, `complete`
- INFERENCE: normalize the donor sequence into gameplay-meaningful state names for our engine.

### Win overlays

- `win_tier_entry`
- `win_tier_text`
- `win_tier_coins`
- `win_tier_escalation_2`
- `win_tier_escalation_3`

Mapping note:

- VERIFIED donor references: `action`, `action_text`, `action_coins`, `action2`, `action3`
- INFERENCE: only higher-intensity overlays need the escalation stages.

## Constraints

- UNOBSERVED: exact donor runtime rules for when each staged animation is triggered.
- UNOBSERVED: exact donor big-win, mega-win, and total-win thresholds.
- UNOBSERVED: exact internal orchestration between skeleton animation callbacks and UI controller code.
- Do not treat local-only donor filenames as a shipped asset contract.
