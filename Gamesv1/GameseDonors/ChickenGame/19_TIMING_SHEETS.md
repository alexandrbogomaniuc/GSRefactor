# 19 Timing Sheets

## Scope

- This file separates timing that is directly visible in donor data from timing that is estimated from clips.
- `VERIFIED` means the time comes from donor animation JSON or an already-recorded reconciled artifact.
- `ESTIMATED` means the time is derived from clip duration and visible checkpoints, not frame-by-frame annotation.

## Timing matrix

| Moment | Timing | Type | Basis | Evidence |
|---|---|---|---|---|
| Preloader / intro pacing | `UNOBSERVED` | UNOBSERVED | Committed evidence contains the intro gate still, but no dedicated preloader motion capture. | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/screenshots/desktop/desktop_001_intro.png` |
| Reel-start stagger begins | Reel 1 movement starts at `0.0000`, reel 2 at `0.0833`, reel 3 at `0.1667` inside `spin_speed1_start`. | VERIFIED | `slot_1.json` translate keys for `slot1/slot2/slot3`. | local-only `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/slot_1.json` |
| Blur-onset stagger during spin start | Example blur keys appear at `0.1667`, `0.2500`, `0.3333` across early blur slots. | VERIFIED | `icon_start_blur_*` RGBA keyframes inside `spin_speed1_start`. | local-only `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/slot_1.json` |
| Normal settle window | Finish animation keyframes end at `0.5000`, `0.5833`, and `0.6667` for reels 1/2/3. | VERIFIED | `spin_speed1_finish` translate keys. | local-only `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/slot_1.json` |
| Normal reel-stop cascade | `stopReel1` at `0.4833`, `stopReel2` at `0.5667`, `stopReel3` at `0.6500`; interval is roughly `83 ms` between stops. | VERIFIED | Explicit events in `spin_speed1_finish`. | local-only `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/slot_1.json`, `Gamesv1/GameseDonors/ChickenGame/14_ANIMATION_REFERENCE.md` |
| Bonus suspense hold | `stopReel3` is delayed to `5.0000` in `spin_speed1_finish_bonus`, or about `4.4333 s` after reel 2 stops. | VERIFIED | Explicit events in `spin_speed1_finish_bonus`. | local-only `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/slot_1.json` |
| Full idle-to-spin clip length | `10.64 s` | VERIFIED | Reconciled video index duration. | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/VIDEO_INDEX_RECONCILED.csv` |
| Full non-zero-win clip length | `14.40 s` | VERIFIED | Reconciled video index duration. | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/VIDEO_INDEX_RECONCILED.csv` |
| Buy Bonus proof clip length | `31.20 s`; modal clearly visible around `00:23.40` in sampled notes. | VERIFIED / ESTIMATED | Duration is indexed; visibility point comes from Phase 1D notes and sampled frame. | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/VIDEO_INDEX_RECONCILED.csv`, `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md` |
| Special collect / boost highlight window | Approx `00:10.88 - 00:20.67` inside the `21.76 s` special-state clip. | ESTIMATED | Phase 1D timing note derived from p50 -> p95 sample range. | `Gamesv1/GameseDonors/ChickenGame/06_VFX_AUDIO_TIMINGS.md` |
| Autoplay proof clip length | `82.76 s` | VERIFIED | Reconciled replacement clip duration. | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/VIDEO_INDEX_RECONCILED.csv` |
| Continuous 20-spin proof clip length | `287.00 s` (`04:47.00`) | VERIFIED | Reconciled replacement clip duration. | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/VIDEO_INDEX_RECONCILED.csv`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/TWENTY_SPIN_RECONCILED.md` |
| Average cycle time in the fixed 20-spin run | Approx `14.35 s` per spin/result cycle | ESTIMATED | Calculated as `287.00 / 20`. This is a run-average, not a per-spin constant. | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/TWENTY_SPIN_RECONCILED.md`, replacement duration from `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/VIDEO_INDEX_RECONCILED.csv` |
| Win overlay sequencing | `big_win` and `mega_win` expose one main `action` plus text/coins layers; `total_win` exposes three escalating actions. Exact real-time hold lengths are `UNOBSERVED`. | VERIFIED / UNOBSERVED | Package structure is known, but live trigger thresholds and dwell times were not isolated. | local-only `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/big_win_landscape.json`, `mega_win_landscape.json`, `total_win_landscape.json` |

## Practical timing guidance

- The donor is strongly staggered. It avoids all-three-at-once starts and all-three-at-once stops.
- The biggest hard timing cue for rebuild work is the reel-stop cascade and the bonus-held third reel.
- The collect / boost / special-result presentation occupies a noticeably longer hold window than a plain spin settle.
- Exact preloader pacing and exact win-overlay dwell times remain unknown from committed public evidence and should be tuned in our original implementation rather than guessed as donor truth.
