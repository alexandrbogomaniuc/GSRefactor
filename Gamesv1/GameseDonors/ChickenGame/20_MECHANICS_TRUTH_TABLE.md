# 20 Mechanics Truth Table

## Scope

- This document consolidates the donor mechanics already verified across committed evidence, local-only read-only references, and prior donor evidence branches.
- When a fact depends on prior donor evidence that is not merged into this branch, that provenance is stated explicitly.

## Verified mechanics

| Topic | Truth | Confidence | Evidence / notes |
|---|---|---|---|
| Board geometry | The main board is `3 reels x 4 rows`. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/screenshots/desktop/desktop_002_main_idle.png`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/screenshots/desktop/spin_001_after_stop.png` |
| Pay direction | Wins pay `left to right` and only the highest win per line is paid. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Active paylines | The number of active lines is fixed at `8`. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Exact payline shapes | The donor uses 4 straight lines and 4 diagonals on the 3x4 grid. Exact mapping is listed below. | VERIFIED | Local-only `assets/_donor_raw_local/image/pay_lines_table_mobile.c0a70cb8.png`; previous donor evidence branch `codex/audit/donor-chicken-phase1f-paylines-bets-20260307-1608`, files `Gamesv1/GameseDonors/ChickenGame/13_PAYLINE_SHAPES.md` and `Gamesv1/GameseDonors/ChickenGame/14_BET_LADDER.md` |
| Bet limits | Min bet `0.10 USD`, max bet `20,000 USD`, max win `100,000 USD`. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_017_after_uid_rules_click.txt` |
| Exact bet ladder | The visible ChickenGame USD ladder is listed below and tops out at `200` in the selector even though max bet is much higher. | VERIFIED | Previous donor evidence branch `codex/audit/donor-chicken-phase1f-paylines-bets-20260307-1608`, file `Gamesv1/GameseDonors/ChickenGame/14_BET_LADDER.md` |
| Standard symbol family | Observed regular symbols include cherries, lemons, oranges, plums, grapes, watermelons, BAR, bell, and 777. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/OBSERVED_SPINS.csv`, local-only `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/image/*.png`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/network/network_capture.txt` |
| Collect symbols | Chicken Coin and Super Chicken Coin are distinct feature symbols. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/OBSERVED_SPINS.csv`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/screenshots/desktop/spin_002_after_stop.png` |
| Bonus symbol | Bonus Coin appears only on reels 1 and 3 in the main game and carries random values. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Collect Feature | When Super Chicken / Chicken appear together with Bonus Coins, all relevant visible values are summed and added to the win. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Super Chicken collect extension in bonus | In Bonus Game, Super Chicken also gathers Chicken and other Super Chicken values from the same spin. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Chicken Boost Feature | Super Chicken can trigger one of three random bonuses: multiplier boost, jackpot trigger, or extra coins. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Multiplier set | Documented boost multipliers are `x2`, `x3`, `x5`, `x7`, `x10`; raw local assets also include `x15` coin art/state. | VERIFIED / PARTIAL | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt`, local-only `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/icon_coin_x15.json` |
| Bonus Game trigger | Bonus starts when Bonus Coins land on reels 1 and 3 and Chicken or Super Chicken lands on reel 2. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Bonus Game persistence | Bonus begins with `3 spins` and resets to `3` whenever a Bonus / Chicken / Super Chicken coin lands. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Bonus Game symbol restriction | Only Bonus, Chicken, and Super Chicken coins appear during the bonus mode. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Pile of Gold Feature | A Bonus Coin in the main game may randomly drop extra Bonus / Chicken / Super Chicken coins to help trigger the Bonus Game. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Jackpot tiers | MINI `25x`, MINOR `50x`, MAJOR `150x`, GRAND `1000x` bet. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Observed jackpot table | At bet `0.2/0.3/0.4`, jackpots were captured as `5/10/30/200`, `7.5/15/45/300`, `10/20/60/400`. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/JACKPOT_BET_TABLE.csv` |
| Buy Bonus present | The donor exposes a Buy Bonus modal with three visible tiers. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md` |
| Buy Bonus tiers | How-to states `75 bets`, `200 bets`, `300 bets`. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |
| Buy Bonus sample prices | Reconciled modal evidence shows `45`, `120`, `180` at bet `0.6`. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/BUY_BONUS_RECONCILED.md`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1c-video-20260305-1643/assets/screenshots/screenshot_04_buy_bonus_probe.png` |
| Autoplay | Autoplay is captured in the fixed replacement clip and shows autonomous repeated spins with stop-state `X`. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/AUTOPLAY_RECONCILED.md`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/assets/video/video_08_autoplay_probe_FIXED.mp4` |
| Hold-for-turbo control | A hold/turbo-style control is visibly present on the right-side control stack. | VERIFIED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/screenshots/desktop/desktop_002_main_idle.png`, local-only `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/image/hold_for_turbo.9418c1be.png` |
| Hold-for-turbo exact semantics | The exact behavior of the hold control was not cleanly isolated in reconciled evidence. | PARTIAL | `Gamesv1/GameseDonors/ChickenGame/03_CONTROL_INVENTORY.md`, `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1d-reconcile-20260305-1735/VIDEO_INDEX_RECONCILED.csv` |
| Big / mega / total win thresholds | Dedicated overlay packages exist, but exact trigger thresholds were not proven. | UNOBSERVED | local-only `Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/anims_v5/big_win_landscape.json`, `mega_win_landscape.json`, `total_win_landscape.json` |
| Full playable bonus-board capture | Bonus rules are clear, but a full clean playable bonus-state screen was not directly captured in committed public media. | UNOBSERVED | `Gamesv1/GameseDonors/ChickenGame/assets/_research_runs/ChickenGame/codex-phase1a-20260305-1323/assets/snapshots/snapshot_020_howto_modal.txt` |

## Exact payline mapping

Board convention:
- `rowIndex` is `0` at the top and `3` at the bottom.

Mappings:
- `line_01: [0, 0, 0]`
- `line_02: [1, 1, 1]`
- `line_03: [2, 2, 2]`
- `line_04: [3, 3, 3]`
- `line_05: [0, 1, 2]`
- `line_06: [1, 2, 3]`
- `line_07: [2, 1, 0]`
- `line_08: [3, 2, 1]`

## Exact bet ladder

Visible selector ladder:
- `0.1`
- `0.2`
- `0.3`
- `0.4`
- `0.5`
- `0.6`
- `0.7`
- `0.8`
- `0.9`
- `1`
- `2`
- `3`
- `4`
- `5`
- `6`
- `7`
- `8`
- `9`
- `10`
- `15`
- `20`
- `25`
- `50`
- `75`
- `100`
- `150`
- `200`

## Remaining unknowns

- Exact RTP and internal reel-strip math.
- Exact probability weights for Boost outcomes and jackpot selection.
- Exact big / mega / total win thresholds.
- Exact hold-for-turbo semantics.
